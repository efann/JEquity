/*
 * JEquity
 * Copyright(c) 2008-2020, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.controller;

import com.beowurks.jequity.controller.tab.TabFinancialController;
import com.beowurks.jequity.controller.tab.TabGroupController;
import com.beowurks.jequity.controller.tab.TabHistoricalGraphController;
import com.beowurks.jequity.controller.tab.TabReportController;
import com.beowurks.jequity.controller.tab.TabSymbolController;
import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.hibernate.threads.ThreadDownloadSymbolInfo;
import com.beowurks.jequity.dao.tableview.EnvironmentProperty;
import com.beowurks.jequity.dao.web.PageScraping;
import com.beowurks.jequity.utility.Misc;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class MainFormController implements EventHandler<ActionEvent>
{
  //********************************************************************************
  // From https://stackoverflow.com/questions/23592148/javafx-nested-controller
  // The name of the controller variable of an included file is always the fx:id value with "Controller" added on to it.

  @FXML
  private MenuController menuMainController;

  @FXML
  private ToolbarController toolbarMainController;

  @FXML
  private TabGroupController tabGroupMainController;

  @FXML
  private TabSymbolController tabSymbolMainController;

  @FXML
  private TabFinancialController tabFinancialMainController;

  @FXML
  private TabHistoricalGraphController tabHistoricalGraphMainController;

  @FXML
  private TabReportController tabReportMainController;
  //********************************************************************************
  // Status Bar

  @FXML
  private Label statusMessage;
  @FXML
  private ProgressBar progressBar;
  @FXML
  private Label progressLabel;

  //********************************************************************************

  @FXML
  private TabPane tabPane;

  @FXML
  private Tab tabFinancial;

  @FXML
  private Tab tabSymbol;

  @FXML
  private Tab tabReports;

  @FXML
  private Tab tabHistorical;

  @FXML
  private Tab tabGroup;

  private final ObservableList<EnvironmentProperty> foDataList = FXCollections.observableArrayList();

  @FXML
  private TableView tblGroup;

  @FXML
  private TableColumn colID;

  @FXML
  private TableColumn colDescription;

  private boolean flShowPrintDialog = false;

  // ---------------------------------------------------------------------------------------------------------------------
  // From https://stackoverflow.com/questions/34785417/javafx-fxml-controller-constructor-vs-initialize-method
  @FXML
  public void initialize()
  {
    this.setupTooltips();

    this.setupListeners();

    this.setupTables();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupTables()
  {
    // You have to setup here and not in the tabFinancialController as the tabFinancialController
    // is created before the MainFormController.
    this.tabFinancialMainController.getTable().setStatusMessage(this.statusMessage);
    this.tabSymbolMainController.getTable().setStatusMessage(this.statusMessage);
    this.tabGroupMainController.getTable().setStatusMessage(this.statusMessage);

  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupTooltips()
  {
    this.toolbarMainController.getRefreshButton().setTooltip(new Tooltip("Refresh (reload) all of the data"));

    this.toolbarMainController.getUpdateButton().setTooltip(new Tooltip("Update the daily stock information"));

    this.toolbarMainController.getGroupComboBox().setTooltip(new Tooltip("Select the current group to display"));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupListeners()
  {
    this.menuMainController.getMenuRefresh().setOnAction(toActionEvent -> MainFormController.this.refreshAllComponents(true));
    this.toolbarMainController.getRefreshButton().setOnAction(toActionEvent -> MainFormController.this.refreshAllComponents(true));

    this.menuMainController.getMenuUpdate().setOnAction(toActionEvent -> MainFormController.this.updateSymbolData());
    this.toolbarMainController.getUpdateButton().setOnAction(toActionEvent -> MainFormController.this.updateSymbolData());

    this.menuMainController.getMenuPrint().setOnAction(toActionEvent -> MainFormController.this.printReport());

    this.tabPane.getSelectionModel().selectedItemProperty().addListener(
      (toObservableValue, toPrevious, toCurrent) -> this.refreshAllComponents(false)
    );

    this.toolbarMainController.getGroupComboBox().setOnAction(this);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void updateSymbolData()
  {
    ThreadDownloadSymbolInfo.INSTANCE.start(true);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void printReport()
  {
    this.flShowPrintDialog = true;

    final SingleSelectionModel<Tab> loSelection = this.tabPane.getSelectionModel();
    if (loSelection.getSelectedItem() == this.tabReports)
    {
      this.refreshAllComponentsFunction(false);
    }
    else
    {
      loSelection.select(this.tabReports);
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void refreshAllComponents(final boolean tlIncludeGroupComboBox)
  {
    Misc.setCursor(Cursor.WAIT);

    if (Platform.isFxApplicationThread())
    {
      this.refreshAllComponentsFunction(tlIncludeGroupComboBox);
    }
    else
    {
      Platform.runLater(() ->
        this.refreshAllComponentsFunction(tlIncludeGroupComboBox));
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  // So that code will not be duplicated in refreshAllComponents.
  private void refreshAllComponentsFunction(final boolean tlIncludeGroupComboBox)
  {
    final DateFormat loDateFormat = new SimpleDateFormat("h:mm:ss a");
    final Calendar loCalender = Calendar.getInstance();
    // I don't know of another way for lowercase am/pm.
    final String lcTime = loDateFormat.format(loCalender.getTime()).toLowerCase();

    // Refresh the Group Table also.
    if (tlIncludeGroupComboBox)
    {
      PageScraping.INSTANCE.refreshData();
      this.tabGroupMainController.refreshData();
    }

    final boolean llRefreshCombo = (tlIncludeGroupComboBox || (this.toolbarMainController.getGroupComboBox().getSelectionModel().getSelectedItem() == null));
    final Integer loGroupID = (llRefreshCombo) ? this.toolbarMainController.refreshGroupComboBox() : this.toolbarMainController.getGroupComboBox().getSelectedItem().getKey();

    HibernateUtil.INSTANCE.setGroupID(loGroupID);

    final Tab loCurrentTab = this.tabPane.getSelectionModel().getSelectedItem();

    if (loCurrentTab == this.tabFinancial)
    {
      Misc.setStatusText(String.format("Refreshed the Financial grid @ %s. . . .", lcTime));

      this.tabFinancialMainController.refreshData();
    }
    else if (loCurrentTab == this.tabSymbol)
    {
      Misc.setStatusText(String.format("Refreshed the Daily grid @ %s. . . .", lcTime));

      this.tabSymbolMainController.refreshData();
    }
    // Called by both the Report tab and the Print menu option.
    else if (loCurrentTab == this.tabReports)
    {
      final var lcFont = "Arial";
      if (Misc.checkForFontAvailability(lcFont))
      {
        Misc.setStatusText(String.format("Refreshed the Financial Report @ %s. . . .", lcTime));

        this.tabReportMainController.refreshReport(this.flShowPrintDialog);
        this.flShowPrintDialog = false;
      }
      else
      {
        final StringBuilder lcMessage = new StringBuilder();
        lcMessage.append(String.format("Your %s system is missing the %s font.\n", System.getProperty("os.name"), lcFont));
        lcMessage.append("By the way, this font is standard on Windows and Mac OS X.\n");
        lcMessage.append("If you're using Linux, try running the following commands:\n\n");
        lcMessage.append("\tsudo apt install ttf-mscorefonts-installer\n");
        lcMessage.append("\tsudo fc-cache -f -v\n\n");
        lcMessage.append("Or google 'ubuntu install arial' or 'linux install arial'");

        Misc.errorMessage(lcMessage.toString());
      }
    }
    else if (loCurrentTab == this.tabHistorical)
    {
      Misc.setStatusText(String.format("Refreshed the Historical data @ %s. . . .", lcTime));
      this.tabHistoricalGraphMainController.refreshData();
    }
    else if (loCurrentTab == this.tabGroup)
    {
      // The refreshData is handled above when obtaining the loGroupID.
      if (tlIncludeGroupComboBox)
      {
        Misc.setStatusText(String.format("Refreshed the Group data @ %s. . . .", lcTime));
      }
    }

    Misc.setCursor(Cursor.DEFAULT);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Label getStatusMessage()
  {
    return (this.statusMessage);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public ProgressBar getProgressBar()
  {
    return (this.progressBar);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Label getProgressLabel()
  {
    return (this.progressLabel);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public ToolbarController getToolbarController()
  {
    return (this.toolbarMainController);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public MenuController getMenuController()
  {
    return (this.menuMainController);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  public void handle(final ActionEvent toEvent)
  {
    final Object loObject = toEvent.getSource();
    if (loObject == this.toolbarMainController.getGroupComboBox())
    {
      MainFormController.this.refreshAllComponents(false);
    }

  }
// ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

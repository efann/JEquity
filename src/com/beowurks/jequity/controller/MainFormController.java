/*
 * JEquity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.controller;

import com.beowurks.jequity.controller.table.TableFinancialController;
import com.beowurks.jequity.controller.table.TableGroupController;
import com.beowurks.jequity.controller.table.TableSymbolController;
import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.hibernate.warehouses.ThreadDownloadSymbolInfo;
import com.beowurks.jequity.dao.tableview.EnvironmentProperty;
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
  private TableGroupController tableGroupMainController;

  @FXML
  private TableSymbolController tableSymbolMainController;

  @FXML
  private TableFinancialController tableFinancialMainController;

  @FXML
  private HistoricalGraphController historicalGraphMainController;

  @FXML
  private ReportController reportMainController;
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
  private Tab tabDaily;

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

    // Refresh the Group Table also
    if (tlIncludeGroupComboBox)
    {
      this.tableGroupMainController.refreshData();
    }

    final Integer loGroupID = (tlIncludeGroupComboBox) ? this.toolbarMainController.refreshGroupComboBox() : this.toolbarMainController.getGroupComboBox().getSelectionModel().getSelectedItem().getKey();
    HibernateUtil.INSTANCE.setGroupID(loGroupID);

    final Tab loCurrentTab = this.tabPane.getSelectionModel().getSelectedItem();

    if (loCurrentTab == this.tabFinancial)
    {
      Misc.setStatusText(String.format("Refreshed the Financial grid @ %s. . . .", lcTime));

      this.tableFinancialMainController.refreshData();
    }
    else if (loCurrentTab == this.tabDaily)
    {
      Misc.setStatusText(String.format("Refreshed the Daily grid @ %s. . . .", lcTime));

      this.tableSymbolMainController.refreshData();
    }
    else if (loCurrentTab == this.tabReports)
    {
      Misc.setStatusText(String.format("Refreshed the Financial Report @ %s. . . .", lcTime));

      this.reportMainController.refreshReport(this.flShowPrintDialog);
      this.flShowPrintDialog = false;
    }
    else if (loCurrentTab == this.tabHistorical)
    {
      Misc.setStatusText(String.format("Refreshed the Historical data @ %s. . . .", lcTime));
      this.historicalGraphMainController.refreshData();
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

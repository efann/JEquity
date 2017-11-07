/*
 * J'Equity
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
import com.beowurks.jequity.dao.tableview.EnvironmentProperty;
import com.beowurks.jequity.utility.Misc;
import com.beowurks.jequity.view.jasperreports.JRViewerBase;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.stage.WindowEvent;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import org.controlsfx.control.StatusBar;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;

import javax.swing.SwingUtilities;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class MainFormController implements EventHandler<WindowEvent>
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

  //********************************************************************************

  @FXML
  private StatusBar statusBar;

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

  @FXML
  private SwingNode rptSwingNode;


  private final ObservableList<EnvironmentProperty> foDataList = FXCollections.observableArrayList();

  @FXML
  private TableView tblGroup;

  @FXML
  private TableColumn colID;

  @FXML
  private TableColumn colDescription;

  private JasperPrint foJPSummary;
  private JRViewerBase foJRViewerSummary;

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

    this.tabPane.getSelectionModel().selectedItemProperty().addListener(
        (toObservableValue, toPrevious, toCurrent) -> this.refreshAllComponents(false)
    );

    this.toolbarMainController.getGroupComboBox().getSelectionModel().selectedItemProperty().addListener(
        (toObservableValue, toPrevious, toCurrent) -> {
          if (toCurrent != null)
          {
            HibernateUtil.INSTANCE.setGroupID(toCurrent.getKey());
          }
          if ((toCurrent != null) && (toPrevious != null))
          {
            MainFormController.this.refreshAllComponents(false);
          }
        }
    );

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

    final Integer loGroupID = tlIncludeGroupComboBox ? this.toolbarMainController.refreshGroupComboBox() : this.toolbarMainController.getGroupComboBox().getSelectionModel().getSelectedItem().getKey();
    if (tlIncludeGroupComboBox)
    {
      this.tableGroupMainController.refreshData();
    }

    HibernateUtil.INSTANCE.setGroupID(loGroupID);
    final Tab loCurrentTab = this.tabPane.getSelectionModel().getSelectedItem();

    // Do not include the Group tab: it's handled above when obtaining the loGroupID.
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
      this.refreshReport();
    }

    Misc.setCursor(Cursor.DEFAULT);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void refreshReport()
  {
    SwingUtilities.invokeLater(this::generateSummary);

  }

  // -----------------------------------------------------------------------------
  private void generateSummary()
  {
    final Session loSession = HibernateUtil.INSTANCE.getSession();

    loSession.doWork(new Work()
    {
      @Override
      public void execute(final Connection toConnection) throws SQLException
      {
        try
        {
          final MainFormController loThis = MainFormController.this;

          final JasperReport loJasperReport = (JasperReport) JRLoader.loadObject(this.getClass().getResource("/com/beowurks/jequity/view/jasperreports/Summary.jasper"));

          final HashMap<String, Object> loHashMap = new HashMap<>();

          final HibernateUtil loHibernate = HibernateUtil.INSTANCE;

          loHashMap.put("parFinancialTable", loHibernate.getTableFinancial());
          loHashMap.put("parGroupTable", loHibernate.getTableGroup());
          loHashMap.put("parGroupID", loHibernate.getGroupID());

          loThis.foJPSummary = JasperFillManager.fillReport(loJasperReport, loHashMap, toConnection);

          if (loThis.foJRViewerSummary == null)
          {
            loThis.foJRViewerSummary = new JRViewerBase(loThis.foJPSummary);
            loThis.rptSwingNode.setContent(loThis.foJRViewerSummary);
          }
          else
          {
            loThis.foJRViewerSummary.loadReport(loThis.foJPSummary);
          }

        }
        catch (final JRException loErr)
        {
          loErr.printStackTrace();
        }
      }
    });

    loSession.close();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public StatusBar getStatusBar()
  {
    return (this.statusBar);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  public void handle(final WindowEvent toEvent)
  {
    final Object loSource = toEvent.getSource();
    if (loSource instanceof Tooltip)
    {
      final String lcText = ((Tooltip) loSource).getText();
      this.statusBar.setText(lcText);
    }

  }
// ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

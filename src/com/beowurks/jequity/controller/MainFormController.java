/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.controller;

import com.beowurks.jequity.dao.combobox.IntegerKeyItem;
import com.beowurks.jequity.dao.hibernate.GroupEntity;
import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.tableview.EnvironmentProperty;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import com.beowurks.jequity.view.jasperreports.JRViewerBase;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.ComboBox;
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
import org.hibernate.query.NativeQuery;

import javax.swing.SwingUtilities;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class MainFormController implements EventHandler<WindowEvent>
{
  // From https://stackoverflow.com/questions/23592148/javafx-nested-controller
  // The name of the controller variable of an included file is always the fx:id value with "Controller" added on to it.
  @FXML
  private MenuController menuMainController;
@FXML
  private ToolbarController toolbarMainController;

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
    this.menuMainController.getMenuRefresh().setOnAction(new EventHandler<ActionEvent>()
    {
      @Override
      public void handle(ActionEvent toActionEvent)
      {
        MainFormController.this.refreshAllComponents(true);
      }
    });

    this.toolbarMainController.getRefreshButton().setOnAction(new EventHandler<ActionEvent>()
    {
      @Override
      public void handle(ActionEvent toActionEvent)
      {
        MainFormController.this.refreshAllComponents(true);
      }
    });

    this.tabPane.getSelectionModel().selectedItemProperty().addListener(
        (toObservableValue, toPrevious, toCurrent) -> {
          if (toCurrent == MainFormController.this.tabReports)
          {
            MainFormController.this.refreshReport();
          }
        }
    );

    this.toolbarMainController.getGroupComboBox().getSelectionModel().selectedItemProperty().addListener(
        (toObservableValue, toPrevious, toCurrent) -> {
          if (toCurrent != null)
          {
            HibernateUtil.INSTANCE.setGroupID(toCurrent.getKey());
            MainFormController.this.refreshAllComponents(false);
          }
        }
    );

  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void refreshAllComponents(boolean tlIncludeGroupComboBox)
  {
    final DateFormat loDateFormat = new SimpleDateFormat("HH:mm:ss");
    final Calendar loCalender = Calendar.getInstance();

    Misc.setStatusText(String.format("Refreshing all of the data @ %s. . . .", loDateFormat.format(loCalender.getTime())));

    Misc.setCursor(Cursor.WAIT);

    if (Platform.isFxApplicationThread())
    {
      this.refreshAllComponentsFunction(tlIncludeGroupComboBox);
    }
    else
    {
      Platform.runLater(() ->
      {
        this.refreshAllComponentsFunction(tlIncludeGroupComboBox);
      });
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  // So that code will not be duplicated in refreshAllComponents.
  private void refreshAllComponentsFunction(boolean tlIncludeGroupComboBox)
  {
    final Integer loGroupID = tlIncludeGroupComboBox ? this.toolbarMainController.refreshGroupComboBox() : this.toolbarMainController.getGroupComboBox().getSelectionModel().getSelectedItem().getKey();
    HibernateUtil.INSTANCE.setGroupID(loGroupID);
    if (this.tabPane.getSelectionModel().getSelectedItem() == this.tabReports)
    {
      this.refreshReport();
    }

    Misc.setCursor(Cursor.DEFAULT);

  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void refreshReport()
  {
    SwingUtilities.invokeLater(() ->
    {
      this.generateSummary();
    });

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

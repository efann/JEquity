/*
 * JEquity
 * Copyright(c) 2008-2018, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.controller.tab;

import com.beowurks.jequity.dao.XMLTextReader;
import com.beowurks.jequity.dao.XMLTextWriter;
import com.beowurks.jequity.dao.combobox.StringKeyItem;
import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.hibernate.SymbolEntity;
import com.beowurks.jequity.dao.tableview.GroupProperty;
import com.beowurks.jequity.utility.AppProperties;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.w3c.dom.Node;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class TabHistoricalGraphController
{
  @FXML
  private ComboBox<StringKeyItem> cboStocks;

  @FXML
  private DatePicker txtStart;

  @FXML
  private DatePicker txtEnd;

  @FXML
  private CheckBox chkUseToday;

  @FXML
  private Button btnAnalyze;

  @FXML
  private Label lblTitleMessage;

  @FXML
  private LineChart<Date, Number> chtLineChart;

  private final ObservableList<GroupProperty> foDataList = FXCollections.observableArrayList();

  private XYChart.Series[] faXYDataSeries;


  private String fcCurrentDescription = "";
  private String fcCurrentSymbol = "";
  private String fcCurrentXML = "";

  // ---------------------------------------------------------------------------------------------------------------------
  private void analyzeData()
  {
    this.writeXML();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // From https://stackoverflow.com/questions/34785417/javafx-fxml-controller-constructor-vs-initialize-method
  @FXML
  public void initialize()
  {
    this.setupXYDataSeries();
    this.setupChart();

    this.setupListeners();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public ComboBox<StringKeyItem> getComboBox()
  {
    return (this.cboStocks);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupListeners()
  {
    this.btnAnalyze.setOnAction(toActionEvent -> TabHistoricalGraphController.this.analyzeData());
    this.chkUseToday.setOnAction(toActionEvent -> TabHistoricalGraphController.this.updateEndDate(null));
    this.cboStocks.setOnAction(toActionEvent -> TabHistoricalGraphController.this.updateOnComboBoxSelect());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupChart()
  {
    for (final XYChart.Series loSeries : this.faXYDataSeries)
    {
      this.chtLineChart.getData().add(loSeries);
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupXYDataSeries()
  {
    this.faXYDataSeries = new XYChart.Series[5];

    this.faXYDataSeries[0] = new XYChart.Series();
    this.faXYDataSeries[0].setName("Open");
    this.faXYDataSeries[1] = new XYChart.Series();
    this.faXYDataSeries[1].setName("High");
    this.faXYDataSeries[2] = new XYChart.Series();
    this.faXYDataSeries[2].setName("Low");
    this.faXYDataSeries[3] = new XYChart.Series();
    this.faXYDataSeries[3].setName("Close");
    this.faXYDataSeries[4] = new XYChart.Series();
    this.faXYDataSeries[4].setName("Adj Close");
  }

  // ---------------------------------------------------------------------------------------------------------------------
  synchronized public StringKeyItem refreshData()
  {
    final ComboBox<StringKeyItem> loCombo = this.cboStocks;
    // Save the onAction event then set to null so nothing happens when rebuilding the list.
    final EventHandler<ActionEvent> loActionHandler = loCombo.getOnAction();
    loCombo.setOnAction(null);

    final StringKeyItem loSelectItem = loCombo.getSelectionModel().getSelectedItem();

    final HibernateUtil loHibernate = HibernateUtil.INSTANCE;
    final Session loSession = loHibernate.getSession();

    // Needs to be * (all fields) as we're populating the SymbolEntity.class.
    final NativeQuery loQuery = loSession.createNativeQuery(String.format("SELECT * FROM %s WHERE (description <> '') ORDER BY description", loHibernate.getTableSymbol()), SymbolEntity.class);

    final ObservableList<StringKeyItem> loStringKeys = FXCollections.observableArrayList();

    StringKeyItem loInitKeyItem = null;
    final List<SymbolEntity> loList = loQuery.list();

    for (final SymbolEntity loRow : loList)
    {
      final String lcID = loRow.getSymbol();
      final StringKeyItem loKeyItem = new StringKeyItem(lcID, loRow.getDescription());
      loStringKeys.add(loKeyItem);
      if (loInitKeyItem == null)
      {
        loInitKeyItem = loKeyItem;
      }
    }
    loSession.close();

    loCombo.getItems().clear();
    loCombo.setItems(loStringKeys);

    // Reset before selection occurs so that the relevant select actions take place.
    loCombo.setOnAction(loActionHandler);

    if (loSelectItem != null)
    {
      loCombo.setValue(loSelectItem);
      loCombo.getSelectionModel().select(loSelectItem);
    }
    else
    {
      loCombo.setValue(loInitKeyItem);
      loCombo.getSelectionModel().select(loInitKeyItem);
    }

    return (loInitKeyItem);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void updateEndDate(final LocalDate toDate)
  {
    final boolean llChecked = this.chkUseToday.isSelected();

    Misc.setEditableForDatePicker(this.txtEnd, !llChecked);

    if (llChecked)
    {
      this.txtEnd.setValue(LocalDate.now());
    }
    else if (toDate != null)
    {
      this.txtEnd.setValue(toDate);
    }
  }

  // -----------------------------------------------------------------------------
  private void updateOnComboBoxSelect()
  {
    if (!this.readXML())
    {
      this.btnAnalyze.setDisable(true);
      final StringKeyItem loItem = this.cboStocks.getSelectionModel().getSelectedItem();

      this.setTitleMessage(String.format("Unable to obtain the setup data for %s (%)", loItem.getDescription(), loItem.getKey()), true);

      return;
    }

    this.setTitleMessage(String.format("%s (%s)", this.fcCurrentDescription, this.fcCurrentSymbol), false);
    this.btnAnalyze.setDisable(false);
    this.updateComponentsFromXML();
  }

  // -----------------------------------------------------------------------------
  private void updateComponentsFromXML()
  {
    final XMLTextReader loReader = XMLTextReader.INSTANCE;
    if (!this.fcCurrentXML.isEmpty() && loReader.initializeXMLDocument(this.fcCurrentXML, false))
    {
      final boolean llUseToday = loReader.getBoolean(Constants.XML_SYMBOL_USE_TODAY, true);

      this.chkUseToday.setSelected(llUseToday);
      // I've decided to store the dates as string rather than longs as it's easier to read the XML with human eyes.
      final String lcStart = loReader.getString(Constants.XML_SYMBOL_START_DATE, LocalDate.now().toString());
      this.txtStart.setValue(LocalDate.parse(lcStart));

      final String lcEnd = loReader.getString(Constants.XML_SYMBOL_END_DATE, LocalDate.now().toString());
      this.updateEndDate(LocalDate.parse(lcEnd));
      return;
    }

    this.chkUseToday.setSelected(true);
    this.txtStart.setValue(AppProperties.INSTANCE.getHistoricalStartDefault().toLocalDate());
    this.updateEndDate(null);
  }

  // -----------------------------------------------------------------------------
  private void writeXML()
  {
    final XMLTextWriter loTextWriter = XMLTextWriter.INSTANCE;
    loTextWriter.initializeXMLDocument();
    loTextWriter.createRootNode(Constants.XML_SYMBOL_ROOT_LABEL, null);

    final Node loRecord = loTextWriter.appendNodeToRoot(Constants.XML_SYMBOL_RECORD_LABEL, (String) null, null);

    loTextWriter.appendToNode(loRecord, Constants.XML_SYMBOL_USE_TODAY, this.chkUseToday.isSelected() ? Constants.XML_TRUE : Constants.XML_FALSE, null);
    // I've decided to store the dates as string rather than longs as it's easier to read the XML with human eyes.
    loTextWriter.appendToNode(loRecord, Constants.XML_SYMBOL_START_DATE, this.txtStart.getValue().toString(), null);
    loTextWriter.appendToNode(loRecord, Constants.XML_SYMBOL_END_DATE, this.txtEnd.getValue().toString(), null);

    final String lcXML = loTextWriter.generateXMLString(2);

    final HibernateUtil loHibernate = HibernateUtil.INSTANCE;
    final Session loSession = HibernateUtil.INSTANCE.getSession();

    final String lcSQL = String.format("UPDATE %s SET historicalinfo = :historicalinfo WHERE symbol = :symbol", loHibernate.getTableSymbol());

    try
    {
      final Transaction loTransaction = loSession.beginTransaction();

      final NativeQuery loQuery = loSession.createNativeQuery(lcSQL)
          .setParameter("symbol", this.fcCurrentSymbol)
          .setParameter("historicalinfo", lcXML);

      loQuery.executeUpdate();

      loTransaction.commit();
      loSession.clear();
    }
    catch (final Exception ignore)
    {
    }

  }

  // -----------------------------------------------------------------------------
  private boolean readXML()
  {
    final HibernateUtil loHibernate = HibernateUtil.INSTANCE;
    final Session loSession = HibernateUtil.INSTANCE.getSession();

    final String lcKey = this.cboStocks.getSelectionModel().getSelectedItem().getKey();
    final String lcDescription = this.cboStocks.getSelectionModel().getSelectedItem().getDescription();
    // There should only be one symbol. I'm not using LIMIT as there could be differences in SQL syntax between
    // the database servers.
    final String lcSQL = String.format("SELECT symbol, historicalinfo FROM %s WHERE symbol = :symbol", loHibernate.getTableSymbol());
    final NativeQuery loQuery = loSession.createNativeQuery(lcSQL)
        .setParameter("symbol", lcKey);

    final List<Object[]> loList = loQuery.list();

    // Could use the getKey() above to set the symbol; however, this just ensures that the record exists
    // in the table.
    for (final Object[] loRow : loList)
    {
      this.fcCurrentSymbol = loRow[0].toString();
      this.fcCurrentXML = loRow[1].toString();
      this.fcCurrentDescription = lcDescription;

      return (true);
    }

    return (false);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setTitleMessage(final String tcTitle, final boolean tlError)
  {
    this.lblTitleMessage.setText(tcTitle);
    this.lblTitleMessage.setTextFill(tlError ? Color.RED : Color.BLACK);
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

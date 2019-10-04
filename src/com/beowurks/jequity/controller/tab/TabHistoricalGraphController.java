/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
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
import com.beowurks.jequity.dao.hibernate.threads.ThreadDownloadHistorical;
import com.beowurks.jequity.dao.tableview.GroupProperty;
import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.AppProperties;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import com.beowurks.jequity.view.textfield.DatePickerPlus;
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
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.controlsfx.control.HyperlinkLabel;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.w3c.dom.Node;

import java.time.LocalDate;
import java.util.List;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class TabHistoricalGraphController implements EventHandler<ActionEvent>
{
  private final static int ALPHA_KEY_MASK_LIMIT = 4;
  private final static String ALPHA_KEY_STRING = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=%s&outputsize=%s&apikey=%s";

  // You can test with this one.
  private final static String ALPHA_DEMO_STRING = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=MSFT&outputsize=full&apikey=demo";

  @FXML
  private ComboBox<StringKeyItem> cboStocks;

  @FXML
  private DatePickerPlus txtStart;

  @FXML
  private DatePickerPlus txtEnd;

  @FXML
  private CheckBox chkUseToday;

  @FXML
  private CheckBox chkShowOpen;
  @FXML
  private CheckBox chkShowHigh;
  @FXML
  private CheckBox chkShowLow;
  @FXML
  private CheckBox chkShowClose;
  @FXML
  private CheckBox chkShowAdjClose;

  @FXML
  private Button btnAnalyze;

  @FXML
  private Label lblTitleMessage;

  @FXML
  private HyperlinkLabel lnkAlphaVantageMessage;

  @FXML
  private LineChart chtLineChart;

  private final ObservableList<GroupProperty> foDataList = FXCollections.observableArrayList();

  private XYChart.Series<String, Double>[] faXYDataSeries;

  private String fcCurrentDescription = "";
  private String fcCurrentSymbol = "";
  private String fcCurrentXML = "";

  // ---------------------------------------------------------------------------------------------------------------------
  private void analyzeData()
  {
    if (!this.isAlphaVantageKeySet())
    {
      Misc.errorMessage("Your Alpha Vantage key has not been set yet.");
      return;
    }

    this.writeXML();

    ThreadDownloadHistorical.INSTANCE.start(true, this);
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
  public LineChart getChart()
  {
    return (this.chtLineChart);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getSymbol()
  {
    return (this.fcCurrentSymbol);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getAlphaVantageURL()
  {
    return (!Main.isDevelopmentEnvironment() ?
        String.format(TabHistoricalGraphController.ALPHA_KEY_STRING, this.getSymbol(), "full", this.getAlphaVantageKey()) :
        TabHistoricalGraphController.ALPHA_DEMO_STRING);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public LocalDate getEndDate()
  {
    return (this.txtEnd.getValue());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public LocalDate getStartDate()
  {
    return (this.txtStart.getValue());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupListeners()
  {
    this.btnAnalyze.setOnAction(this);
    this.chkUseToday.setOnAction(this);
    this.cboStocks.setOnAction(this);

    this.chkShowOpen.setOnAction(this);
    this.chkShowHigh.setOnAction(this);
    this.chkShowLow.setOnAction(this);
    this.chkShowClose.setOnAction(this);
    this.chkShowAdjClose.setOnAction(this);

    this.lnkAlphaVantageMessage.setOnAction(this);
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
  // Do not call when initializing the tab: AppProperties.INSTANCE has not yet been set.
  private void refreshLabels()
  {
    final StringBuilder lcMessage = new StringBuilder();

    if (this.isAlphaVantageKeySet())
    {
      lcMessage.append(String.format("Your Alpha Vantage key, %s, is set for downloading historical data. For more information, visit [https://www.alphavantage.co/documentation/].", this.maskKey(this.getAlphaVantageKey())));
    }
    else
    {
      lcMessage.append(String.format("Visit [https://www.alphavantage.co/] and get your free API key for downloading historical data. Then save key under %s.", Misc.isMacintosh() ? "Preferences... | Stock Data" : "Tools | Options... | Stock Data"));
    }

    this.lnkAlphaVantageMessage.setText(lcMessage.toString());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private boolean isAlphaVantageKeySet()
  {
    return (!this.getAlphaVantageKey().isEmpty());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private String maskKey(final String tcKey)
  {
    final StringBuilder loMasked = new StringBuilder();

    final int lnLength = tcKey.length();
    if (lnLength < TabHistoricalGraphController.ALPHA_KEY_MASK_LIMIT)
    {
      for (int i = 0; i < TabHistoricalGraphController.ALPHA_KEY_MASK_LIMIT; ++i)
      {
        loMasked.append("*");
      }

      return (loMasked.toString());
    }

    final char[] taChar = tcKey.toCharArray();
    for (int i = 0; i < lnLength; ++i)
    {
      loMasked.append(i < (lnLength - TabHistoricalGraphController.ALPHA_KEY_MASK_LIMIT) ? '*' : taChar[i]);
    }

    return (loMasked.toString());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getAlphaVantageKey()
  {
    return (AppProperties.INSTANCE.getAlphaVantageAPIKey());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  synchronized public StringKeyItem refreshData()
  {
    // Just in case, the key has been modified in the Options dialog.
    this.refreshLabels();

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
      final String lcID = loRow.getSymbol().trim();
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

      this.chkShowOpen.setSelected(true);
      this.chkShowHigh.setSelected(true);
      this.chkShowLow.setSelected(true);
      this.chkShowClose.setSelected(true);
      this.chkShowAdjClose.setSelected(true);

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
      this.chkUseToday.setSelected(loReader.getBoolean(Constants.XML_SYMBOL_USE_TODAY, true));
      // I've decided to store the dates as string rather than longs as it's easier to read the XML with human eyes.
      final String lcStart = loReader.getString(Constants.XML_SYMBOL_START_DATE, LocalDate.now().toString());
      this.txtStart.setValue(LocalDate.parse(lcStart));

      this.chkShowOpen.setSelected(loReader.getBoolean(Constants.XML_SYMBOL_SHOW_OPEN, true));
      this.chkShowHigh.setSelected(loReader.getBoolean(Constants.XML_SYMBOL_SHOW_HIGH, true));
      this.chkShowLow.setSelected(loReader.getBoolean(Constants.XML_SYMBOL_SHOW_LOW, true));
      this.chkShowClose.setSelected(loReader.getBoolean(Constants.XML_SYMBOL_SHOW_CLOSE, true));
      this.chkShowAdjClose.setSelected(loReader.getBoolean(Constants.XML_SYMBOL_SHOW_ADJCLOSE, true));

      final String lcEnd = loReader.getString(Constants.XML_SYMBOL_END_DATE, LocalDate.now().toString());
      this.updateEndDate(LocalDate.parse(lcEnd));
      return;
    }

    this.chkUseToday.setSelected(true);
    this.txtStart.setValue(AppProperties.INSTANCE.getHistoricalStartDefault().toLocalDate());
    this.updateEndDate(null);
  }

  // ---------------------------------------------------------------------------------------------------------------------
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

    loTextWriter.appendToNode(loRecord, Constants.XML_SYMBOL_SHOW_OPEN, this.chkShowOpen.isSelected() ? Constants.XML_TRUE : Constants.XML_FALSE, null);
    loTextWriter.appendToNode(loRecord, Constants.XML_SYMBOL_SHOW_HIGH, this.chkShowHigh.isSelected() ? Constants.XML_TRUE : Constants.XML_FALSE, null);
    loTextWriter.appendToNode(loRecord, Constants.XML_SYMBOL_SHOW_LOW, this.chkShowLow.isSelected() ? Constants.XML_TRUE : Constants.XML_FALSE, null);
    loTextWriter.appendToNode(loRecord, Constants.XML_SYMBOL_SHOW_CLOSE, this.chkShowClose.isSelected() ? Constants.XML_TRUE : Constants.XML_FALSE, null);
    loTextWriter.appendToNode(loRecord, Constants.XML_SYMBOL_SHOW_ADJCLOSE, this.chkShowAdjClose.isSelected() ? Constants.XML_TRUE : Constants.XML_FALSE, null);

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
      this.fcCurrentSymbol = loRow[0].toString().trim();
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
  // ---------------------------------------------------------------------------------------------------------------------
  // Implements methods for EventHandler
  @Override
  public void handle(final ActionEvent toEvent)
  {
    final Object loSource = toEvent.getSource();
    if (loSource.equals(this.btnAnalyze))
    {
      this.analyzeData();
    }
    else if (loSource.equals(this.chkUseToday))
    {
      this.updateEndDate(null);
    }
    else if (loSource.equals(this.chkShowOpen))
    {
      // From https://stackoverflow.com/questions/39507491/how-to-remove-symbol-markers-from-only-selected-series-in-javafx-charts
      for (final Object loTemp : this.chtLineChart.getData())
      {
        final XYChart.Series<Number, Number> series = (XYChart.Series<Number, Number>) loTemp;
        if (!series.getName().equals("Open")) //if Name is "blue" then continue
        {
          // continue;
        }

        //for all series, take date, each data has Node (symbol) for representing point
        for (final XYChart.Data<Number, Number> data : series.getData())
        {
          // this node is StackPane
          final StackPane stackPane = (StackPane) data.getNode();
          stackPane.setVisible(false);
        }
      }
    }
    else if (loSource.equals(this.chkShowHigh))
    {
    }
    else if (loSource.equals(this.chkShowLow))
    {
    }
    else if (loSource.equals(this.chkShowClose))
    {
    }
    else if (loSource.equals(this.chkShowAdjClose))
    {
    }
    else if (loSource.equals(this.cboStocks))
    {
      this.updateOnComboBoxSelect();
    }
    else if (loSource instanceof Hyperlink)
    {
      final Hyperlink loHyperLink = (Hyperlink) loSource;

      final String lcURL = loHyperLink.getText();
      Main.getMainHostServices().showDocument(lcURL);
    }

  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

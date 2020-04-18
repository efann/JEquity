/*
 * JEquity
 * Copyright(c) 2008-2020, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.controller.tab;

import com.beowurks.jequity.dao.XMLTextReader;
import com.beowurks.jequity.dao.XMLTextWriter;
import com.beowurks.jequity.dao.combobox.DoubleKeyItem;
import com.beowurks.jequity.dao.combobox.StringKeyItem;
import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.hibernate.SymbolEntity;
import com.beowurks.jequity.dao.hibernate.threads.DataExtraValue;
import com.beowurks.jequity.dao.hibernate.threads.JSONDataElements;
import com.beowurks.jequity.dao.hibernate.threads.ThreadDownloadHistorical;
import com.beowurks.jequity.dao.tableview.GroupProperty;
import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.AppProperties;
import com.beowurks.jequity.utility.Calculations;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import com.beowurks.jequity.view.checkbox.CheckBoxPlus;
import com.beowurks.jequity.view.combobox.ComboBoxDoubleKey;
import com.beowurks.jequity.view.combobox.ComboBoxIntegerKey;
import com.beowurks.jequity.view.combobox.ComboBoxStringKey;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.controlsfx.control.HyperlinkLabel;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.w3c.dom.Node;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.List;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class TabHistoricalGraphController implements EventHandler<ActionEvent>
{
  @FXML
  private ComboBoxStringKey cboStocks;

  @FXML
  private ComboBoxIntegerKey cboRanges;

  @FXML
  private ComboBoxDoubleKey cboSmoothing;

  @FXML
  private HBox hboxSeriesVisibility;

  @FXML
  private Button btnAnalyze;

  @FXML
  private Label lblTitleMessage;

  @FXML
  private HyperlinkLabel lnkAlphaVantageMessage;

  @FXML
  private LineChart chtLineChartData;

  @FXML
  private LineChart chtLineChartTrends;

  private final ObservableList<GroupProperty> foDataList = FXCollections.observableArrayList();

  private XYChart.Series<String, Double>[] faXYDataSeriesData;
  private XYChart.Series<String, Double>[] faXYDataSeriesTrends;

  private final DateTimeFormatter foMonthTrackerDateFormat = DateTimeFormatter.ofPattern("MMMMyyyy");

  private final DateTimeFormatter foXAxisFormat = DateTimeFormatter.ofPattern("MM-dd-yy");

  private String fcCurrentDescription = "";
  private String fcCurrentSymbol = "";
  private String fcCurrentXML = "";

  private CheckBoxPlus[] faSeriesVisibility;
  private String[] faSeriesColors;

  // ---------------------------------------------------------------------------------------------------------------------
  // From https://stackoverflow.com/questions/34785417/javafx-fxml-controller-constructor-vs-initialize-method
  @FXML
  public void initialize()
  {
    this.setupXYDataSeries();
    this.setupCheckboxes();
    this.setupComboBoxes();
    this.initializeCharts();

    this.setupListeners();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public LineChart getChartData()
  {
    return (this.chtLineChartData);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public LineChart getChartTrends()
  {
    return (this.chtLineChartTrends);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public CheckBoxPlus[] getCheckBoxesForSeriesVisibility()
  {
    return (this.faSeriesVisibility);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getSymbol()
  {
    return (this.fcCurrentSymbol);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public double getSmoothingValue()
  {
    final DoubleKeyItem loItem = (DoubleKeyItem) this.cboSmoothing.getSelectionModel().getSelectedItem();

    return ((loItem != null) ? loItem.getKey() : 0.0);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public XYChart.Series<String, Double>[] getDataSeriesData()
  {
    return (this.faXYDataSeriesData);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public XYChart.Series<String, Double>[] getDataSeriesTrends()
  {
    return (this.faXYDataSeriesTrends);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getAlphaVantageURL()
  {
    return (!Main.isDevelopmentEnvironment() ?
      String.format(Constants.ALPHA_KEY_STRING, this.getSymbol(), "full", this.getAlphaVantageKey()) :
      Constants.ALPHA_DEMO_STRING);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public HistoricalDateInfo getHistoricalDateInfo()
  {
    int lnIndex = this.cboRanges.getSelectedIndex();
    if (lnIndex < 0)
    {
      lnIndex = 0;
    }

    final int lnDays = Constants.HISTORICAL_RANGE[lnIndex].getKey();

    final LocalDate loCurrent = LocalDate.now();
    final HistoricalDateInfo loDateInfo = new HistoricalDateInfo();
    loDateInfo.foLocalEndDateData = loCurrent;

    LocalDate loStart = loCurrent.minusDays(lnDays);

    loDateInfo.fnDisplaySequenceData = Constants.HISTORICAL_EVERY_DAY;
    loDateInfo.fnDisplaySequenceTrends = Constants.HISTORICAL_EVERY_DAY;

    loDateInfo.foLocalStartDate = loStart;

    // Get next start of the week or Monday.
    if ((lnDays > Constants.HISTORICAL_1_YEAR) && (lnDays <= Constants.HISTORICAL_5_YEARS))
    {
      while (loStart.getDayOfWeek() != DayOfWeek.MONDAY)
      {
        loStart = loStart.plusDays(1);
      }

      loDateInfo.fnDisplaySequenceData = Constants.HISTORICAL_EVERY_WEEK;
      loDateInfo.foLocalStartDate = loStart;
    }

    // Get next start of the month.
    if (lnDays > Constants.HISTORICAL_5_YEARS)
    {
      while (loStart.getDayOfMonth() != 1)
      {
        loStart = loStart.plusDays(1);
      }

      loDateInfo.fnDisplaySequenceData = Constants.HISTORICAL_EVERY_MONTH;
      loDateInfo.foLocalStartDate = loStart;
    }

    final long lnDaysDiffence = Math.min(ChronoUnit.DAYS.between(loStart, loCurrent), Constants.HISTORICAL_5_YEARS);

    if ((lnDaysDiffence > Constants.HISTORICAL_1_YEAR) && (lnDaysDiffence <= Constants.HISTORICAL_5_YEARS))
    {
      loDateInfo.fnDisplaySequenceTrends = Constants.HISTORICAL_EVERY_WEEK;
    }

    if (lnDays > Constants.HISTORICAL_5_YEARS)
    {
      loDateInfo.fnDisplaySequenceTrends = Constants.HISTORICAL_EVERY_MONTH;
    }

    loDateInfo.foLocalEndDateTrends = loCurrent.plusDays(lnDaysDiffence);


    return (loDateInfo);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void redrawCharts(final boolean tlRecalculate)
  {
    this.redrawChartData();
    this.redrawChartTrends(tlRecalculate);

    this.updateChartTooltips();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void redrawChartData()
  {
    // If you don't setAnimated(false), with an empty chart, you will receive an
    //   Exception in thread "JavaFX Application Thread" java.lang.IllegalArgumentException: Duplicate series added
    // Solution in https://stackoverflow.com/questions/32151435/javafx-duplicate-series-added
    //
    // By the way, you could create a LineChartPlus which sets animate to false. However, I had problems with FXML files
    // as I couldn't create default constructor and setAnimated is called in different spots of JavaFX code. So I just
    // set when needed.
    this.chtLineChartData.setAnimated(false);

    final StringBuilder loStyles = new StringBuilder();
    // Seems awkward to remove data and then re-add, but it works. There's not a setVisible()
    // for each series.
    final ObservableList<XYChart.Series> loData = this.chtLineChartData.getData();

    loData.clear();

    final int lnCheckBoxesLength = this.faSeriesVisibility.length;
    for (int i = 0; i < lnCheckBoxesLength; ++i)
    {
      if (this.faSeriesVisibility[i].isSelected())
      {
        loData.add(this.faXYDataSeriesData[i]);
        final int lnSize = loData.size();
        loStyles.append(this.getChartColorString(lnSize, this.faSeriesColors[i]));
      }
    }

    if (loStyles.length() > 0)
    {
      this.chtLineChartData.setStyle(loStyles.toString());
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void redrawChartTrends(final boolean tlRecalculate)
  {
    // If you don't setAnimated(false), with an empty chart, you will receive an
    //   Exception in thread "JavaFX Application Thread" java.lang.IllegalArgumentException: Duplicate series added
    // Solution in https://stackoverflow.com/questions/32151435/javafx-duplicate-series-added
    //
    // By the way, you could create a LineChartPlus which sets animate to false. However, I had problems with FXML files
    // as I couldn't create default constructor and setAnimated is called in different spots of JavaFX code. So I just
    // set when needed.
    this.chtLineChartTrends.setAnimated(false);

    if (tlRecalculate)
    {
      final ThreadDownloadHistorical loThreadHistorical = ThreadDownloadHistorical.INSTANCE;
      final ArrayList<JSONDataElements> loJSONDateRangeList = loThreadHistorical.getJSONDateRangeList();

      // The chart trends need to be re-calculated and re-drawn each time. In other words,
      // chart trends are dynamic; chart data is not.
      Calculations.INSTANCE.refreshAll(loThreadHistorical);

      // Recalculate the series just in case.
      final XYChart.Series<String, Double> loRegressionSeries = this.faXYDataSeriesTrends[Constants.HISTORICAL_TRENDS_REGRESS];
      final int lnSizeReg = loRegressionSeries.getData().size();
      for (int i = 0; i < lnSizeReg; ++i)
      {
        final XYChart.Data<String, Double> loData = loRegressionSeries.getData().get(i);
        final Object loExtra = loData.getExtraValue();
        if (loExtra instanceof DataExtraValue)
        {
          final int lnDay = ((DataExtraValue) loExtra).getWeekDays();
          loData.setYValue(Calculations.INSTANCE.getYValueRegression(lnDay));
        }
      }

      final XYChart.Series<String, Double> loFFTDataSeries = this.faXYDataSeriesTrends[Constants.HISTORICAL_TRENDS_FFT];
      final int lnSizeFFT = loFFTDataSeries.getData().size();
      for (int i = 0; i < lnSizeFFT; ++i)
      {
        final XYChart.Data<String, Double> loData = loFFTDataSeries.getData().get(i);
        final Object loExtra = loData.getExtraValue();
        if (loExtra instanceof DataExtraValue)
        {
          final int lnDateIndex = ((DataExtraValue) loExtra).getWeekDays();
          final Double loFFT = Calculations.INSTANCE.getYValueFFT(lnDateIndex);
          if (loFFT != null)
          {
            loData.setYValue(loFFT);
          }
        }
      }

      final XYChart.Series<String, Double> loRawDataAvgSeries = this.faXYDataSeriesTrends[Constants.HISTORICAL_TRENDS_RAW_DATA_AVG];
      final int lnSizeRawAvg = loRawDataAvgSeries.getData().size();
      for (int i = 0; i < lnSizeRawAvg; ++i)
      {
        final XYChart.Data<String, Double> loData = loRawDataAvgSeries.getData().get(i);
        final Object loExtra = loData.getExtraValue();
        if (loExtra instanceof DataExtraValue)
        {
          final int lnDateIndex = ((DataExtraValue) loExtra).getWeekDays();
          final Double loAvg = Calculations.INSTANCE.getYValueAverage(lnDateIndex);
          if (loAvg != null)
          {
            loData.setYValue(loAvg);
          }
        }
      }
    }

    // Seems awkward to remove data and then re-add, but it works. There's not a setVisible()
    // for each series.
    final ObservableList<XYChart.Series> loData = this.chtLineChartTrends.getData();
    loData.clear();

    for (final XYChart.Series<String, Double> laXYDataSeriesTrend : this.faXYDataSeriesTrends)
    {
      loData.add(laXYDataSeriesTrend);
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void recreateChartData()
  {
    final LineChart loChart = this.getChartData();
    final XYChart.Series<String, Double>[] laDataSeries = this.getDataSeriesData();
    final int lnDataSeriesTotal = laDataSeries.length;

    final StringBuilder loTrackDatesUsed = new StringBuilder(",");
    final HistoricalDateInfo loDateInfo = this.getHistoricalDateInfo();

    // From https://stackoverflow.com/questions/28850211/performance-issue-with-javafx-linechart-with-65000-data-points
    final ArrayList<XYChart.Data<String, Double>>[] laPlotPoints = new ArrayList[lnDataSeriesTotal];
    for (int i = 0; i < lnDataSeriesTotal; i++)
    {
      laPlotPoints[i] = new ArrayList<>();
    }

    final ArrayList<JSONDataElements> loJSONDateRangeList = ThreadDownloadHistorical.INSTANCE.getJSONDateRangeList();
    // By the way, I wanted to use the techniques found here;
    // From https://stackoverflow.com/questions/46987823/javafx-line-chart-with-date-axis
    // However, I was having round off problems where 1,566,566,566,566 was converted to 1,500,000,000,000.
    for (final JSONDataElements loElement : loJSONDateRangeList)
    {
      boolean llOkay = false;

      if (loDateInfo.fnDisplaySequenceData == Constants.HISTORICAL_EVERY_DAY)
      {
        llOkay = true;
      }
      else if (loDateInfo.fnDisplaySequenceData == Constants.HISTORICAL_EVERY_WEEK)
      {
        final String lcMarker = String.format("%2d%d", loElement.foDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR), loElement.foDate.getYear());

        // If not found, then use and add to the loTrackDatesUsed so that
        // no more days of that particular week will be used.
        llOkay = (loTrackDatesUsed.indexOf(lcMarker) == -1);
        if (llOkay)
        {
          loTrackDatesUsed.append(lcMarker).append(",");
        }

      }
      else if (loDateInfo.fnDisplaySequenceData == Constants.HISTORICAL_EVERY_MONTH)
      {
        final String lcMarker = loElement.foDate.format(this.foMonthTrackerDateFormat);
        // If not found, then use and add to the loTrackDatesUsed so that
        // no more days of that particular month will be used.
        llOkay = (loTrackDatesUsed.indexOf(lcMarker) == -1);
        if (llOkay)
        {
          loTrackDatesUsed.append(lcMarker).append(",");
        }
      }

      if (!llOkay)
      {
        continue;
      }

      final String lcDate = this.foXAxisFormat.format(loElement.foDate);
      // Now add the elements to the particular line.
      final int lnCount = loElement.faNumbers.length;
      for (int i = 0; i < lnCount; ++i)
      {
        final XYChart.Data loData = new XYChart.Data<>(lcDate, loElement.faNumbers[i]);

        laPlotPoints[i].add(loData);
      }
    }

    this.resetDataSeries(loChart, laDataSeries, laPlotPoints);

    Misc.setStatusText(0.0);

  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void recreateChartTrends()
  {
    final LineChart loChart = this.getChartTrends();

    final XYChart.Series<String, Double>[] laDataSeries = this.getDataSeriesTrends();
    final int lnDataSeriesTotal = laDataSeries.length;

    final StringBuilder loTrackDatesUsed = new StringBuilder(",");
    final HistoricalDateInfo loDateInfo = this.getHistoricalDateInfo();

    // From https://stackoverflow.com/questions/28850211/performance-issue-with-javafx-linechart-with-65000-data-points
    final ArrayList<XYChart.Data<String, Double>>[] laPlotPoints = new ArrayList[lnDataSeriesTotal];
    for (int i = 0; i < lnDataSeriesTotal; i++)
    {
      laPlotPoints[i] = new ArrayList<>();
    }

    final ThreadDownloadHistorical loThreadHistorical = ThreadDownloadHistorical.INSTANCE;

    Calculations.INSTANCE.refreshAll(loThreadHistorical);

    final ArrayList<JSONDataElements> loJSONDateRangeList = loThreadHistorical.getJSONDateRangeList();

    // Start with the 1st date of the data set, not the start date of loDateInfo. This will ensure matching
    // the displayed start date of ChartData.
    LocalDate loTrackDate = loJSONDateRangeList.get(0).foDate;
    final LocalDate loEnd = loDateInfo.foLocalEndDateTrends;

    final LocalDate loLastRawDataDate = loJSONDateRangeList.get(loJSONDateRangeList.size() - 1).foDate;

    // lnCountWeekDays are 0-based as are the Calculations.
    int lnCountWeekDays = -1;

    while (!loTrackDate.isAfter(loEnd))
    {
      boolean llOkay = false;

      // Continue only if not a weekend date.
      if ((loTrackDate.getDayOfWeek() != DayOfWeek.SATURDAY) && (loTrackDate.getDayOfWeek() != DayOfWeek.SUNDAY))
      {
        ++lnCountWeekDays;

        if (loDateInfo.fnDisplaySequenceTrends == Constants.HISTORICAL_EVERY_DAY)
        {
          llOkay = true;
        }
        else if (loDateInfo.fnDisplaySequenceTrends == Constants.HISTORICAL_EVERY_WEEK)
        {
          final String lcMarker = String.format("%2d%d", loTrackDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR), loTrackDate.getYear());

          // If not found, then use and add to the loTrackDatesUsed so that
          // no more days of that particular week will be used.
          llOkay = (loTrackDatesUsed.indexOf(lcMarker) == -1);
          if (llOkay)
          {
            loTrackDatesUsed.append(lcMarker).append(",");
          }
        }
        else if (loDateInfo.fnDisplaySequenceTrends == Constants.HISTORICAL_EVERY_MONTH)
        {
          final String lcMarker = loTrackDate.format(this.foMonthTrackerDateFormat);
          // If not found, then use and add to the loTrackDatesUsed so that
          // no more days of that particular month will be used.
          llOkay = (loTrackDatesUsed.indexOf(lcMarker) == -1);
          if (llOkay)
          {
            loTrackDatesUsed.append(lcMarker).append(",");
          }
        }
      }

      if (llOkay)
      {
        final String lcDate = this.foXAxisFormat.format(loTrackDate);

        // By the way, you need separate DataExtraValue variables. Otherwise, if you change
        // a DataExtraValue variable anywhere in the below code, it changes in the other dataseries elements linked to it.
        // Duh.
        final DataExtraValue loExtraReg = new DataExtraValue(loTrackDate, lnCountWeekDays);

        // Now add the elements to the particular line.
        final XYChart.Data loDataReg = new XYChart.Data<>(lcDate, Calculations.INSTANCE.getYValueRegression(lnCountWeekDays));
        loDataReg.setExtraValue(loExtraReg);
        laPlotPoints[Constants.HISTORICAL_TRENDS_REGRESS].add(loDataReg);

        // Averages and FFT must match a date.
        // However, after the end date, FFT can match the Regression dates.
        final Integer lnDateIndex = this.getDateIndex(loJSONDateRangeList, loTrackDate);
        if (lnDateIndex != null)
        {
          final double lnAverage = Calculations.INSTANCE.getYValueAverage(lnDateIndex);
          // Needs to match the index for the average array, not the actual weekday number, as we're
          // accessing actual, not calculated data.
          final DataExtraValue loExtraAvg = new DataExtraValue(loTrackDate, lnDateIndex);

          final XYChart.Data loDataAvg = new XYChart.Data<>(lcDate, lnAverage);
          loDataAvg.setExtraValue(loExtraAvg);
          laPlotPoints[Constants.HISTORICAL_TRENDS_RAW_DATA_AVG].add(loDataAvg);
        }

        int lnFFTDateIndex = -1;
        if (lnDateIndex != null)
        {
          lnFFTDateIndex = lnDateIndex;
        }
        else if (loTrackDate.isAfter(loLastRawDataDate))
        {
          lnFFTDateIndex = lnCountWeekDays;
        }

        if (lnFFTDateIndex != -1)
        {
          // Needs to match the index for the average array, not the actual weekday number, as we're
          // accessing actual, not calculated data.
          final DataExtraValue loExtraFFT = new DataExtraValue(loTrackDate, lnFFTDateIndex);

          final XYChart.Data loDataFFT = new XYChart.Data<>(lcDate, Calculations.INSTANCE.getYValueFFT(lnFFTDateIndex));
          loDataFFT.setExtraValue(loExtraFFT);
          laPlotPoints[Constants.HISTORICAL_TRENDS_FFT].add(loDataFFT);
        }
      }

      loTrackDate = loTrackDate.plusDays(1);
    }

    this.resetDataSeries(loChart, laDataSeries, laPlotPoints);

    Misc.setStatusText(0.0);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void analyzeData()
  {
    if (!this.isAlphaVantageKeySet())
    {
      Misc.errorMessage("Your Alpha Vantage key has not been set yet.");
      return;
    }

    if (!this.isAtLeastOneSeriesVisible())
    {
      Misc.errorMessage("At least one of the series must be visible before analyzing.");
      return;
    }

    this.writeXML();

    ThreadDownloadHistorical.INSTANCE.start(true, this, true);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private boolean isAtLeastOneSeriesVisible()
  {
    final int lnCount = this.faSeriesVisibility.length;

    for (int i = 0; i < lnCount; ++i)
    {
      if (this.faSeriesVisibility[i].isSelected())
      {
        return (true);
      }
    }

    return (false);

  }

  // ---------------------------------------------------------------------------------------------------------------------
  private Integer getDateIndex(final ArrayList<JSONDataElements> loJSONDateRangeList, final LocalDate toDate)
  {
    if ((loJSONDateRangeList == null) || (toDate == null))
    {
      return (null);
    }

    int lnDateTrack = 0;
    for (final JSONDataElements loElement : loJSONDateRangeList)
    {
      if (loElement.foDate.isEqual(toDate))
      {
        return (lnDateTrack);
      }

      ++lnDateTrack;
    }

    return (null);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void resetDataSeries(final LineChart toChart, final XYChart.Series<String, Double>[] taDataSeries,
                               final ArrayList<XYChart.Data<String, Double>>[] taPlotPoints)
  {
    final int lnDataSeriesTotal = taDataSeries.length;

    // Must be run in the JavaFX thread, duh.
    // Otherwise, you get java.util.ConcurrentModificationException exceptions.
    Platform.runLater(() ->
    {
      // BUG ALERT!!!!!!!!!!
      // https://stackoverflow.com/questions/48995257/javafx-barchart-xaxis-labels-bad-positioning
      // With a possible solution
      // https://stackoverflow.com/questions/49589889/all-labels-at-the-same-position-in-xaxis-barchart-javafx
      //
      // By the way, you could create a LineChartPlus which sets animate to false. However, I had problems with FXML files
      // as I couldn't create default constructor and setAnimated is called in different spots of JavaFX code. So I just
      // set when needed.
      toChart.setAnimated(false);

      final int lnMaxFirst = (lnDataSeriesTotal > 0) ? taPlotPoints[0].size() : -1;
      // Update all of the series, whether they are visible or not.
      for (int i = 0; i < lnDataSeriesTotal; ++i)
      {
        // From https://stackoverflow.com/questions/19264919/javafx-linechart-areachart-wrong-sorted-values-on-x-axis-categoryaxis
        if (lnMaxFirst < taPlotPoints[i].size())
        {
          System.err.println("The series with all of the points (maximum) should be first so that sorting is not messed up.");
          System.exit(-1);
        }

        taDataSeries[i].getData().clear();
        taDataSeries[i].getData().addAll(taPlotPoints[i]);
      }

    });
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void updateChartTooltips()
  {
    this.updateTooltips(this.chtLineChartData);
    this.updateTooltips(this.chtLineChartTrends);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void updateTooltips(final LineChart toChart)
  {
    // From https://stackoverflow.com/questions/14615590/javafx-linechart-hover-values
    // loop through data and add tooltip
    // THIS MUST BE DONE AFTER ADDING THE DATA TO THE CHART!
    final int lnSeriesCount = toChart.getData().size();
    for (int i = 0; i < lnSeriesCount; ++i)
    {
      final Object loSeries = toChart.getData().get(i);
      if (loSeries instanceof XYChart.Series)
      {
        for (final Object loObject : ((XYChart.Series) loSeries).getData())
        {
          if (loObject instanceof XYChart.Data)
          {
            final XYChart.Data loData = (XYChart.Data) loObject;

            final javafx.scene.Node loNode = loData.getNode();
            // No need to also check for null as that won't be an instance of StackPane.
            if (loNode instanceof StackPane)
            {
              final StackPane loStackPane = (StackPane) loNode;
              // From https://stackoverflow.com/questions/39658056/how-do-i-change-the-size-of-a-chart-symbol-in-a-javafx-scatter-chart
              loStackPane.setPrefWidth(7);
              loStackPane.setPrefHeight(7);

              // The uninstall just uses the node parameter, so you can just pass null:
              //    public static void uninstall(Node node, Tooltip t) {
              //      BEHAVIOR.uninstall(node);
              //    }
              Tooltip.uninstall(loStackPane, null);

              final Tooltip loTooltip = new Tooltip("$ " + loData.getYValue() + " (" + loData.getXValue() + ")");

              loTooltip.setShowDelay(Duration.millis(0));
              Tooltip.install(loStackPane, loTooltip);
            }
          }
        }
      }
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupListeners()
  {
    this.btnAnalyze.setOnAction(this);
    this.cboSmoothing.setOnAction(this);

    this.lnkAlphaVantageMessage.setOnAction(this);

    for (final CheckBoxPlus loCheckBoxPlus : this.faSeriesVisibility)
    {
      loCheckBoxPlus.setOnAction(this);
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  // this.cboStocks is handeled by refreshData
  private void setupComboBoxes()
  {
    this.cboRanges.getItems().clear();
    this.cboRanges.getItems().addAll(Constants.HISTORICAL_RANGE);
    this.cboRanges.getSelectionModel().select(0);

    this.cboSmoothing.getItems().clear();
    this.cboSmoothing.getItems().addAll(Constants.HISTORICAL_SMOOTH_COEFFICIENTS);
    for (final Object loItem : this.cboSmoothing.getItems())
    {
      if (((DoubleKeyItem) loItem).getKey() == 1.0)
      {
        this.cboSmoothing.getSelectionModel().select(loItem);
        break;
      }
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void initializeCharts()
  {
    if (this.faSeriesColors.length < this.faXYDataSeriesTrends.length)
    {
      System.err.println("this.faSeriesColors.length < this.faXYDataSeriesTrends.length. PLEASE CORRECT.");
      return;
    }

    final StringBuilder loStyles = new StringBuilder();
    for (final XYChart.Series loSeries : this.faXYDataSeriesData)
    {
      this.chtLineChartData.getData().add(loSeries);
      final int lnSize = this.chtLineChartData.getData().size();
      loStyles.append(this.getChartColorString(lnSize, this.faSeriesColors[lnSize - 1]));
    }

    if (loStyles.length() > 0)
    {
      this.chtLineChartData.setStyle(loStyles.toString());
    }

    // Reset.
    loStyles.setLength(0);

    for (final XYChart.Series loSeries : this.faXYDataSeriesTrends)
    {
      this.chtLineChartTrends.getData().add(loSeries);
      final int lnSize = this.chtLineChartTrends.getData().size();
      loStyles.append(this.getChartColorString(lnSize, this.faSeriesColors[lnSize - 1]));
    }

    if (loStyles.length() > 0)
    {
      this.chtLineChartTrends.setStyle(loStyles.toString());
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupXYDataSeries()
  {
    this.faXYDataSeriesData = new XYChart.Series[5];

    this.faXYDataSeriesData[0] = new XYChart.Series();
    this.faXYDataSeriesData[0].setName("Open");
    this.faXYDataSeriesData[1] = new XYChart.Series();
    this.faXYDataSeriesData[1].setName("High");
    this.faXYDataSeriesData[2] = new XYChart.Series();
    this.faXYDataSeriesData[2].setName("Low");
    this.faXYDataSeriesData[3] = new XYChart.Series();
    this.faXYDataSeriesData[3].setName("Close");
    this.faXYDataSeriesData[4] = new XYChart.Series();
    this.faXYDataSeriesData[4].setName("Adj Close");

    //*****
    this.faXYDataSeriesTrends = new XYChart.Series[3];

    this.faXYDataSeriesTrends[Constants.HISTORICAL_TRENDS_REGRESS] = new XYChart.Series();
    this.faXYDataSeriesTrends[Constants.HISTORICAL_TRENDS_REGRESS].setName("Regression");

    this.faXYDataSeriesTrends[Constants.HISTORICAL_TRENDS_FFT] = new XYChart.Series();
    this.faXYDataSeriesTrends[Constants.HISTORICAL_TRENDS_FFT].setName("Fourier Transform");

    this.faXYDataSeriesTrends[Constants.HISTORICAL_TRENDS_RAW_DATA_AVG] = new XYChart.Series();
    this.faXYDataSeriesTrends[Constants.HISTORICAL_TRENDS_RAW_DATA_AVG].setName("Raw Data Avg");

    //*****
    // From modena.css at https://gist.github.com/maxd/63691840fc372f22f470
    // which define CHART_COLOR_1 through CHART_COLOR_8
    this.faSeriesColors = new String[5];
    this.faSeriesColors[0] = "#57b757";
    this.faSeriesColors[1] = "#4258c9";
    this.faSeriesColors[2] = "#f3622d";
    this.faSeriesColors[3] = "#fba71b";
    this.faSeriesColors[4] = "#41a9c9";
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupCheckboxes()
  {
    if (this.faXYDataSeriesData == null)
    {
      System.err.println("EROR: setupCheckboxes muse be called after setupXYDataSeries");
      return;
    }

    final int lnCount = this.faXYDataSeriesData.length;
    this.faSeriesVisibility = new CheckBoxPlus[lnCount];

    for (int i = 0; i < lnCount; ++i)
    {
      final CheckBoxPlus loCheckBox = new CheckBoxPlus(this.faXYDataSeriesData[i].getName());
      loCheckBox.setSelected(true);
      this.faSeriesVisibility[i] = loCheckBox;

      this.hboxSeriesVisibility.getChildren().add(loCheckBox);
    }

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
    if (lnLength < Constants.ALPHA_KEY_MASK_LIMIT)
    {
      loMasked.append("*".repeat(Constants.ALPHA_KEY_MASK_LIMIT));

      return (loMasked.toString());
    }

    final char[] taChar = tcKey.toCharArray();
    for (int i = 0; i < lnLength; ++i)
    {
      loMasked.append(i < (lnLength - Constants.ALPHA_KEY_MASK_LIMIT) ? '*' : taChar[i]);
    }

    return (loMasked.toString());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getAlphaVantageKey()
  {
    return (AppProperties.INSTANCE.getAlphaVantageAPIKey());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  synchronized public void refreshData()
  {
    // Just in case, the AlphaVantage key has been modified in the Options dialog.
    this.refreshLabels();

    final ComboBox<StringKeyItem> loCombo = this.cboStocks;

    final StringKeyItem loSelectItem = loCombo.getSelectionModel().getSelectedItem();
    final String lcSelectKey = (loSelectItem != null) ? loSelectItem.getKey() : "";

    // Save the onAction event then set to null so nothing happens when rebuilding the list.
    final EventHandler<ActionEvent> loActionHandler = loCombo.getOnAction();
    loCombo.setOnAction(null);

    final HibernateUtil loHibernate = HibernateUtil.INSTANCE;
    final Session loSession = loHibernate.getSession();

    // Needs to be * (all fields) as we're populating the SymbolEntity.class.
    final NativeQuery loQuery = loSession.createNativeQuery(String.format("SELECT * FROM %s WHERE (description <> '') ORDER BY description", loHibernate.getTableSymbol()), SymbolEntity.class);

    final ObservableList<StringKeyItem> loStringKeys = FXCollections.observableArrayList();

    final List<SymbolEntity> loList = loQuery.list();

    StringKeyItem loMatchItem = null;
    for (final SymbolEntity loRow : loList)
    {
      final String lcID = loRow.getSymbol().trim();
      final StringKeyItem loKeyItem = new StringKeyItem(lcID, loRow.getDescription());
      loStringKeys.add(loKeyItem);

      if (loMatchItem != null)
      {
        continue;
      }

      if (loKeyItem.getKey().equals(lcSelectKey))
      {
        loMatchItem = loKeyItem;
      }

    }
    loSession.close();

    loCombo.getItems().clear();
    loCombo.setItems(loStringKeys);

    // Reset before selection occurs so that the relevant select actions take place.
    loCombo.setOnAction(loActionHandler);

    if (loMatchItem != null)
    {
      // Must match an existing item object in the list. loSelectedItem object
      // doesn't exist in the new list.
      loCombo.getSelectionModel().select(loMatchItem);
    }
    else
    {
      loCombo.getSelectionModel().select(0);
    }

  }

  // -----------------------------------------------------------------------------
  private void updateOnComboBoxSelect()
  {
    if (!this.readXML())
    {
      final StringKeyItem loItem = this.cboStocks.getSelectedItem();

      this.setTitleMessage(String.format("Unable to obtain the setup data for %s (%s)", loItem.getDescription(), loItem.getKey()), true);

      return;
    }

    this.setTitleMessage(String.format("%s (%s)", this.fcCurrentDescription, this.fcCurrentSymbol), false);
    this.updateComponentsFromXML();
  }

  // -----------------------------------------------------------------------------
  private void updateComponentsFromXML()
  {
    final XMLTextReader loReader = XMLTextReader.INSTANCE;
    if (!this.fcCurrentXML.isEmpty() && loReader.initializeXMLDocument(this.fcCurrentXML, false))
    {
/*
      this.chkUseToday.setSelected(loReader.getBoolean(Constants.XML_SYMBOL_USE_TODAY, true));
      // I've decided to store the dates as string rather than longs as it's easier to read the XML with human eyes.
      final String lcStart = loReader.getString(Constants.XML_SYMBOL_START_DATE, LocalDate.now().toString());
      this.txtStart.setValue(LocalDate.parse(lcStart));

      final String lcEnd = loReader.getString(Constants.XML_SYMBOL_END_DATE, LocalDate.now().toString());
      this.updateEndDate(LocalDate.parse(lcEnd));
      */
      return;
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void writeXML()
  {
    final XMLTextWriter loTextWriter = XMLTextWriter.INSTANCE;
    loTextWriter.initializeXMLDocument();
    loTextWriter.createRootNode(Constants.XML_SYMBOL_ROOT_LABEL, null);

    final Node loRecord = loTextWriter.appendNodeToRoot(Constants.XML_SYMBOL_RECORD_LABEL, (String) null, null);

    /*
    loTextWriter.appendToNode(loRecord, Constants.XML_SYMBOL_USE_TODAY, this.chkUseToday.isSelected() ? Constants.XML_TRUE : Constants.XML_FALSE, null);
    // I've decided to store the dates as string rather than longs as it's easier to read the XML with human eyes.
    loTextWriter.appendToNode(loRecord, Constants.XML_SYMBOL_START_DATE, this.txtStart.getValue().toString(), null);
    loTextWriter.appendToNode(loRecord, Constants.XML_SYMBOL_END_DATE, this.txtEnd.getValue().toString(), null);
*/
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

    final StringKeyItem loItem = this.cboStocks.getSelectedItem();
    final String lcKey = loItem.getKey();
    final String lcDescription = loItem.getDescription();
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
  // I got the idea from https://stackoverflow.com/questions/23228344/change-chart-color
  // Helps with a series having a constant color as series are removed / added.
  private String getChartColorString(final int tnIndex1Based, final String tcColor)
  {
    return (String.format("CHART_COLOR_%d: %s;\n", tnIndex1Based, tcColor));
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
      this.updateOnComboBoxSelect();
      this.analyzeData();
    }
    else if (loSource.equals(this.cboSmoothing))
    {
      ThreadDownloadHistorical.INSTANCE.start(true, this, false);
    }
    else if (loSource instanceof Hyperlink)
    {
      final Hyperlink loHyperLink = (Hyperlink) loSource;

      final String lcURL = loHyperLink.getText();
      Main.getMainHostServices().showDocument(lcURL);
    }
    // This should come last.
    else if (loSource instanceof CheckBoxPlus)
    {
      if (((CheckBoxPlus) loSource).getParent() == this.hboxSeriesVisibility)
      {
        ThreadDownloadHistorical.INSTANCE.start(true, this, false);
      }
    }

  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

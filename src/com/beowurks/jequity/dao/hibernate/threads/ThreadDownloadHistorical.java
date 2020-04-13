/*
 * JEquity
 * Copyright(c) 2008-2020, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */
package com.beowurks.jequity.dao.hibernate.threads;

import com.beowurks.jequity.controller.tab.HistoricalDateInfo;
import com.beowurks.jequity.controller.tab.TabHistoricalGraphController;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ProgressBar;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class ThreadDownloadHistorical extends ThreadBase implements Runnable
{
  public static final ThreadDownloadHistorical INSTANCE = new ThreadDownloadHistorical();

  private String fcSymbol;
  private LineChart<String, Number> chtLineChart;

  private class DataElements
  {
    LocalDate foDate;
    final double[] faNumbers = new double[5];
  }

  private final ArrayList<DataElements> foDataList = new ArrayList<>();

  private TabHistoricalGraphController foTabHistoricalGraphController;

  private final DateTimeFormatter foXAxisFormat = DateTimeFormatter.ofPattern("MM-dd-yy");

  private final DateTimeFormatter foMonthTrackerDateFormat = DateTimeFormatter.ofPattern("MMMMyyyy");
  private final DateTimeFormatter foHistoricalFileDateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");

  // ---------------------------------------------------------------------------------------------------------------------
  private ThreadDownloadHistorical()
  {
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public boolean start(final boolean tlDisplayMessage, final TabHistoricalGraphController toTabHistoricalGraphController)
  {
    this.flDisplayDialogMessage = tlDisplayMessage;

    if ((this.foThread != null) && (this.foThread.isAlive()))
    {
      if (this.flDisplayDialogMessage)
      {
        Misc.errorMessage("The historical stock information is currently being updated. . . .");
      }
      return (false);
    }

    this.foTabHistoricalGraphController = toTabHistoricalGraphController;
    this.chtLineChart = this.foTabHistoricalGraphController.getChartData();
    this.fcSymbol = this.foTabHistoricalGraphController.getSymbol();

    this.foThread = new Thread(this);
    this.foThread.setPriority(Thread.NORM_PRIORITY);
    this.foThread.start();

    return (true);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  public void run()
  {
    Misc.setStatusText(ProgressBar.INDETERMINATE_PROGRESS);

    if (this.downloadHistoricalFile())
    {
      this.updateChartData();
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  private boolean updateChartData()
  {
    final LineChart loChart = this.foTabHistoricalGraphController.getChartData();
    final XYChart.Series<String, Double>[] laDataSeries = this.foTabHistoricalGraphController.getDataSeriesData();
    final int lnDataSeriesTotal = laDataSeries.length;

    final StringBuilder loTrackDatesUsed = new StringBuilder(",");
    final HistoricalDateInfo loDateInfo = this.foTabHistoricalGraphController.getHistoricalDateInfo();

    // From https://stackoverflow.com/questions/28850211/performance-issue-with-javafx-linechart-with-65000-data-points
    final ArrayList<XYChart.Data<String, Double>>[] laPlotPoints = new ArrayList[lnDataSeriesTotal];
    for (int i = 0; i < lnDataSeriesTotal; i++)
    {
      laPlotPoints[i] = new ArrayList<>();
    }

    // By the way, I wanted to use the techniques found here;
    // From https://stackoverflow.com/questions/46987823/javafx-line-chart-with-date-axis
    // However, I was having round off problems where 1,566,566,566,566 was converted to 1,500,000,000,000.
    for (final DataElements loElement : this.foDataList)
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
      loChart.setAnimated(false);

      // Update all of the series, whether they are visible or not.
      // Important, as the refreshChart assumes that they are complete when toggling the CheckBoxes.
      for (int i = 0; i < lnDataSeriesTotal; ++i)
      {
        laDataSeries[i].getData().clear();
        laDataSeries[i].getData().addAll(laPlotPoints[i]);
      }

      this.foTabHistoricalGraphController.refreshCharts();
    });

    Misc.setStatusText(0.0);

    return (true);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private boolean downloadHistoricalFile()
  {
    Misc.setStatusText("Starting the process for historical data. . . .");

    final String lcSymbol = this.fcSymbol;
    final String lcURL = this.foTabHistoricalGraphController.getAlphaVantageURL();

    final String lcJSONFile = String.format("%s%s-%s.json", Constants.TEMPORARY_HISTORICAL_PATH, lcSymbol, LocalDate.now().format(this.foHistoricalFileDateFormat));
    final File loJSONFile = new File(lcJSONFile);

    Misc.setStatusText(String.format("Downloading information for the symbol of %s . . . .", lcSymbol));

    String lcJSONText = null;

    if (loJSONFile.exists())
    {
      Misc.setStatusText("Reading the historical data from the cache. . . .");

      try
      {
        lcJSONText = FileUtils.readFileToString(loJSONFile, Charset.defaultCharset());
      }
      catch (final IOException loErr)
      {
        lcJSONText = null;
      }
    }
    else
    {
      Misc.setStatusText("Downloading the historical data. . . .");

      try
      {
        // Highly recommended to set the userAgent.
        // Leave out data. I get errors when setting that parameter.
        lcJSONText = Jsoup.connect(lcURL)
          .followRedirects(false)
          .userAgent(Constants.getUserAgent())
          .maxBodySize(0)
          .timeout(Constants.WEB_TIME_OUT)
          .ignoreContentType(true)
          .execute()
          .body();

        FileUtils.writeStringToFile(loJSONFile, lcJSONText, Charset.defaultCharset());
      }
      catch (final Exception loErr)
      {
        lcJSONText = null;
      }
    }

    if (lcJSONText == null)
    {
      // Just in case the file was created.
      FileUtils.deleteQuietly(loJSONFile);

      final String lcMessage = String.format("Unable to read the page of %s. Make sure that the stock symbol, %s, is still valid. If so, try again.", lcURL, lcSymbol);

      // Display both messages, so at least one of them is noticed.
      Misc.errorMessage(lcMessage);
      Misc.setStatusText(lcMessage, 0.0);

      return (false);
    }

    final boolean llOkay = this.updateDataList("[" + lcJSONText.trim() + "]");
    if (llOkay)
    {
      Misc.setStatusText("Successfully read & imported " + lcSymbol + " historical information");
    }
    else
    {
      // If unable to read, then, if the file exists, the file is corrupt.
      FileUtils.deleteQuietly(loJSONFile);

      Misc.setStatusText("Unable to read from Alpha Vantage for " + lcSymbol + " historical information", 0.0);
    }

    return (llOkay);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private boolean updateDataList(final String tcJSONText)
  {
    JSONArray laJSONInfo = null;
    this.foDataList.clear();
    try
    {
      laJSONInfo = new JSONArray(tcJSONText);
    }
    catch (final Exception loErr)
    {
      Misc.showStackTraceInMessage(loErr, "ooops");
      return (false);
    }

    final HistoricalDateInfo loDateInfo = this.foTabHistoricalGraphController.getHistoricalDateInfo();
    final LocalDate ldStart = loDateInfo.foLocalStartDate;
    final LocalDate ldEnd = loDateInfo.foLocalEndDateData;

    final Object loSeries = this.getSeries(laJSONInfo);
    if (loSeries == null)
    {
      return (false);
    }

    final JSONObject loDates = (JSONObject) loSeries;
    final Iterator<String> loIterator = loDates.keys();

    while (loIterator.hasNext())
    {
      final String loDateKey = loIterator.next();
      final Object loValues = loDates.get(loDateKey);

      final DataElements loElement = new DataElements();

      loElement.foDate = LocalDate.parse(loDateKey);

      if ((loElement.foDate.isBefore(ldStart)) || (loElement.foDate.isAfter(ldEnd)))
      {
        continue;
      }

      if (loValues instanceof JSONObject)
      {
        final JSONObject loNumbers = (JSONObject) loValues;
        loNumbers.keys().forEachRemaining(loSequence ->
        {
          final Object loStockValue = loNumbers.get(loSequence);

          try
          {
            final int lnIndex = Integer.parseInt(loSequence.trim().substring(0, 1)) - 1;
            if ((lnIndex >= 0) && (lnIndex < loElement.faNumbers.length))
            {
              loElement.faNumbers[lnIndex] = Double.parseDouble(loStockValue.toString().trim());
            }
          }
          catch (final NumberFormatException ignore)
          {
          }
        });

      }

      this.foDataList.add(loElement);
    }

    this.foDataList.sort(Comparator.comparing(o -> o.foDate));

    return (true);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private Object getSeries(final JSONArray taJSONInfo)
  {

    for (final Object loElement : taJSONInfo)
    {
      if (loElement instanceof JSONObject)
      {
        final JSONObject loTopObject = (JSONObject) loElement;

        try
        {
          final Object loSeries = loTopObject.get("Time Series (Daily)");
          if (loSeries instanceof JSONObject)
          {
            return (loSeries);
          }
        }
        catch (final JSONException ignore)
        {
        }
      }
    }

    return (null);
  }
  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

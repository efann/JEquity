/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */
package com.beowurks.jequity.dao.hibernate.threads;

import com.beowurks.jequity.controller.tab.HistoricalStartDateInfo;
import com.beowurks.jequity.controller.tab.TabHistoricalGraphController;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.time.DayOfWeek;
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
    this.chtLineChart = this.foTabHistoricalGraphController.getChart();
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
      this.updateChart();
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  private boolean updateChart()
  {
    final LineChart loChart = this.foTabHistoricalGraphController.getChart();
    final int lnSize = loChart.getData().size();

    // From https://stackoverflow.com/questions/28850211/performance-issue-with-javafx-linechart-with-65000-data-points
    final ArrayList<XYChart.Data<String, Number>>[] laTempList = new ArrayList[lnSize];
    for (int i = 0; i < lnSize; i++)
    {
      laTempList[i] = new ArrayList<>();
    }

    // By the way, I wanted to use the techniques found here;
    // From https://stackoverflow.com/questions/46987823/javafx-line-chart-with-date-axis
    // However, I was having round off problems where 1,566,566,566,566 was converted to 1,500,000,000,000.
    for (final DataElements loElement : this.foDataList)
    {
      final String lcDate = this.foXAxisFormat.format(loElement.foDate);
      // Now add the elements to the particular line.
      final int lnCount = loElement.faNumbers.length;
      for (int i = 0; i < lnCount; ++i)
      {
        final XYChart.Data loData = new XYChart.Data<>(lcDate, loElement.faNumbers[i]);

        laTempList[i].add(loData);
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
      loChart.setAnimated(false);

      for (int i = 0; i < lnSize; ++i)
      {
        final XYChart.Series loSeries = (XYChart.Series) loChart.getData().get(i);
        loSeries.getData().clear();
      }

      for (int i = 0; i < lnSize; ++i)
      {
        final XYChart.Series loSeries = (XYChart.Series) loChart.getData().get(i);
        loSeries.getData().addAll(laTempList[i]);
      }

      // From https://stackoverflow.com/questions/14615590/javafx-linechart-hover-values
      //loop through data and add tooltip
      //THIS MUST BE DONE AFTER ADDING THE DATA TO THE CHART!
      for (int i = 0; i < lnSize; ++i)
      {
        final Object loSeries = loChart.getData().get(i);
        if (loSeries instanceof XYChart.Series)
        {
          for (final Object loObject : ((XYChart.Series) loSeries).getData())
          {
            if (loObject instanceof XYChart.Data)
            {
              final XYChart.Data loData = (XYChart.Data) loObject;

              final Node loNode = loData.getNode();
              if ((loNode != null) && (loNode instanceof StackPane))
              {
                final StackPane loStackPane = (StackPane) loNode;
                // From https://stackoverflow.com/questions/39658056/how-do-i-change-the-size-of-a-chart-symbol-in-a-javafx-scatter-chart
                loStackPane.setPrefWidth(7);
                loStackPane.setPrefHeight(7);

                final Tooltip loTooltip = new Tooltip("$ " + loData.getYValue() + " (" + loData.getXValue() + ")");
                loTooltip.setShowDelay(Duration.millis(0));
                Tooltip.install(loStackPane, loTooltip);
              }
            }
          }
        }
      }
    });

    Misc.setStatusText(0.0);

    return (true);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private boolean downloadHistoricalFile()
  {
    Misc.setStatusText("Downloading the historical data. . . .");

    final String lcSymbol = this.fcSymbol;
    final String lcURL = this.foTabHistoricalGraphController.getAlphaVantageURL();

    Misc.setStatusText(String.format("Downloading information for the symbol of %s . . . .", lcSymbol));

    String lcJSONText = null;
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

    }
    catch (final Exception loErr)
    {
      lcJSONText = null;
    }

    if (lcJSONText == null)
    {
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

    final HistoricalStartDateInfo loDateInfo = this.foTabHistoricalGraphController.getStartDateInfo();
    final LocalDate ldStart = loDateInfo.foLocalDate;
    final LocalDate ldEnd = this.foTabHistoricalGraphController.getEndDate();

    final Object loSeries = this.getSeries(laJSONInfo);
    if (loSeries == null)
    {
      return (false);
    }

    StringBuilder loTrackDatesUsed = new StringBuilder(",");

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

      boolean llOkay = false;

      if (loElement.foDate.isEqual(ldStart))
      {
        llOkay = true;
      }
      else if (loDateInfo.fnDataDisplay == Constants.HISTORICAL_EVERY_DAY)
      {
        llOkay = true;
      }
      else if (loDateInfo.fnDataDisplay == Constants.HISTORICAL_EVERY_WEEK)
      {
        llOkay = (loElement.foDate.getDayOfWeek() == DayOfWeek.MONDAY);
        String lcWeekYear = String.format("%2d%d", loElement.foDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR), loElement.foDate.getYear());

        if (!llOkay)
        {
          // If not found, then use and add to the loTrackDatesUsed so that
          // no more days of that particular week will be used.
          llOkay = (loTrackDatesUsed.indexOf(lcWeekYear) == -1);
        }

        if (llOkay)
        {
          loTrackDatesUsed.append(lcWeekYear + ",");
        }

      }
      else if (loDateInfo.fnDataDisplay == Constants.HISTORICAL_EVERY_MONTH)
      {
        //loElement.foDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        String lcMonthYear = loElement.foDate.format(this.foMonthTrackerDateFormat);
        // If not found, then use and add to the loTrackDatesUsed so that
        // no more days of that particular month will be used.
        llOkay = (loTrackDatesUsed.indexOf(lcMonthYear) == -1);
        if (llOkay)
        {
          loTrackDatesUsed.append(lcMonthYear + ",");
        }
      }

      if (!llOkay)
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
    final Iterator<Object> loIterator = taJSONInfo.iterator();

    while (loIterator.hasNext())
    {
      final Object loElement = loIterator.next();
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

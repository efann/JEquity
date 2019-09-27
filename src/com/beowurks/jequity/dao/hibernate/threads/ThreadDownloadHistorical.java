/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */
package com.beowurks.jequity.dao.hibernate.threads;

import com.beowurks.jequity.controller.tab.TabHistoricalGraphController;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class ThreadDownloadHistorical extends ThreadBase implements Runnable
{
  public static ThreadDownloadHistorical INSTANCE = new ThreadDownloadHistorical();

  private String fcSymbol;
  private LineChart<Number, Number> chtLineChart;

  private class DataElements
  {
    Date foDate;
    double[] faNumbers = new double[5];
  }

  private final ArrayList<DataElements> foDataList = new ArrayList<>();

  private final String fcAlphaVantage = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=%s&outputsize=%s&apikey=%s";

  private boolean flProccessRunning = false;
  private TabHistoricalGraphController foTabHistoricalGraphController;

  private final DateFormat foAlphaVantageDateFormat = new SimpleDateFormat("yyyy-MM-dd");
  private final DateFormat foXAxisFormat = new SimpleDateFormat("MM-dd-yy");

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

    // As we're unable to determine if Platform thread has ended (it's always there as
    // we set Platform.setImplicitExit(false); in the Installer), we use flProccessRunning also.
    this.flProccessRunning = true;

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
    if (this.downloadHistoricalFile())
    {
      // Must be run in the JavaFX thread, duh.
      // Otherwise, you get java.util.ConcurrentModificationException exceptions.
      Platform.runLater(() ->
          this.updateChart());
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private boolean updateChart()
  {
    final LineChart loChart = this.foTabHistoricalGraphController.getChart();

    // BUG ALERT!!!!!!!!!!
    // https://stackoverflow.com/questions/48995257/javafx-barchart-xaxis-labels-bad-positioning
    // With a possible solution
    // https://stackoverflow.com/questions/49589889/all-labels-at-the-same-position-in-xaxis-barchart-javafx
    loChart.setAnimated(false);

    final int lnSize = loChart.getData().size();
    for (int i = 0; i < lnSize; ++i)
    {
      final Object loSeries = loChart.getData().get(i);
      if (loSeries instanceof XYChart.Series)
      {
        ((XYChart.Series) loSeries).getData().clear();
      }
    }


    // By the way, I wanted to use the techniques found here;
    // From https://stackoverflow.com/questions/46987823/javafx-line-chart-with-date-axis
    // However, I was having round off problems where 1,566,566,566,566 was converted to 1,500,000,000,000.
    for (final DataElements loElement : this.foDataList)
    {
      final String lcDate = this.foXAxisFormat.format(loElement.foDate);
      final int lnCount = loElement.faNumbers.length;
      for (int i = 0; i < lnCount; ++i)
      {
        final Object loSeries = loChart.getData().get(i);
        if (loSeries instanceof XYChart.Series)
        {
          ((XYChart.Series) loSeries).getData().add(new XYChart.Data<>(lcDate, loElement.faNumbers[i]));
        }
      }

    }


    return (true);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private boolean downloadHistoricalFile()
  {
    Misc.setStatusText("Downloading the historical data. . . .");

    final String lcSymbol = this.fcSymbol;
    //final String lcURL = String.format(this.fcAlphaVantage, "MFST", "compact", "demo");
    final String lcURL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=MSFT&apikey=demo";

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
      final String lcMessage = String.format("Unable to read the page of %s. Make sure that the stock symbol, %s, is still valid.", lcURL, lcSymbol);
      Misc.setStatusText(lcMessage, Constants.THREAD_ERROR_DISPLAY_DELAY);
      return (false);
    }

    final boolean llOkay = this.updateDataList("[" + lcJSONText.trim() + "]");
    if (llOkay)
    {
      Misc.setStatusText("Successfully read " + lcSymbol + " historical information");
    }
    else
    {
      Misc.setStatusText("Unable to read from Alpha Vantage for " + lcSymbol + " historical information");
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

    laJSONInfo.iterator().forEachRemaining(toElement ->
    {
      if (toElement instanceof JSONObject)
      {
        final JSONObject loTopObject = (JSONObject) toElement;

        final Object loSeries = loTopObject.get("Time Series (Daily)");

        if (loSeries instanceof JSONObject)
        {
          final JSONObject loDates = (JSONObject) loSeries;
          loDates.keys().forEachRemaining(loDateKey ->
          {
            final Object loValues = loDates.get(loDateKey);

            final DataElements loElement = new DataElements();

            try
            {
              loElement.foDate = this.foAlphaVantageDateFormat.parse(loDateKey);
            }
            catch (final ParseException ignore)
            {
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
          });
        }
      }
    });

    this.foDataList.sort(Comparator.comparing(o -> o.foDate));

    return (true);
  }
  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

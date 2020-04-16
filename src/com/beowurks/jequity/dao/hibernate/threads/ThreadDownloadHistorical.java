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

  private final ArrayList<JSONDataElements> foJSONDateRangeList = new ArrayList<>();

  private TabHistoricalGraphController foTabHistoricalGraphController;

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
    this.fcSymbol = this.foTabHistoricalGraphController.getSymbol();

    this.foThread = new Thread(this);
    this.foThread.setPriority(Thread.NORM_PRIORITY);
    this.foThread.start();

    return (true);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public TabHistoricalGraphController getTabHistoricalGraphController()
  {
    return (this.foTabHistoricalGraphController);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public ArrayList<JSONDataElements> getJSONDateRangeList()
  {
    return (this.foJSONDateRangeList);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  public void run()
  {
    Misc.setStatusText(ProgressBar.INDETERMINATE_PROGRESS);

    if (this.downloadHistoricalFile())
    {
      this.foTabHistoricalGraphController.recreateChartData();
      this.foTabHistoricalGraphController.recreateChartTrends();

      // Must be run in the JavaFX thread, duh.
      // Otherwise, you get java.util.ConcurrentModificationException exceptions.
      Platform.runLater(() ->
        this.foTabHistoricalGraphController.redrawCharts(false));
    }

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

    final boolean llOkay = this.updateJSONDateRangeList("[" + lcJSONText.trim() + "]");
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
  private boolean updateJSONDateRangeList(final String tcJSONText)
  {
    this.foJSONDateRangeList.clear();

    JSONArray laJSONInfo = null;

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

      final JSONDataElements loElement = new JSONDataElements();

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

      this.foJSONDateRangeList.add(loElement);
    }

    this.foJSONDateRangeList.sort(Comparator.comparing(o -> o.foDate));

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

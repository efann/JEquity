/*
 * JEquity
 * Copyright(c) 2008-2023, Beowurks
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
import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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

  private final static int INVALID_SEQUENCE_INDEX = -1;
  private String fcSymbol;

  private final ArrayList<JSONDataElements> foJSONDateRangeList = new ArrayList<>();

  private TabHistoricalGraphController foTabHistoricalGraphController;

  private final DateTimeFormatter foHistoricalFileDateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");

  private boolean flRecreateCharts = true;

  // From https://www.baeldung.com/java-lambda-effectively-final-local-variables
  private boolean flUpdateChartCaptions;

  // ---------------------------------------------------------------------------------------------------------------------
  private ThreadDownloadHistorical()
  {
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public boolean start(final boolean tlDisplayMessage, final TabHistoricalGraphController toTabHistoricalGraphController, final boolean tlRecreateCharts)
  {
    this.flDisplayDialogMessage = tlDisplayMessage;
    this.flRecreateCharts = tlRecreateCharts;

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
    if (this.flRecreateCharts)
    {
      Misc.setStatusText(ProgressBar.INDETERMINATE_PROGRESS);

      if (this.downloadHistoricalFile())
      {
        this.foTabHistoricalGraphController.recreateChartData();
        this.foTabHistoricalGraphController.recreateChartTrends();
      }
      else
      {
        return;
      }
    }

    // Must be run in the JavaFX thread, duh.
    // Otherwise, you get java.util.ConcurrentModificationException exceptions.
    Platform.runLater(() ->
      this.foTabHistoricalGraphController.redrawCharts(!this.flRecreateCharts));

  }

  // ---------------------------------------------------------------------------------------------------------------------
  private boolean downloadHistoricalFile()
  {
    Misc.setStatusText("Starting the process for historical data. . . .");

    final String lcSymbol = this.fcSymbol;
    final URL loURL;
    try
    {
      loURL = new URL(this.foTabHistoricalGraphController.getAlphaVantageURL());
    }
    catch (final MalformedURLException loErr)
    {
      throw new RuntimeException(loErr);
    }

    final File loJSONFile = new File(String.format("%s%s-%s.json", Constants.TEMPORARY_HISTORICAL_PATH, lcSymbol, LocalDate.now().format(this.foHistoricalFileDateFormat)));

    Misc.setStatusText(String.format("Downloading information for the symbol of %s . . . .", lcSymbol));

    String lcJSONText = null;

    // Don't download again if the file was already downloaded today (the date is in the name).
    if (!loJSONFile.exists())
    {
      Misc.setStatusText("Downloading the historical data. . . .");

      try
      {
        FileUtils.copyURLToFile(loURL, loJSONFile, Constants.WEB_TIME_OUT, Constants.WEB_TIME_OUT);
      }
      catch (final Exception loErr)
      {
      }
    }

    Misc.setStatusText("Reading the historical data from the cache. . . .");
    try
    {
      lcJSONText = FileUtils.readFileToString(loJSONFile, Charset.defaultCharset());
    }
    catch (final IOException loErr)
    {
    }

    if (lcJSONText == null)
    {
      // Just in case the file was created.
      FileUtils.deleteQuietly(loJSONFile);

      final String lcMessage = String.format("Unable to read the page of %s. Make sure that the stock symbol, %s, is still valid. If so, try again.", loURL, lcSymbol);

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
      Misc.errorMessage(this.deciphorErrorMessage(laJSONInfo));

      return (false);
    }

    final JSONObject loDates = (JSONObject) loSeries;
    final Iterator<String> loIterator = loDates.keys();
    this.flUpdateChartCaptions = true;

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

      if (loValues instanceof final JSONObject loNumbers)
      {
        loNumbers.keys().forEachRemaining(loSequence ->
        {
          final Object loStockValue = loNumbers.get(loSequence);

          try
          {
            final int lnIndex = this.getSequenceIndex(loSequence, loElement);
            if (lnIndex != ThreadDownloadHistorical.INVALID_SEQUENCE_INDEX)
            {
              loElement.faNumbers[lnIndex] = Double.parseDouble(loStockValue.toString().trim());
              if (this.flUpdateChartCaptions)
              {
                this.foTabHistoricalGraphController.updateDataSeriesLabels(lnIndex, this.getSequenceLabel(loSequence));
              }
            }
          }
          catch (final NumberFormatException ignore)
          {
          }
        });

        if (this.flUpdateChartCaptions)
        {
          this.foTabHistoricalGraphController.updateCheckboxesLabels();
          this.flUpdateChartCaptions = false;
        }

      }

      this.foJSONDateRangeList.add(loElement);
    }

    this.foJSONDateRangeList.sort(Comparator.comparing(o -> o.foDate));

    return (true);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private String getSequenceLabel(final String toSequence)
  {
    final int lnPos = toSequence.indexOf(' ');
    if (lnPos != -1)
    {
      return (WordUtils.capitalize(toSequence.substring(lnPos).trim()));
    }

    return ("<empty>");
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private int getSequenceIndex(final String toSequence, final JSONDataElements toElement) throws NumberFormatException
  {
    final int lnIndex = Integer.parseInt(toSequence.trim().substring(0, 1)) - 1;

    if ((lnIndex >= 0) && (lnIndex < toElement.faNumbers.length))
    {
      return (lnIndex);
    }

    return (ThreadDownloadHistorical.INVALID_SEQUENCE_INDEX);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private String deciphorErrorMessage(final JSONArray taJSONInfo)
  {
    String lcMessage = "There was an unknown error in reading the JSON file.";
    if (taJSONInfo.length() != 0)
    {
      final Object loObject = taJSONInfo.get(0);
      if (loObject instanceof final JSONObject loJSONObj)
      {
        try
        {
          final String lcKey = loJSONObj.keys().next();
          lcMessage = loJSONObj.getString(lcKey);
        }
        catch (final JSONException ignored)
        {
        }

      }
    }

    return (lcMessage + "\n\nEnsure that your Alpha Vantage key is valid.");
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private Object getSeries(final JSONArray taJSONInfo)
  {
    for (final Object loElement : taJSONInfo)
    {
      if (loElement instanceof final JSONObject loTopObject)
      {
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

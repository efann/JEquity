/*
 * JEquity
 * Copyright(c) 2008-2023, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.dao.web;

import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// This class contains the default values for scraping a page. It is called in AppProperties.
public class ConfigJSONSettings implements Runnable
{
  public static final ConfigJSONSettings INSTANCE = new ConfigJSONSettings();

  private static final String BASE_FILE = "config.json";
  private static final String CONFIG_URL = String.format("https://www.beowurks.com/Software/JEquity/%s", ConfigJSONSettings.BASE_FILE);
  private static final String CONFIG_URL_LOCAL = Constants.TEMPORARY_PATH + ConfigJSONSettings.BASE_FILE;
  private String fcYahooDescriptionMarker = Constants.WEB_MARKER_DEFAULT_VALUE_DESCRIPTION;
  // List of previously used values
  //   "#quote-header-info div[class^=My] span[class^=Trsdu]"
  private String fcYahooLastTradeMarker = Constants.WEB_MARKER_DEFAULT_VALUE_LASTTRADE;

  private String fcWebPageURL = Constants.WEB_PAGE_URL;
  private String fcAlphaVantageURL = Constants.ALPHA_VANTAGE_URL;

  private Thread foThread = null;

  // ---------------------------------------------------------------------------------------------------------------------
  private ConfigJSONSettings()
  {
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void refreshData()
  {
    if ((this.foThread != null) && (this.foThread.isAlive()))
    {
      return;
    }

    this.foThread = new Thread(this);
    this.foThread.setPriority(Thread.NORM_PRIORITY);
    this.foThread.start();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  public void run()
  {
    final URL loURL;
    try
    {
      loURL = new URL(ConfigJSONSettings.CONFIG_URL);
    }
    catch (final MalformedURLException loErr)
    {
      throw new RuntimeException(loErr);
    }

    final File loJSONFile = new File(ConfigJSONSettings.CONFIG_URL_LOCAL);
    try
    {
      FileUtils.copyURLToFile(loURL, loJSONFile, Constants.WEB_TIME_OUT, Constants.WEB_TIME_OUT);
    }
    catch (final Exception loErr)
    {
    }

    String lcJSONText = null;
    try
    {
      lcJSONText = FileUtils.readFileToString(loJSONFile, Charset.defaultCharset());
    }
    catch (final IOException loErr)
    {
    }

    if (lcJSONText == null)
    {
      final String lcMessage = String.format("Unable to read the page of %s.", ConfigJSONSettings.CONFIG_URL);
      Misc.setStatusText(lcMessage, Constants.THREAD_ERROR_DISPLAY_DELAY);

      return;
    }

    final JSONObject loJSONObject = new JSONObject(lcJSONText);

    final JSONObject loStocksJSON = loJSONObject.getJSONObject("stocks");
    final JSONObject loHistoricalJSON = loJSONObject.getJSONObject("historical");

    final String lcDescription = loStocksJSON.get("description-marker").toString().trim();
    if (!lcDescription.isEmpty())
    {
      this.fcYahooDescriptionMarker = lcDescription;
    }

    final String lcLastTrade = loStocksJSON.get("lasttrade-marker").toString().trim();
    if (!lcLastTrade.isEmpty())
    {
      this.fcYahooLastTradeMarker = lcLastTrade;
    }

    final String lcURL = loStocksJSON.get("web-page-url").toString().trim();
    if (!lcURL.isEmpty())
    {
      this.fcWebPageURL = lcURL;
    }

    final String lcAlphaVantageURL = loHistoricalJSON.get("alpha-vantage-url").toString().trim();
    if (!lcAlphaVantageURL.isEmpty())
    {
      this.fcAlphaVantageURL = lcAlphaVantageURL;
    }

  }


  // ---------------------------------------------------------------------------------------------------------------------
  public String getMarkerDescription()
  {
    return (this.fcYahooDescriptionMarker);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getMarkerLastTrade()
  {
    return (this.fcYahooLastTradeMarker);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getWebPageURL()
  {
    return (this.fcWebPageURL);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getAlphaVantageURL()
  {
    return (this.fcAlphaVantageURL);
  }
  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

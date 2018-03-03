/*
 * JEquity
 * Copyright(c) 2008-2018, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.dao;

import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class HTMLMarkers implements Runnable
{
  public static HTMLMarkers INSTANCE = new HTMLMarkers();

  private static final String CONFIG_URL = "https://www.beowurks.com/Software/JEquity/config.xml";
  private static final String SYMBOL_MARKER = "###symbol###";

  private String fcYahooDailyURL = String.format("https://finance.yahoo.com/quote/%s?p=%s", HTMLMarkers.SYMBOL_MARKER, HTMLMarkers.SYMBOL_MARKER);
  private String fcYahooDescriptionMarker = "#quote-header-info h1";
  private String fcYahooLastTradeMarker = "#quote-header-info div[class^=My] span[class^=Trsdu]";

  // ---------------------------------------------------------------------------------------------------------------------
  private HTMLMarkers()
  {
    this.refreshData();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void refreshData()
  {
    final Thread loThread = new Thread(this);
    loThread.setPriority(Thread.NORM_PRIORITY);
    loThread.start();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  public void run()
  {
    Document loDoc = null;
    for (int lnTries = 0; (lnTries < 5) && (loDoc == null); ++lnTries)
    {
      try
      {
        // Highly recommended to set the userAgent.
        loDoc = Jsoup.connect(HTMLMarkers.CONFIG_URL + "?" + System.currentTimeMillis())
            .followRedirects(true)
            .userAgent(Constants.USER_AGENT[0])
            .data("name", "jsoup")
            .maxBodySize(0)
            .timeout(Constants.WEB_TIME_OUT)
            .get();
      }
      catch (final Exception loErr)
      {
        loDoc = null;
      }
    }

    if (loDoc == null)
    {
      final String lcMessage = String.format("Unable to read the page of %s.", HTMLMarkers.CONFIG_URL);
      Misc.setStatusText(lcMessage, Constants.THREAD_ERROR_DISPLAY_DELAY);

      return;
    }

    final String lcDescription = loDoc.select("description-marker").text().trim();
    if (!lcDescription.isEmpty())
    {
      this.fcYahooDescriptionMarker = lcDescription;
    }

    final String lcLastTrade = loDoc.select("lasttrade-marker").text().trim();
    if (!lcLastTrade.isEmpty())
    {
      this.fcYahooLastTradeMarker = lcLastTrade;
    }

    final String lcURL = loDoc.select("daily-url").text().trim();
    if (!lcURL.isEmpty())
    {
      this.fcYahooDailyURL = lcURL;
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getDailyStockURL(final String tcSymbol)
  {
    final String lcSymbol = tcSymbol.trim();
    final String lcURL = this.fcYahooDailyURL.replaceAll(HTMLMarkers.SYMBOL_MARKER, lcSymbol);

    return (lcURL);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getDescriptionMarker()
  {
    return (this.fcYahooDescriptionMarker);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getLastTradeMarker()
  {
    return (this.fcYahooLastTradeMarker);
  }

  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

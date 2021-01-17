/*
 * JEquity
 * Copyright(c) 2008-2021, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.dao.web;

import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class PageScraping implements Runnable
{
  public static final PageScraping INSTANCE = new PageScraping();

  private static final String CONFIG_URL = "https://www.beowurks.com/Software/JEquity/config.xml";
  private static final String SYMBOL_MARKER = "###symbol###";

  private String fcYahooDailyURL = String.format("https://finance.yahoo.com/quote/%s?p=%s", PageScraping.SYMBOL_MARKER, PageScraping.SYMBOL_MARKER);
  private String fcYahooDescriptionMarker = "#quote-header-info h1";
  private String fcYahooLastTradeMarker = "#quote-header-info div[class^=My] span[class^=Trsdu]";

  private Thread foThread = null;

  // ---------------------------------------------------------------------------------------------------------------------
  private PageScraping()
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
    Document loDoc = null;
    for (int lnTries = 0; (lnTries < 5) && (loDoc == null); ++lnTries)
    {
      try
      {
        // Highly recommended to set the userAgent.
        loDoc = Jsoup.connect(PageScraping.CONFIG_URL + "?" + System.currentTimeMillis())
          .followRedirects(true)
          .userAgent(Constants.getUserAgent())
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
      final String lcMessage = String.format("Unable to read the page of %s.", PageScraping.CONFIG_URL);
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
    final String lcURL = this.fcYahooDailyURL.replaceAll(PageScraping.SYMBOL_MARKER, lcSymbol);

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

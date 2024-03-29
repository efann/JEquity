/*
 * JEquity
 * Copyright(c) 2008-2023, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */
package com.beowurks.jequity.dao.hibernate.threads;

import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import javafx.application.Platform;
import org.jsoup.nodes.Document;

import java.time.LocalDate;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class ThreadDownloadSingleSymbol extends ThreadDownloadHTML implements Runnable
{

  public static final ThreadDownloadSingleSymbol INSTANCE = new ThreadDownloadSingleSymbol();

  private SingleSymbolInfo foSingleSymbolInfo;

  // ---------------------------------------------------------------------------------------------------------------------
  private ThreadDownloadSingleSymbol()
  {
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public boolean start(final SingleSymbolInfo toSingleSymbolInfo)
  {
    this.foSingleSymbolInfo = toSingleSymbolInfo;

    if ((this.foThread != null) && (this.foThread.isAlive()))
    {
      return (false);
    }

    this.foThread = new Thread(this);
    this.foThread.setPriority(Thread.NORM_PRIORITY);
    this.foThread.start();

    return (true);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  public void run()
  {
    if (!this.foSingleSymbolInfo.runUpdate())
    {
      return;
    }

    this.downloadPageAndImport();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void downloadPageAndImport()
  {
    final String lcSymbol = this.foSingleSymbolInfo.getSymbol().getText();
    if (lcSymbol.isEmpty() || lcSymbol.isBlank())
    {
      final String lcMessage = "The stock symbol is blank so unable to download any information.";
      Misc.setStatusText(lcMessage, Constants.THREAD_ERROR_DISPLAY_DELAY);

      return;
    }

    final String lcDailyURL = Misc.getDailyStockURL(lcSymbol);

    Misc.setStatusText(String.format("Downloading information for the symbol of %s . . . .", lcSymbol));

    // Daily Information
    final Document loDoc = this.getDocument(lcDailyURL);

    if (loDoc == null)
    {
      final String lcMessage = String.format("Unable to read the page of %s. Make sure that the stock symbol, %s, is still valid.", lcDailyURL, lcSymbol);
      Misc.setStatusText(lcMessage, Constants.THREAD_ERROR_DISPLAY_DELAY);

      return;
    }

    // Just so the loDoc value can be used in the runLater routiner.
    final Document loDocValue = loDoc;
    Platform.runLater(() ->
    {
      if (!this.foSingleSymbolInfo.getSaveButton().isDisabled())
      {
        this.importInformation(lcSymbol, loDocValue);
      }
    });


    Misc.setStatusText("Successfully updated " + lcSymbol + " description, price and date");
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // Bye bye, Yahoo Financial CSV downloads. . . .
  // Discovered a new way to import daily information rather than scrape the screen. From the following:
  // http://stackoverflow.com/questions/6308950/capture-yahoo-finance-stock-data-symbols-for-daily-break-out-leaders-etc
  // http://www.gummy-stuff.org/Yahoo-data.htm
  // http://code.google.com/p/yahoo-finance-managed/wiki/enumQuoteProperty
  private void importInformation(final String tcSymbol, final Document toDoc)
  {
    this.refreshCurrentTextList(toDoc, tcSymbol);

    final String lcDescription = this.getDescriptionFromHtml(toDoc);

    this.foSingleSymbolInfo.getDescriptionField().setText(lcDescription);

    final double lnLastTrade = this.getLastTradeValue(toDoc);

    this.foSingleSymbolInfo.getPriceField().setText(Double.toString(lnLastTrade));

    this.foSingleSymbolInfo.getDateField().setValue(LocalDate.now());
  }

  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

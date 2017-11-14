/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */
package com.beowurks.jequity.dao.hibernate.warehouses;

import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.hibernate.SymbolEntity;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class ThreadDownloadSymbolInfo implements Runnable
{

  public static ThreadDownloadSymbolInfo INSTANCE = new ThreadDownloadSymbolInfo();

  private Thread foThread = null;

  private String [] faCurrentDoc;

  // ---------------------------------------------------------------------------------------------------------------------
  private ThreadDownloadSymbolInfo()
  {
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public boolean start(final boolean tlDisplayMessage)
  {
    if ((this.foThread != null) && (this.foThread.isAlive()))
    {
      if (tlDisplayMessage)
      {
        Misc.errorMessage("The stock information is currently being updated. . . .");
      }
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
    final String lcErrorMessage = this.updateSymbolTable();
    // If no error message has been returned.
    if (lcErrorMessage.isEmpty())
    {
      this.updateAllSymbolInformation();
      this.updateFinancialTable();

      return;
    }

    Misc.errorMessage(lcErrorMessage);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // The basic idea behind this routine is to add any symbols that are in
  // the Financial table and remove any symbols that no longer exist.
  private String updateSymbolTable()
  {
    Misc.setStatusText("First updating codes in the Symbol table. . . .");

    final StringBuilder lcMessage = new StringBuilder("");

    final HibernateUtil loHibernate = HibernateUtil.INSTANCE;
    final Session loSession = loHibernate.getSession();

    final String lcSymbolTable = loHibernate.getTableSymbol();
    final String lcFinancialTable = loHibernate.getTableFinancial();

    // I had to use this method rather than using loSession.createNativeQuery.
    // Otherwise I was getting an invalid char '"' in statement error.
    loSession.doWork(toConnection -> {
      Statement loStatement = null;
      try
      {
        final String lcDelete = String.format("DELETE FROM %s WHERE symbol NOT IN (SELECT DISTINCT symbol FROM %s WHERE symbol <> ' ')",
            lcSymbolTable, lcFinancialTable);
        final String lcInsert = String.format("INSERT INTO %s (Symbol) SELECT DISTINCT f.symbol FROM %s "
                + "f LEFT JOIN %s "
                + "s ON f.symbol = s.symbol WHERE (s.symbol IS NULL) AND (f.symbol <> ' ')",
            lcSymbolTable, lcFinancialTable, lcSymbolTable);

        loStatement = toConnection.createStatement();

        loStatement.addBatch(lcDelete);
        loStatement.addBatch(lcInsert);

        loStatement.executeBatch();

        toConnection.commit();

      }
      catch (final Exception loError)
      {
        lcMessage.append(String.format("There was an error in updating symbols with doWork:\n\n%s", loError.getMessage()));
      }
      finally
      {
        if (null != loStatement)
        {
          loStatement.close();
        }
      }
    });

    loSession.close();

    return (lcMessage.toString());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void updateAllSymbolInformation()
  {
    final HibernateUtil loHibernate = HibernateUtil.INSTANCE;
    final Session loSession = loHibernate.getSession();

    final String lcSQL = String.format("SELECT * FROM %s ORDER BY symbol", loHibernate.getTableSymbol());
    final NativeQuery loQuery = loSession.createNativeQuery(lcSQL)
        .addEntity(SymbolEntity.class);

    final List<SymbolEntity> loList = loQuery.list();

    final int lnTotal = loList.size();

    // Must be initialized each time.
    Misc.setStatusText("Downloading. . . .", 0.0);

    for (final SymbolEntity loSymbol : loList)
    {
      final String lcSymbol = loSymbol.getSymbol().trim();

      final String lcDailyURL = ThreadDownloadSymbolInfo.getSymbolDailyURL(lcSymbol);

      Misc.setStatusText(String.format("Downloading information for the symbol of %s . . . .", lcSymbol));

      // Daily Information
      Document loDoc = null;
      for (int lnTries = 0; (lnTries < 5) && (loDoc == null); ++lnTries)
      {
        try
        {
          // Highly recommended to set the userAgent.
          loDoc = Jsoup.connect(lcDailyURL)
              .followRedirects(false)
              .userAgent(Constants.USER_AGENT)
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
        final String lcMessage = String.format("Unable to read the page of %s. Make sure that the stock symbol, %s, is still valid.", lcDailyURL, lcSymbol);
        Misc.setStatusText(lcMessage, Constants.THREAD_ERROR_DISPLAY_DELAY);

        continue;
      }

      Misc.setStatusText("Successfully read " + lcSymbol + " daily information");


      if (this.importDailyInformation(loSession, loSymbol, loDoc))
      {
        Misc.setStatusText("Successfully imported " + lcSymbol + "'s daily information");
      }

      Misc.setStatusText((double) loList.indexOf(loSymbol) / (double) lnTotal);
    }

    loSession.close();

    Misc.setStatusText("Daily information imported. . . .", 0.0);

  }

  private boolean refreshCurrentDoc(final Document toDoc)
  {
    String lcText = toDoc.text();
    lcText = lcText.replaceAll("Previous Close", "Previous Close")

  }
  // ---------------------------------------------------------------------------------------------------------------------
  // Bye bye, Yahoo Financial CSV downloads. . . .
  // Discovered a new way to import daily information rather than scrape the screen. From the following:
  // http://stackoverflow.com/questions/6308950/capture-yahoo-finance-stock-data-symbols-for-daily-break-out-leaders-etc
  // http://www.gummy-stuff.org/Yahoo-data.htm
  // http://code.google.com/p/yahoo-finance-managed/wiki/enumQuoteProperty
  private boolean importDailyInformation(final Session toSession, final SymbolEntity toSymbol, final Document toDoc)
  {
    boolean llOkay = false;


    String lcDescription = this.getHTML(toDoc, "#quote-header-info h1");
    if (lcDescription.isEmpty())
    {
      lcDescription = Constants.UNKNOWN_STOCK_SYMBOL;
    }

    lcDescription = lcDescription.replaceAll("\\(.*\\)", "").trim();
    lcDescription = lcDescription.replaceAll("&amp;", "&").trim();

    toSymbol.setDescription(lcDescription);

    double lnLastTrade = this.parseDouble(toDoc, Constants.LAST_TRADE_HTML_CODE);

    if (lnLastTrade == 0.0)
    {
      // Some symbols, like FDRXX, don't have a last trade field. So in that case,
      // default to 1.0.
      final String lcLastTrade = this.getHTML(toDoc, Constants.LAST_TRADE_HTML_CODE);
      if (lcLastTrade.isEmpty())
      {
        lnLastTrade = 1.0;
      }
    }

    toSymbol.setLastTrade(lnLastTrade);

    final java.util.Date loDate = new java.util.Date();
    final Timestamp loTimestamp = new Timestamp(loDate.getTime());
    toSymbol.setTradeTime(loTimestamp);

    final double lnPrevClose = this.parseDouble(toDoc, "#quote-summary td[class^=Ta] span[data-reactid^=41]");
    toSymbol.setPreviousClose(lnPrevClose);

    toSymbol.setDifferential((lnPrevClose != 0.0) ? ((lnLastTrade - lnPrevClose) / lnPrevClose) * 100.0 : 0.0);

    toSymbol.setOpened(this.parseDouble(toDoc, "#quote-summary td[class^=Ta] span[data-reactid^=47]"));

    toSymbol.setTargetEstimate(this.parseDouble(toDoc, "#quote-summary td[class^=Ta] span[data-reactid^=129]"));

    toSymbol.setBidding(this.parseDouble(toDoc, "#quote-summary td[class^=Ta] span[data-reactid^=53]"));
    toSymbol.setAsking(this.parseDouble(toDoc, "#quote-summary td[class^=Ta] span[data-reactid^=59]"));

    toSymbol.setVolume(this.parseInt(toDoc, "#quote-summary td[class^=Ta] span[data-reactid^=73]"));

    toSymbol.setAverageVolume(this.parseInt(toDoc, "#quote-summary td[class^=Ta] span[data-reactid^=79]"));

    toSymbol.setMarketCap(this.getHTML(toDoc, "#quote-summary td[class^=Ta] span[data-reactid^=88]"));

    toSymbol.setPriceEarnings(this.parseDouble(toDoc, "#quote-summary td[class^=Ta] span[data-reactid^=100]"));

    toSymbol.setEarningsPerShare(this.parseDouble(toDoc, "#quote-summary td[class^=Ta] span[data-reactid^=106]"));

    toSymbol.setDayRange(this.getHTML(toDoc, "#quote-summary td[data-reactid^=58] td"));
    toSymbol.setYearRange(this.getHTML(toDoc, "#quote-summary td[data-reactid^=62] td"));
    toSymbol.setDividendYield(this.getHTML(toDoc, "#quote-summary td[data-reactid^=109] td"));

    System.err.println(lcDescription);
    System.err.println(this.getHTML(toDoc, "#quote-summary span:contains(s Range)", "td", 1));
    System.err.println(this.getHTML(toDoc, "#quote-summary td[data-reactid^=62] td"));
    System.err.println(this.getHTML(toDoc, "#quote-summary td[data-reactid^=109] td"));

    toSymbol.setComments("From https://finance.yahoo.com/");

    Transaction loTransaction = null;
    try
    {
      loTransaction = toSession.beginTransaction();
      toSession.update(toSymbol);
      loTransaction.commit();

      llOkay = true;
    }
    catch (final Exception loErr)
    {
      final String lcMessage = String.format("There was an error with %s: %s %s", toSymbol.getSymbol(), loErr.getMessage(), loErr.getCause().toString());

      Misc.setStatusText(lcMessage, Constants.THREAD_ERROR_DISPLAY_DELAY);

      try
      {
        if (loTransaction != null)
        {
          loTransaction.rollback();

          // If you don't clear, then on subsequent commits, you will get the following error:
          //  HHH000010: On release of batch it still contained JDBC statements.
          toSession.clear();
        }
      }
      catch (final RuntimeException loRTErr)
      {
        final String lcRTMessage = String.format("There was a rollback error with %s: %s", toSymbol.getSymbol(), loRTErr.getMessage());

        Misc.setStatusText(lcRTMessage, Constants.THREAD_ERROR_DISPLAY_DELAY);
      }

    }

    return (llOkay);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private int parseInt(final Document toDoc, final String tcFirst)
  {
    int lnInteger;

    final String lcHTML = this.cleanForNumber(this.getHTML(toDoc, tcFirst));

    try
    {
      lnInteger = Integer.parseInt(lcHTML);
    }
    catch (final NumberFormatException loErr)
    {
      lnInteger = 0;
    }

    return (lnInteger);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private double parseDouble(final Document toDoc, final String tcFirst)
  {
    double lnDouble;

    final String lcHTML = this.cleanForNumber(this.getHTML(toDoc, tcFirst));

    try
    {
      lnDouble = Double.parseDouble(lcHTML);
    }
    catch (final NumberFormatException loErr)
    {
      lnDouble = 0;
    }

    return (lnDouble);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private String cleanForNumber(final String tcNumber)
  {
    String lcNumber = tcNumber.replaceAll(",", "");
    int lnPos = lcNumber.indexOf("x");
    if (lnPos >= 0)
    {
      lcNumber = lcNumber.substring(0, lnPos).trim();
    }

    return (lcNumber);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private String getHTML(final Document toDoc, final String tcFirst, final String tcSecond, final int tnSecond)
  {
    String lcHTML;

    try
    {
      if (tnSecond == 0)
      {
        lcHTML = toDoc.select(tcFirst).first().parent().select(tcSecond).first().html();
      }
      else
      {
        lcHTML = toDoc.select(tcFirst).first().parent().select(tcSecond).get(tnSecond).html();
      }

      // Get rid of all tags and any quotes.
      lcHTML = lcHTML.replaceAll("<[^>]*>", "").replaceAll("\"", "");

    }
    catch (final Exception loErr)
    {
      lcHTML = "";
    }

    return (lcHTML.trim());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private String getHTML(final Document toDoc, final String tcFirst)
  {
    String lcHTML;

    try
    {
      // Get rid of all tags and any quotes.
      lcHTML = toDoc.select(tcFirst).first().html().replaceAll("<[^>]*>", "").replaceAll("\"", "");
    }
    catch (final Exception loErr)
    {
      lcHTML = "";
    }

    return (lcHTML.trim());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // Also used by the data entry screen to display the URL for the stock symbol.
  public static String getSymbolDailyURL(final String tcSymbol)
  {
    return (String.format(Constants.YAHOO_DAILY_HTML, tcSymbol.trim()));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // Update the price, date and description.
  private boolean updateFinancialTable()
  {
    Misc.setStatusText("Updating the Financial table. . . .");

    boolean llOkay = false;

    final HibernateUtil loHibernate = HibernateUtil.INSTANCE;
    final Session loSession = loHibernate.getSession();

    final String lcSymbolTable = loHibernate.getTableSymbol();
    final String lcFinancialTable = loHibernate.getTableFinancial();

    try
    {
      final Transaction loTransaction = loSession.beginTransaction();

      // I had to use this method rather than using loSession.createNativeQuery.
      // Otherwise I was getting an invalid char '"' in statement error.
      loSession.doWork(toConnection -> {
        // Yea, complicated SQL statements. . . .
        final String lcPrice = String.format("UPDATE %s f SET Price = (SELECT s.LastTrade FROM %s s WHERE f.Symbol = s.Symbol) WHERE (f.symbol <> ' ') AND (f.symbol IN (SELECT symbol FROM %s))", lcFinancialTable, lcSymbolTable, lcSymbolTable);
        final String lcTradeTime = String.format("UPDATE %s f SET ValuationDate = (SELECT CAST(s.TradeTime AS date) FROM %s s WHERE f.Symbol = s.Symbol) WHERE (f.symbol <> ' ') AND (f.symbol IN (SELECT symbol FROM %s))", lcFinancialTable, lcSymbolTable, lcSymbolTable);
        final String lcDescription = String.format("UPDATE %s f SET Description = (SELECT s.Description FROM %s s WHERE f.Symbol = s.Symbol) WHERE (f.symbol <> ' ') AND (f.symbol IN (SELECT symbol FROM %s))", lcFinancialTable, lcSymbolTable, lcSymbolTable);

        Statement loStatement = null;
        try
        {
          loStatement = toConnection.createStatement();

          loStatement.addBatch(lcPrice);
          loStatement.addBatch(lcTradeTime);
          loStatement.addBatch(lcDescription);

          loStatement.executeBatch();
        }
        catch (final Exception loError)
        {
          final String lcMessage = String.format("There was an error in updating the Financial table with doWork: %s", loError.getMessage());
          Misc.setStatusText(lcMessage);
        }
        finally
        {
          if (null != loStatement)
          {
            loStatement.close();
          }
        }
      });

      /*
      From http://stackoverflow.com/questions/3220336/whats-the-use-of-session-flush-in-hibernate
      By default, Hibernate will flush changes automatically for you:
        - before some query executions
        - when a transaction is committed
      So loSession.flush() is not needed and now throws a "no transaction is in progress" if used.
       */
      loTransaction.commit();

      loSession.clear();

      llOkay = true;
    }
    catch (final Exception loError)
    {
      final String lcMessage = String.format("There was an error in updating financial information: %s", loError.getMessage());
      Misc.setStatusText(lcMessage);
    }

    loSession.close();

    return (llOkay);
  }

  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

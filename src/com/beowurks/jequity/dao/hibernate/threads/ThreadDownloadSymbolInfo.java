/*
 * JEquity
 * Copyright(c) 2008-2021, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */
package com.beowurks.jequity.dao.hibernate.threads;

import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.hibernate.SymbolEntity;
import com.beowurks.jequity.dao.web.PageScraping;
import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.AppProperties;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.sql.Date;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class ThreadDownloadSymbolInfo extends ThreadDownloadHTML implements Runnable
{

  public static final ThreadDownloadSymbolInfo INSTANCE = new ThreadDownloadSymbolInfo();

  private Timer foTimer = null;
  private Instant fiTimerStart;

  // I had some issues with setting the status message from both the timer and this thread.
  // Main.getController().getStatusMessage().getText() did not appear to always have the
  // latest and greatest value. So I just track the value using this.fcTimerMessage.
  // And I decided to use String instead of the thread-safe StringBuffer as I would constantly calling
  // StringBuffer.toString and setLength(0).
  private String fcTimerMessage;

  // ---------------------------------------------------------------------------------------------------------------------
  private ThreadDownloadSymbolInfo()
  {
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public boolean start(final boolean tlDisplayDialogMessage)
  {
    this.flDisplayDialogMessage = tlDisplayDialogMessage;

    if ((this.foThread != null) && (this.foThread.isAlive()))
    {
      if (this.flDisplayDialogMessage)
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
    this.fcErrorMessage = this.updateSymbolTable();
    // If no error message has been returned.
    if (this.fcErrorMessage.isEmpty())
    {
      this.updateAllSymbolInformation();
      this.updateFinancialTable();

      Main.getController().refreshAllComponents(false);
      return;
    }

    if (this.flDisplayDialogMessage)
    {
      Misc.errorMessage(this.fcErrorMessage);
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // The basic idea behind this routine is to add any symbols that are in
  // the Financial table and remove any symbols that no longer exist.
  private String updateSymbolTable()
  {
    Misc.setStatusText("First updating codes in the Symbol table. . . .");

    final StringBuilder lcMessage = new StringBuilder();

    final HibernateUtil loHibernate = HibernateUtil.INSTANCE;
    final Session loSession = loHibernate.getSession();

    final String lcSymbolTable = loHibernate.getTableSymbol();
    final String lcFinancialTable = loHibernate.getTableFinancial();

    // I had to use this method rather than using loSession.createNativeQuery.
    // Otherwise I was getting an invalid char '"' in statement error.
    loSession.doWork(toConnection ->
    {
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
    this.startTimer();

    final HibernateUtil loHibernate = HibernateUtil.INSTANCE;
    final Session loSession = loHibernate.getSession();

    final String lcSQL = String.format("SELECT * FROM %s ORDER BY symbol", loHibernate.getTableSymbol());
    final NativeQuery loQuery = loSession.createNativeQuery(lcSQL)
      .addEntity(SymbolEntity.class);

    final List<SymbolEntity> loList = loQuery.list();

    final int lnTotal = loList.size();

    // Must be initialized each time.
    this.updateStatusText("Downloading. . . .", 0.0);

    for (final SymbolEntity loSymbol : loList)
    {
      final String lcSymbol = loSymbol.getSymbol().trim();

      final String lcDailyURL = PageScraping.INSTANCE.getDailyStockURL(lcSymbol);

      this.updateStatusText(String.format("Downloading information for the symbol of %s . . . .", lcSymbol));

      // Daily Information
      Document loDoc = null;
      for (int lnTries = 0; (lnTries < Constants.JSOUP_TIMEOUT_TRIES) && (loDoc == null); ++lnTries)
      {
        try
        {
          // Highly recommended to set the userAgent.
          loDoc = Jsoup.connect(lcDailyURL)
            .followRedirects(false)
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
        final String lcMessage = String.format("Unable to read the page of %s. Make sure that the stock symbol, %s, is still valid.", lcDailyURL, lcSymbol);
        this.updateStatusText(lcMessage, Constants.THREAD_ERROR_DISPLAY_DELAY);

        continue;
      }

      this.updateStatusText(String.format("Successfully read %s daily information", lcSymbol));

      final long lnDelay = AppProperties.INSTANCE.getUpdateIntervalKey() * 1000L;
      if (this.importDailyInformation(loSession, loSymbol, loDoc))
      {
        this.updateStatusText(String.format("Successfully imported %s's daily information. Now waiting %.1f seconds.", lcSymbol, lnDelay / 1000.0));
      }

      this.updateStatusText((double) loList.indexOf(loSymbol) / (double) lnTotal);

      try
      {
        Thread.sleep(lnDelay);
      }
      catch (final InterruptedException loErr)
      {
        loErr.printStackTrace();
      }
    }

    loSession.close();

    this.updateStatusText("Daily information imported. . . .", 0.0);

    if (this.foTimer != null)
    {
      this.foTimer.cancel();
    }

  }


  // ---------------------------------------------------------------------------------------------------------------------
  private void startTimer()
  {
    if (this.foTimer != null)
    {
      this.foTimer.cancel();
    }

    this.fiTimerStart = Instant.now();
    this.fcTimerMessage = "";
    // From
    // http://stackoverflow.com/questions/1041675/java-timer
    // and
    // http://stackoverflow.com/questions/10335784/restart-timer-in-java
    this.foTimer = new Timer();
    this.foTimer.schedule(
      new TimerTask()
      {
        @Override
        public void run()
        {
          ThreadDownloadSymbolInfo.this.updateStatusText();
        }
      }, 0L, 1000L);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void updateStatusText(final String tcMessage, final double tnProgress)
  {
    this.fcTimerMessage = this.updateDuration(tcMessage);

    Misc.setStatusText(this.fcTimerMessage, tnProgress);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void updateStatusText(final String tcMessage)
  {
    this.fcTimerMessage = this.updateDuration(tcMessage);

    Misc.setStatusText(this.fcTimerMessage);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void updateStatusText(final double tnProgress)
  {
    this.fcTimerMessage = this.updateDuration(this.fcTimerMessage);

    Misc.setStatusText(this.fcTimerMessage, tnProgress);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void updateStatusText()
  {
    this.fcTimerMessage = this.updateDuration(this.fcTimerMessage);

    Misc.setStatusText(this.fcTimerMessage);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private String updateDuration(final String tcStatusText)
  {
    final Duration loDuration = Duration.between(this.fiTimerStart, Instant.now());
    final String lcStatus;

    final String lcTimePassed = String.format("[%02d:%02d]", loDuration.toMinutes(), loDuration.toSecondsPart());
    if (tcStatusText.contains("[") && tcStatusText.contains("]"))
    {
      lcStatus = tcStatusText.replaceAll("\\[(.*?)\\]", lcTimePassed);
    }
    else
    {
      lcStatus = String.format("%s %s", lcTimePassed, tcStatusText);
    }

    return (lcStatus);
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
    final String lcSymbol = toSymbol.getSymbol();

    this.refreshCurrentTextList(toDoc, lcSymbol);

    final String lcDescription = this.getDescriptionFromHtml(toDoc);

    toSymbol.setDescription(lcDescription);

    double lnLastTrade = this.parseDouble(toDoc, PageScraping.INSTANCE.getLastTradeMarker());

    if (lnLastTrade == 0.0)
    {
      // Some symbols, like FDRXX, don't have a last trade field. So in that case,
      // default to 1.0.
      final String lcLastTrade = this.getHTML(toDoc, PageScraping.INSTANCE.getLastTradeMarker());
      if (lcLastTrade.isEmpty())
      {
        lnLastTrade = 1.0;
      }
    }

    toSymbol.setLastTrade(lnLastTrade);

    final Date loDate = new Date(Calendar.getInstance().getTimeInMillis());
    final Timestamp loTimestamp = new Timestamp(loDate.getTime());
    toSymbol.setTradeTime(loTimestamp);

    toSymbol.setComments(String.format("Source reference: %s", PageScraping.INSTANCE.getDailyStockURL(lcSymbol)));

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
      final String lcMessage = String.format("There was an error with %s: %s %s", lcSymbol, loErr.getMessage(), loErr.getCause().toString());

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
        final String lcRTMessage = String.format("There was a rollback error with %s: %s", lcSymbol, loRTErr.getMessage());

        Misc.setStatusText(lcRTMessage, Constants.THREAD_ERROR_DISPLAY_DELAY);
      }

    }

    return (llOkay);
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
      loSession.doWork(toConnection ->
      {
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

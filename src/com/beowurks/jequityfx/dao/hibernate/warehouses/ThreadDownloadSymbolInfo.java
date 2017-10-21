/*
 * J'EquityFX
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */
package com.beowurks.jequityfx.dao.hibernate.warehouses;

import com.beowurks.jequityfx.dao.hibernate.HibernateUtil;
import com.beowurks.jequityfx.dao.hibernate.SymbolEntity;
import com.beowurks.jequityfx.utility.Constants;
import com.beowurks.jequityfx.utility.Misc;
import com.beowurks.jequityfx.view.ProgressHandle;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.jdbc.Work;
import org.hibernate.query.NativeQuery;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
public class ThreadDownloadSymbolInfo implements Runnable
{

  public static ThreadDownloadSymbolInfo INSTANCE = new ThreadDownloadSymbolInfo();

  private Thread foThread = null;

  // -----------------------------------------------------------------------------
  private ThreadDownloadSymbolInfo()
  {
  }

  // -----------------------------------------------------------------------------
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

  // -----------------------------------------------------------------------------
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

  // -----------------------------------------------------------------------------
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
    loSession.doWork(new Work()
    {
      @Override
      public void execute(final Connection toConnection) throws SQLException
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
      }

    });

    loSession.close();

    return (lcMessage.toString());
  }

  // ---------------------------------------------------------------------------
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
    final ProgressHandle loProgressHandle = ProgressHandle.createHandle("Downloading");

    loProgressHandle.start(lnTotal);

    for (final SymbolEntity loSymbol : loList)
    {
      final String lcSymbol = loSymbol.getSymbol().trim();

      Misc.setStatusText(String.format("Downloading information for the symbol of %s . . . .", lcSymbol));

      final String lcSymbolFile = this.buildSymbolLocalFile(lcSymbol);
      IOException loIOException = null;
      try
      {
        // If you don't use the multiple parameters of the URL constructor, then an unknown protocol
        // error will be thrown because of the c parameter.
        final URL loURL = new URL(Constants.YAHOO_SYMBOL_PROTOCOL,
                Constants.YAHOO_SYMBOL_HOST,
                Constants.YAHOO_SYMBOL_PORT,
                this.buildSymbolHTMLFile(lcSymbol));

        final File loFile = new File(lcSymbolFile);

        FileUtils.copyURLToFile(loURL, loFile, 3000, 3000);
      }
      catch (final IOException loErr)
      {
        loIOException = loErr;
      }

      if (loIOException != null)
      {
        final String lcMessage = String.format("Unable to read the CSV for %s. %s", lcSymbolFile, loIOException.getMessage());
        Misc.setStatusText(lcMessage);
      }
      else
      {
        Misc.setStatusText(String.format("Successfully read %s daily information", lcSymbol));
      }

      if (this.importDailyInformation(loSession, loSymbol, lcSymbolFile))
      {
        Misc.setStatusText(String.format("Successfully imported %s's daily information", lcSymbol));
      }

      loProgressHandle.progress(loList.indexOf(loSymbol));
    }

    loSession.close();

    loProgressHandle.finish();

  }

  // -----------------------------------------------------------------------------
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
      loSession.doWork(new Work()
      {
        @Override
        public void execute(final Connection toConnection) throws SQLException
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

  // ---------------------------------------------------------------------------
  // Discovered a new way to import daily information rather than scrape the screen. From the following:
  // http://stackoverflow.com/questions/6308950/capture-yahoo-finance-stock-data-symbols-for-daily-break-out-leaders-etc
  // http://www.jarloo.com/yahoo_finance/
  // http://code.google.com/p/yahoo-finance-managed/wiki/enumQuoteProperty
  private boolean importDailyInformation(final Session toSession, final SymbolEntity toSymbol, final String tcFilename)
  {
    LineIterator loLines = null;
    final String lcLine;
    String[] laElements = null;
    Exception loException = null;

    try
    {
      loLines = FileUtils.lineIterator(new File(tcFilename), "UTF-8");

      if (!loLines.hasNext())
      {
        loException = new Exception(String.format("No lines in %s", tcFilename));
      }
      lcLine = loLines.nextLine();
      // From http://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes
      laElements = lcLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
    }
    catch (final IOException loErr)
    {
      loException = loErr;
    }
    finally
    {
      if (loLines != null)
      {
        LineIterator.closeQuietly(loLines);
      }
    }

    if (loException != null)
    {
      return (false);
    }

    boolean llOkay = false;

    String lcDescription = this.stripQuotes(laElements[1]);
    if (lcDescription.isEmpty())
    {
      lcDescription = Constants.UNKNOWN_STOCK_SYMBOL;
    }

    lcDescription = lcDescription.replaceAll("&amp;", "&").trim();
    toSymbol.setDescription(lcDescription);

    double lnLastTrade = this.parseDouble(laElements[2]);

    if (lnLastTrade == 0.0)
    {
      // Some symbols, like FDRXX, don't have a last trade field. So in that case,
      // default to 1.0.
      lnLastTrade = 1.0;
    }

    toSymbol.setLastTrade(lnLastTrade);

    final java.util.Date loDate = new java.util.Date();
    final Timestamp loTimestamp = new Timestamp(loDate.getTime());
    toSymbol.setTradeTime(loTimestamp);

    final double lnPrevClose = this.parseDouble(laElements[3]);
    toSymbol.setPreviousClose(lnPrevClose);

    toSymbol.setDifferential((lnPrevClose != 0.0) ? ((lnLastTrade - lnPrevClose) / lnPrevClose) * 100.0 : 0.0);

    toSymbol.setOpened(this.parseDouble(laElements[4]));
    toSymbol.setTargetEstimate(this.parseDouble(laElements[5]));
    toSymbol.setBidding(this.parseDouble(laElements[6]));
    toSymbol.setAsking(this.parseDouble(laElements[7]));
    toSymbol.setDayRange(this.stripQuotes(laElements[8]));
    toSymbol.setYearRange(this.stripQuotes(laElements[9]));
    toSymbol.setVolume(this.parseInt(laElements[10]));

    toSymbol.setAverageVolume(this.parseInt(laElements[11]));

    toSymbol.setMarketCap(this.stripQuotes(laElements[12]));

    toSymbol.setPriceEarnings(this.parseDouble(laElements[13]));

    toSymbol.setEarningsPerShare(this.parseDouble(laElements[14]));

    toSymbol.setDividendYield(this.stripQuotes(laElements[12]));

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

      Misc.setStatusText(lcMessage);

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

        Misc.setStatusText(lcRTMessage);
      }

    }

    return (llOkay);
  }

  // ---------------------------------------------------------------------------
  private int parseInt(final String tcValue)
  {
    int lnInteger;
    final String lcValue = this.stripQuotes(tcValue);

    try
    {
      lnInteger = Integer.parseInt(lcValue);
    }
    catch (final NumberFormatException loErr)
    {
      lnInteger = 0;
    }

    return (lnInteger);
  }

  // ---------------------------------------------------------------------------
  private double parseDouble(final String tcValue)
  {
    double lnDouble;
    final String lcValue = this.stripQuotes(tcValue);

    try
    {
      lnDouble = Double.parseDouble(lcValue);
    }
    catch (final NumberFormatException loErr)
    {
      lnDouble = 0;
    }

    return (lnDouble);
  }

  // ---------------------------------------------------------------------------
  private String stripQuotes(final String tcValue)
  {
    return (tcValue.replaceAll("\"", ""));
  }

  // ---------------------------------------------------------------------------
  public static String getSymbolDailyURL(final String tcSymbol)
  {
    return (Constants.YAHOO_DAILY_HTML + tcSymbol);
  }

  // -----------------------------------------------------------------------------
  private String buildSymbolHTMLFile(final String tcSymbol)
  {
    final StringBuilder loString = new StringBuilder(Constants.YAHOO_SYMBOL_FILE);

    Misc.replaceAll(loString, Constants.YAHOO_SYMBOL, tcSymbol);

    final int lnCount = Constants.YAHOO_CODES.length;
    for (int i = 0; i < lnCount; ++i)
    {
      loString.append(Constants.YAHOO_CODES[i][0]);
    }
    return (loString.toString());
  }

  // -----------------------------------------------------------------------------
  private String buildSymbolLocalFile(final String tcSymbol)
  {
    return (Misc.includeTrailingBackslash(Constants.TEMPORARY_STOCK_PATH) + tcSymbol + "SymbolQuote.csv");
  }

  // -----------------------------------------------------------------------------
}
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------

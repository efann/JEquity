/*
 * JEquity
 * Copyright(c) 2008-2018, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */
package com.beowurks.jequity.dao.hibernate.threads;

import com.beowurks.jequity.dao.IDefaultXYChartSeries;
import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import javax.swing.SwingUtilities;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;

// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
public class ThreadDownloadHistorical implements Runnable
{

  public static ThreadDownloadHistorical INSTANCE = new ThreadDownloadHistorical();

  private Thread foThread = null;

  private String fcHistoricalFile;
  private String fcSymbol;
  private String fcDescription;
  private java.util.Date fdStartDate;
  private java.util.Date fdEndDate;
  private XYChart.Series[] faXYDataSeries;
  private LineChart<java.util.Date, Number> chtLineChart;

  private final ArrayList<double[]> foDoubleList = new ArrayList<>();

  private boolean flProccessRunning = false;

  // -----------------------------------------------------------------------------
  private ThreadDownloadHistorical()
  {
  }

  // -----------------------------------------------------------------------------
  public boolean start(final boolean tlDisplayMessage, final IDefaultXYChartSeries toHistoricalPanel)
  {
    if ((this.flProccessRunning) && (this.foThread != null) && (this.foThread.isAlive()))
    {
      if (tlDisplayMessage)
      {
        SwingUtilities.invokeLater(() ->
            Misc.errorMessage("The historical information is currently being updated. . . ."));
      }
      return (false);
    }

    // As we're unable to determine if Platform thread has ended (it's always there as
    // we set Platform.setImplicitExit(false); in the Installer), we use flProccessRunning also.
    this.flProccessRunning = true;

    this.chtLineChart = toHistoricalPanel.getLineChart();
    this.faXYDataSeries = toHistoricalPanel.getXYDataSeries();
    this.fcSymbol = toHistoricalPanel.getSymbol();
    this.fdStartDate = toHistoricalPanel.getStartDate();
    this.fdEndDate = toHistoricalPanel.getEndDate();
    this.fcDescription = toHistoricalPanel.getDescription();

    this.foThread = new Thread(this);
    this.foThread.setPriority(Thread.NORM_PRIORITY);
    this.foThread.start();

    return (true);
  }

  // -----------------------------------------------------------------------------
  @Override
  public void run()
  {
    if (this.downloadHistoricalFile())
    {
      Platform.runLater(() ->
      {
        this.resetDataset();
        this.importHistoricalInformation();

        this.flProccessRunning = false;
      });

    }
  }

  // ---------------------------------------------------------------------------
  private boolean importHistoricalInformation()
  {
    // The index out of range error is not being thrown here.
    if (!this.refreshNumberList())
    {
      return (false);
    }

    this.chtLineChart.setTitle(String.format("%s (%s)", this.fcDescription, this.fcSymbol));

    this.refreshDataSeries();

    return (true);
  }

  // ---------------------------------------------------------------------------
  private void resetDataset()
  {
    Misc.setStatusText("Resetting the XY data series. . . .");

    final int lnCount = this.faXYDataSeries.length;
    for (int i = lnCount - 1; i >= 0; --i)
    {
      this.faXYDataSeries[i].getData().clear();
    }

  }

  // ---------------------------------------------------------------------------
  private boolean refreshNumberList()
  {
    Misc.setStatusText("Refreshing the number list. . . .");

    LineIterator loLines = null;
    Exception loException = null;
    try
    {
      this.foDoubleList.clear();

      boolean llFirst = true;
      loLines = FileUtils.lineIterator(new File(this.fcHistoricalFile), "UTF-8");
      int lnElements = 0;

      while (loLines.hasNext())
      {
        final String lcLine = loLines.nextLine();
        final String[] laElements = lcLine.split(",");

        if (llFirst)
        {
          llFirst = false;
          lnElements = laElements.length;
        }
        else
        {
          final int lnLength = laElements.length;
          if (lnLength != lnElements)
          {
            throw new IOException(String.format("%s read error: line on has %d elements.", Main.getApplicationName(), lnLength));
          }

          final double[] laDouble = new double[lnLength];
          for (int i = 0; i < lnLength; ++i)
          {
            if (i != 0)
            {
              try
              {
                laDouble[i] = Double.parseDouble(laElements[i]);
              }
              catch (final NumberFormatException loErr)
              {
                laDouble[i] = 0.0;
              }
            }
            else
            {
              final DateFormat loDateFormat = new SimpleDateFormat("yyyy-MM-dd");
              try
              {
                final java.util.Date loDate = loDateFormat.parse(laElements[i]);
                // getTime returns the number of milliseconds since January 1, 1970, 00:00:00 GMT represented by this Date object.
                laDouble[i] = loDate.getTime();
              }
              catch (final ParseException loErr)
              {
                laDouble[i] = 0L;
              }
            }
          }
          this.foDoubleList.add(laDouble);
        }
      }

      this.foDoubleList.sort(new DoubleComparator());
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

    return (loException == null);

  }

  // ---------------------------------------------------------------------------
  private void refreshDataSeries()
  {
    Misc.setStatusText("Refreshing the data series. . . .");
    /*
     Date,Open,High,Low,Close,Volume,Adj Close
     2015-03-11,5.47,5.47,5.47,5.47,000,5.47
     2015-03-10,5.47,5.47,5.47,5.47,000,5.47
     2015-03-09,5.56,5.56,5.56,5.56,000,5.56
     2015-03-06,5.53,5.53,5.53,5.53,000,5.53
     */

    final Iterator loDoubleLines = this.foDoubleList.iterator();

    while (loDoubleLines.hasNext())
    {
      final double[] laDouble = (double[]) loDoubleLines.next();

      final java.util.Date ldDate = new java.util.Date((long) laDouble[0]);

      this.faXYDataSeries[0].getData().add(new XYChart.Data(ldDate, laDouble[1]));
      this.faXYDataSeries[1].getData().add(new XYChart.Data(ldDate, laDouble[2]));
      this.faXYDataSeries[2].getData().add(new XYChart.Data(ldDate, laDouble[3]));
      this.faXYDataSeries[3].getData().add(new XYChart.Data(ldDate, laDouble[4]));
      // Skipping Volume
      this.faXYDataSeries[4].getData().add(new XYChart.Data(ldDate, laDouble[6]));

    }

  }

  // ---------------------------------------------------------------------------
  private boolean downloadHistoricalFile()
  {
    Misc.setStatusText("Downloading the historical data. . . .");

    final String lcSymbol = this.fcSymbol;

    this.fcHistoricalFile = this.buildSymbolHistoricalLocalFile(lcSymbol);

    Misc.setStatusText(String.format("Downloading information for the symbol of %s . . . .", lcSymbol));

    // Historical Information
    IOException loIOException = null;
    try
    {
      // If you don't use the multiple parameters of the URL constructor, then an unknown protocol
      // error will be thrown because of the c parameter.
      final URL loURL = new URL(Constants.YAHOO_HISTORICAL_PROTOCOL,
          Constants.YAHOO_HISTORICAL_HOST,
          Constants.YAHOO_HISTORICAL_PORT,
          this.buildSymbolHistoricalHTMLFile(lcSymbol, this.fdStartDate, this.fdEndDate));

      final File loFile = new File(this.fcHistoricalFile);

      FileUtils.copyURLToFile(loURL, loFile, 3000, 3000);
    }
    catch (final IOException loErr)
    {
      loIOException = loErr;
    }

    if (loIOException != null)
    {
      final String lcMessage = String.format("Unable to read the page for %s. %s", this.fcSymbol, loIOException.getMessage());
      Misc.setStatusText(lcMessage);
      return (false);
    }
    else
    {
      Misc.setStatusText("Successfully read " + lcSymbol + " historical information");
    }

    return (true);
  }

  // -----------------------------------------------------------------------------
  // Yahoo's formatting for the month uses 0 to 11 rather than 1 to 12.
  // Not the case, though, for days and years.
  private String buildSymbolHistoricalHTMLFile(final String tcSymbol, final java.util.Date tdStartDate, final java.util.Date tdEndDate)
  {
    final Calendar loCalendar = Calendar.getInstance();

    final StringBuilder loString = new StringBuilder(Constants.YAHOO_HISTORICAL_FILE);

    loCalendar.setTime(tdStartDate);

    Misc.replaceAll(loString, Constants.YAHOO_SYMBOL, tcSymbol);
    Misc.replaceAll(loString, Constants.YAHOO_STARTDAY, String.valueOf(loCalendar.get(Calendar.DAY_OF_MONTH)));
    // By the way, Calendar always reports its months from 0 to 11 but days go
    // from 1 to 31,
    // just the way Yahoo expects.
    Misc.replaceAll(loString, Constants.YAHOO_STARTMONTH, String.valueOf(loCalendar.get(Calendar.MONTH)));
    Misc.replaceAll(loString, Constants.YAHOO_STARTYEAR, String.valueOf(loCalendar.get(Calendar.YEAR)));

    // ---
    loCalendar.setTime(tdEndDate);

    Misc.replaceAll(loString, Constants.YAHOO_ENDDAY,
        String.valueOf(loCalendar.get(Calendar.DAY_OF_MONTH)));
    // By the way, Calendar always reports its months from 0 to 11 but days go
    // from 1 to 31,
    // just the way Yahoo expects.
    Misc.replaceAll(loString, Constants.YAHOO_ENDMONTH, String.valueOf(loCalendar.get(Calendar.MONTH)));
    Misc.replaceAll(loString, Constants.YAHOO_ENDYEAR, String.valueOf(loCalendar.get(Calendar.YEAR)));

    return (loString.toString());
  }

  // -----------------------------------------------------------------------------
  private String buildSymbolHistoricalLocalFile(final String tcSymbol)
  {
    return (Misc.includeTrailingBackslash(Constants.TEMPORARY_STOCK_PATH) + tcSymbol + "Historical.csv");
  }

  // -----------------------------------------------------------------------------
  // -----------------------------------------------------------------------------
  // -----------------------------------------------------------------------------
  private class DoubleComparator implements Comparator<double[]>
  {

    // -----------------------------------------------------------------------------
    @Override
    public int compare(final double[] toValue1, final double[] toValue2)
    {
      final Double loValue1 = toValue1[0];
      final Double loValue2 = toValue2[0];

      return (loValue1.compareTo(loValue2));

    }
    // -----------------------------------------------------------------------------
  }
  // -----------------------------------------------------------------------------
  // -----------------------------------------------------------------------------
  // -----------------------------------------------------------------------------

}
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------

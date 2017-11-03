/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */
package com.beowurks.jequity.dao.hibernate.warehouses;

import com.beowurks.jequity.utility.AppProperties;
import com.beowurks.jequity.utility.Constants;

import java.util.Timer;
import java.util.TimerTask;

// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// From http://stackoverflow.com/questions/32001/resettable-java-timer
public class TimerSymbolInfo
{

  public static TimerSymbolInfo INSTANCE = new TimerSymbolInfo();

  private Timer foTimer = null;

  // -----------------------------------------------------------------------------
  private TimerSymbolInfo()
  {
  }

  // -----------------------------------------------------------------------------
  public void reSchedule()
  {
    if (this.foTimer != null)
    {
      this.foTimer.cancel();
    }

    final int lnDelay = AppProperties.INSTANCE.getDailyIntervalKey();
    if (lnDelay == Constants.DAILY_INTERVAL_NEVER)
    {
      return;
    }

    int lnStart = AppProperties.INSTANCE.getDailyStartKey();
    if (lnStart == Constants.DAILY_INTERVAL_MATCH)
    {
      lnStart = lnDelay;
    }

    // From
    // http://stackoverflow.com/questions/1041675/java-timer
    // and
    // http://stackoverflow.com/questions/10335784/restart-timer-in-java
    this.foTimer = new Timer();
    this.foTimer.scheduleAtFixedRate(
        new TimerTask()
        {
          @Override
          public void run()
          {
            ThreadDownloadSymbolInfo.INSTANCE.start(false);
          }
        }, lnStart, lnDelay);
  }

  // -----------------------------------------------------------------------------
}
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------

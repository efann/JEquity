/*
 * JEquity
 * Copyright(c) 2008-2020, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.utility;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

import com.beowurks.jequity.dao.hibernate.threads.JSONDataElements;
import com.beowurks.jequity.dao.hibernate.threads.ThreadDownloadHistorical;
import com.beowurks.jequity.view.checkbox.CheckBoxPlus;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.ArrayList;

public class Calculations
{
  public static final Calculations INSTANCE = new Calculations();

  private final SimpleRegression foSimpleRegression = new SimpleRegression();

  // ---------------------------------------------------------------------------------------------------------------------
  private Calculations()
  {
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void refreshDataPoints(final ThreadDownloadHistorical toThreadDownloadHistorical)
  {
    final ArrayList<JSONDataElements> loJSONDateRangeList = toThreadDownloadHistorical.getJSONDateRangeList();
    final CheckBoxPlus[] laCheckBoxPlus = toThreadDownloadHistorical.getTabHistoricalGraphController().getCheckBoxesForSeriesVisibility();

    this.foSimpleRegression.clear();

    // 1-based index.
    int lnIndex = 1;
    for (final JSONDataElements loElement : loJSONDateRangeList)
    {
      double lnValue = 0.0;
      int lnDivisor = 0;

      final int lnCheckBoxesLength = laCheckBoxPlus.length;
      for (int i = 0; i < lnCheckBoxesLength; ++i)
      {
        if (laCheckBoxPlus[i].isSelected())
        {
          ++lnDivisor;
          lnValue += loElement.faNumbers[i];
        }
      }

      this.foSimpleRegression.addData(lnIndex, (lnDivisor != 0) ? (lnValue / (double) lnDivisor) : lnValue);
      ++lnIndex;
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  // y = intercept + slope * x
  public double getYValueRegression(final double tnXValue)
  {
    return (this.foSimpleRegression.getIntercept() + (this.foSimpleRegression.getSlope() * tnXValue));
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

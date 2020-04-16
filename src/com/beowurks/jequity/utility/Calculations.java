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
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.ArrayList;

public class Calculations
{
  public static final Calculations INSTANCE = new Calculations();

  private final SimpleRegression foSimpleRegression = new SimpleRegression();

  private final FastFourierTransformer foFFTransformer = new FastFourierTransformer(DftNormalization.STANDARD);

  private Complex[] foComplex;

  // ---------------------------------------------------------------------------------------------------------------------
  private Calculations()
  {
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void refreshRegression(final ThreadDownloadHistorical toThreadDownloadHistorical)
  {
    this.foSimpleRegression.clear();

    if (toThreadDownloadHistorical.getTabHistoricalGraphController() == null)
    {
      return;
    }

    final ArrayList<JSONDataElements> loJSONDateRangeList = toThreadDownloadHistorical.getJSONDateRangeList();
    final CheckBoxPlus[] laCheckBoxPlus = toThreadDownloadHistorical.getTabHistoricalGraphController().getCheckBoxesForSeriesVisibility();

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
  public void refreshFFT(final ThreadDownloadHistorical toThreadDownloadHistorical)
  {
    if (toThreadDownloadHistorical.getTabHistoricalGraphController() == null)
    {
      return;
    }

    final ArrayList<JSONDataElements> loJSONDateRangeList = toThreadDownloadHistorical.getJSONDateRangeList();
    final CheckBoxPlus[] laCheckBoxPlus = toThreadDownloadHistorical.getTabHistoricalGraphController().getCheckBoxesForSeriesVisibility();

    final int lnPowerOfTwo = this.getPowerOfTwo(loJSONDateRangeList.size());
    final double[] laFFTData = new double[lnPowerOfTwo];

    for (int i = 0; i < lnPowerOfTwo; ++i)
    {
      laFFTData[i] = 0.0;
    }

    // 0-based index.
    int lnIndex = 0;
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

      laFFTData[lnIndex++] = (lnDivisor != 0) ? (lnValue / (double) lnDivisor) : lnValue;
    }

    this.foComplex = this.foFFTransformer.transform(laFFTData, TransformType.FORWARD);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // y = intercept + slope * x
  // 1-based
  public double getYValueRegression(final double tnXValue)
  {
    return (this.foSimpleRegression.getIntercept() + (this.foSimpleRegression.getSlope() * tnXValue));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // 0-based.
  public double getYValueFFT(final int tnXValue)
  {
    final int lnLimit = this.foComplex.length;
    int lnIndex = tnXValue;
    while (lnIndex >= lnLimit)
    {
      lnIndex -= lnLimit;
    }

    return (this.foComplex[lnIndex].getReal());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // From https://stackoverflow.com/questions/5242533/fast-way-to-find-exponent-of-nearest-superior-power-of-2
  private int getPowerOfTwo(final int tnValue)
  {
    int lnPowerOfTwo = tnValue;
    if (lnPowerOfTwo != Integer.highestOneBit(lnPowerOfTwo))
    {
      lnPowerOfTwo = Integer.highestOneBit(lnPowerOfTwo) * 2;
    }

    return (lnPowerOfTwo);
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

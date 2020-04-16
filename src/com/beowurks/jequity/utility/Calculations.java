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

  private double[] faAvgValues;

  private double fnRealFactor;

  // ---------------------------------------------------------------------------------------------------------------------
  private Calculations()
  {
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void refreshAll(final ThreadDownloadHistorical toThreadDownloadHistorical)
  {
    this.foSimpleRegression.clear();

    if (toThreadDownloadHistorical.getTabHistoricalGraphController() == null)
    {
      return;
    }

    this.resetAverageArray(toThreadDownloadHistorical.getJSONDateRangeList(), toThreadDownloadHistorical.getTabHistoricalGraphController().getCheckBoxesForSeriesVisibility());

    this.refreshRegression();
    this.refreshFFT();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void resetAverageArray(final ArrayList<JSONDataElements> toJSONDateRangeList, final CheckBoxPlus[] taCheckBoxPlus)
  {
    this.faAvgValues = new double[toJSONDateRangeList.size()];

    int lnIndex = 0;
    for (final JSONDataElements loElement : toJSONDateRangeList)
    {
      double lnValue = 0.0;
      int lnDivisor = 0;

      final int lnCheckBoxesLength = taCheckBoxPlus.length;
      for (int i = 0; i < lnCheckBoxesLength; ++i)
      {
        if (taCheckBoxPlus[i].isSelected())
        {
          ++lnDivisor;
          lnValue += loElement.faNumbers[i];
        }
      }

      this.faAvgValues[lnIndex++] = (lnDivisor != 0) ? (lnValue / (double) lnDivisor) : 0.0;
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void refreshRegression()
  {
    final int lnLength = this.faAvgValues.length;
    for (int i = 0; i < lnLength; ++i)
    {
      // 1-based index.
      this.foSimpleRegression.addData(i + 1, this.faAvgValues[i]);
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void refreshFFT()
  {
    final int lnPowerOfTwo = this.nextPowerOfTwo(this.faAvgValues.length);

    final double[] laFFTData = new double[lnPowerOfTwo];

    this.fnRealFactor = 2.0 / (double)lnPowerOfTwo;
    for (int i = 0; i < lnPowerOfTwo; ++i)
    {
      laFFTData[i] = 0.0;
    }

    final int lnLength = this.faAvgValues.length;
    for (int i = 0; i < lnLength; ++i)
    {
      final double lnValue = 0.0;
      final int lnDivisor = 0;

      // 0-based index.
      laFFTData[i] = this.faAvgValues[i];
    }

    this.foComplex = this.foFFTransformer.transform(laFFTData, TransformType.INVERSE);
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

    final double lnReal = (this.foComplex[lnIndex].getReal());

    return (lnReal * this.fnRealFactor);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // From https://stackoverflow.com/questions/5242533/fast-way-to-find-exponent-of-nearest-superior-power-of-2
  private int nextPowerOfTwo(final int tnValue)
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

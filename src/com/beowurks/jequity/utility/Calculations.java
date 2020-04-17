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
import java.util.Arrays;

public class Calculations
{
  public static final Calculations INSTANCE = new Calculations();

  private final SimpleRegression foSimpleRegression = new SimpleRegression();

  private final FastFourierTransformer foFFTransformer = new FastFourierTransformer(DftNormalization.STANDARD);

  private Complex[] foComplex;

  private double[] faAvgValues;

  // Yes, I want this as an integer so that I can compare to 0.
  private int fnSmoothing;

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

    this.fnSmoothing = toThreadDownloadHistorical.getTabHistoricalGraphController().getSmoothingValue();

    this.resetAverageArray(toThreadDownloadHistorical.getJSONDateRangeList(), toThreadDownloadHistorical.getTabHistoricalGraphController().getCheckBoxesForSeriesVisibility());

    this.refreshRegression();
    this.refreshFFT();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // y = intercept + slope * x
  public double getYValueRegression(final double tnXValue)
  {
    return (this.foSimpleRegression.getIntercept() + (this.foSimpleRegression.getSlope() * tnXValue));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // 0-based.
  public double getYValueFFT(final int tnXValue)
  {
    final int lnIndex = tnXValue % this.faAvgValues.length;

    return (this.foComplex[lnIndex].getReal());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // 0-based.
  public double getYValueAverage(final int tnXValue)
  {
    return (this.faAvgValues[tnXValue]);
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

      this.faAvgValues[lnIndex] = (lnDivisor != 0) ? (lnValue / (double) lnDivisor) : 0.0;
      lnIndex++;
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void refreshRegression()
  {
    final int lnLength = this.faAvgValues.length;
    for (int i = 0; i < lnLength; ++i)
    {
      this.foSimpleRegression.addData(i, this.faAvgValues[i]);
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void refreshFFT()
  {
    final int lnAvgLength = this.faAvgValues.length;
    final int lnPowerOfTwo = this.nextPowerOfTwo(lnAvgLength);

    final double[] laFFTData = new double[lnPowerOfTwo];
    Arrays.fill(laFFTData, 0.0);

    for (int i = 0; i < lnAvgLength; ++i)
    {
      laFFTData[i] = this.faAvgValues[i];
    }

    this.foComplex = this.foFFTransformer.transform(laFFTData, TransformType.FORWARD);

    final int lnComplex = this.foComplex.length;
    double lnAverage = 0.0;
    for (int i = 0; i < lnComplex; ++i)
    {
      final double lnFrequency = Math.sqrt(Math.pow(this.foComplex[i].getReal(), 2) + Math.pow(this.foComplex[i].getImaginary(), 2));
      lnAverage += lnFrequency;
    }

    lnAverage /= lnComplex;

    final double lnSmoothing = (this.fnSmoothing != 0) ? this.fnSmoothing : 5;

    for (int i = 0; i < lnComplex; ++i)
    {
      final double lnTest = Math.sqrt(Math.pow(this.foComplex[i].getReal(), 2) + Math.pow(this.foComplex[i].getImaginary(), 2));

      if (lnTest < (lnAverage / lnSmoothing))
      {
        this.foComplex[i] = new Complex(0.0);
      }
    }

    this.foComplex = this.foFFTransformer.transform(this.foComplex, TransformType.INVERSE);
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

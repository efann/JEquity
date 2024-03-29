/*
 * JEquity
 * Copyright(c) 2008-2023, Beowurks
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

  private Complex[] foComplexFFTSeasonal;

  private double[] faAvgValues;

  private double fnSmoothing;

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

    if (this.faAvgValues.length == 0)
    {
      return;
    }

    this.refreshRegression();
    // Regression must be done before FFTSeasonal as FFTSeasonal uses the Regression results.
    this.refreshFFTSeasonal();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // y = intercept + slope * x
  public double getYValueRegression(final int tnXValue)
  {
    final double lnXValue = tnXValue;
    return (this.foSimpleRegression.getIntercept() + (this.foSimpleRegression.getSlope() * lnXValue));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // 0-based.
  public double getYValueFFTSeasonal(final int tnXValue)
  {
    final int lnAvgValuesLen = this.faAvgValues.length;
    final int lnIndex = tnXValue % lnAvgValuesLen;

    // You should use lnIndex for foComplexFFTSeasonal. However, getYValueRegression should use the actual tnXValue.
    return (this.foComplexFFTSeasonal[lnIndex].getReal() + this.getYValueRegression(tnXValue));
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
  private void refreshFFTSeasonal()
  {
    final int lnAvgLength = this.faAvgValues.length;
    final int lnPowerOfTwo = this.nextPowerOfTwo(lnAvgLength);

    final double[] laFFTSeasonalData = new double[lnPowerOfTwo];
    Arrays.fill(laFFTSeasonalData, 0.0);

    for (int i = 0; i < lnAvgLength; ++i)
    {
      // By substracting the regression value before running through FFT, the data is smoothed. I guess
      // some noise is also removed by regression calculations.
      // Plus I can now use the values in laFFTSeasonalData to calculate future values.
      laFFTSeasonalData[i] = this.faAvgValues[i] - this.getYValueRegression(i);
    }

    // Convert data to frequencies.
    this.foComplexFFTSeasonal = this.foFFTransformer.transform(laFFTSeasonalData, TransformType.FORWARD);

    double lnAverage = 0.0;
    for (final Complex loComplex : this.foComplexFFTSeasonal)
    {
      // From https://coderanch.com/t/618633/java/Basic-FFT-identify-frequency
      final double lnFrequency = Math.sqrt(Math.pow(loComplex.getReal(), 2) + Math.pow(loComplex.getImaginary(), 2));
      lnAverage += lnFrequency;
    }

    final int lnComplex = this.foComplexFFTSeasonal.length;
    lnAverage /= lnComplex;

    final double lnSmoothing = this.fnSmoothing;

    for (int i = 0; i < lnComplex; ++i)
    {
      final double lnTest = Math.sqrt(Math.pow(this.foComplexFFTSeasonal[i].getReal(), 2) + Math.pow(this.foComplexFFTSeasonal[i].getImaginary(), 2));

      // Now removing insignificant noise, I hope.
      // From https://stackoverflow.com/questions/20618804/how-to-smooth-a-curve-in-the-right-way
      if ((lnSmoothing > 0.0) && (lnTest < (lnAverage * lnSmoothing)))
      {
        this.foComplexFFTSeasonal[i] = new Complex(0.0);
      }
    }

    // Now convert altered frequency data back to normal data.
    this.foComplexFFTSeasonal = this.foFFTransformer.transform(this.foComplexFFTSeasonal, TransformType.INVERSE);
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

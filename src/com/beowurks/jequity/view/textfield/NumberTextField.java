/*
 * JEquity
 * Copyright(c) 2008-2022, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.textfield;

import java.text.NumberFormat;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class NumberTextField extends TextFieldPlus
{
  private final static String REGEX_NUMBER = "^[-+]?\\d{0,8}([.]\\d{0,6})?";

  private NumberFormat foNumbersOnlyTextFormat;

  // ---------------------------------------------------------------------------------------------------------------------
  public NumberTextField()
  {
    super();

    this.setupFormatters();
    this.setupListeners();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public NumberTextField(final String tcText)
  {
    super(tcText);

    this.setupFormatters();
    this.setupListeners();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void setupFormatters()
  {
    this.foNumbersOnlyTextFormat = NumberFormat.getNumberInstance();
    // In the regex expression, REGEX_NUMBER, the max decimal is 6.
    this.foNumbersOnlyTextFormat.setMaximumFractionDigits(6);

    // No commas should be used in the text box. Otherwise, in the textProperty listener,
    // the final check of (lcValue.matches(NumberTextField.REGEX_NUMBER)) will always
    // be false for numbers greater than 999. This will happen only when pasting large
    // numbers, not typing the numbers.
    this.foNumbersOnlyTextFormat.setGroupingUsed(false);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void setupListeners()
  {
    // This listener occurs when typing in the field or when setting to a complete
    // value, like when scrolling through the grid. Sometimes, a number is passed, like
    // -1.3423477784501E7 which is -13423477.784501.
    this.textProperty().addListener((observable, oldValue, newValue) ->
    {
      String lcValue = newValue;

      if (!lcValue.matches(NumberTextField.REGEX_NUMBER))
      {
        // Strip out all but the numbers, decimal point and minus sign.
        lcValue = lcValue.replaceAll("[^\\d.-]", "");

        double lnValue = 0.0;
        try
        {
          lnValue = Double.parseDouble(lcValue);
          lcValue = this.foNumbersOnlyTextFormat.format(lnValue);
        }
        catch (final NumberFormatException loErr)
        {
          lcValue = oldValue;
        }

        // If now okay, then replace the text with the correct value.
        if (lcValue.matches(NumberTextField.REGEX_NUMBER))
        {
          NumberTextField.this.setText(lcValue);
        }
        else
        {
          NumberTextField.this.setText(oldValue);
        }
      }
    });
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

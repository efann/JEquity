/*
 * JEquity
 * Copyright(c) 2008-2020, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.textfield;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class NumberTextField extends TextFieldPlus
{
  private final static String REGEX_NUMBER = "^[-+]?\\d{0,8}([\\.]\\d{0,6})?";

  // ---------------------------------------------------------------------------------------------------------------------
  public NumberTextField()
  {
    super();

    this.setupListeners();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public NumberTextField(final String tcText)
  {
    super(tcText);

    this.setupListeners();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void setupListeners()
  {
    this.textProperty().addListener((observable, oldValue, newValue) ->
    {
      String lcValue = newValue;

      if (!lcValue.matches(NumberTextField.REGEX_NUMBER))
      {
        // Strip out all but the numbers and decimal point.
        lcValue = lcValue.replaceAll("[^0-9.]", "");
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

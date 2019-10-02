/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.textfield;

import javafx.scene.control.DatePicker;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class DatePickerPlus extends DatePicker
{
  // ---------------------------------------------------------------------------------------------------------------------
  public DatePickerPlus()
  {
    super();

    this.setupListeners();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public DatePickerPlus(final LocalDate tdLocalDate)
  {
    super(tdLocalDate);

    this.setupListeners();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // Maybe a bug or not.
  // From https://stackoverflow.com/questions/32346893/javafx-datepicker-not-updating-value
  private void setupListeners()
  {
    this.getEditor().focusedProperty().addListener((obj, wasFocused, isFocused) ->
    {
      if (!isFocused)
      {
        try
        {
          this.setValue(this.getConverter().fromString(this.getEditor().getText()));
        }
        catch (final DateTimeParseException loErr)
        {
          this.getEditor().setText(this.getConverter().toString(this.getValue()));
        }
      }
    });
  }
  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

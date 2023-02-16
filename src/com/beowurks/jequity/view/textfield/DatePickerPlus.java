/*
 * JEquity
 * Copyright(c) 2008-2023, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.textfield;

import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.view.interfaces.IReadOnly;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class DatePickerPlus extends DatePicker implements IReadOnly
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
  public void setReadOnly(final boolean tlReadOnly)
  {
    final String lcStyle = tlReadOnly ? Constants.DISABLED_CONTROL_BACKGROUND : "";

    this.setEditable(!tlReadOnly);
    this.getEditor().setStyle(lcStyle);

    // The following hides / shows the button for the calendar.
    if (!tlReadOnly)
    {
      if (this.getStyleClass().contains(Constants.DATEPICKER_NON_EDITABLE))
      {
        this.getStyleClass().removeAll(Constants.DATEPICKER_NON_EDITABLE);
      }
    }
    else if (!this.getStyleClass().contains(Constants.DATEPICKER_NON_EDITABLE))
    {
      this.getStyleClass().add(Constants.DATEPICKER_NON_EDITABLE);
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

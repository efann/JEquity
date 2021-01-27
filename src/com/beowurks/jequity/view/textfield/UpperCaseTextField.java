/*
 * JEquity
 * Copyright(c) 2008-2021, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.textfield;

import javafx.scene.control.TextFormatter;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class UpperCaseTextField extends TextFieldPlus
{

  // ---------------------------------------------------------------------------------------------------------------------
  public UpperCaseTextField()
  {
    super();

    this.setupFormatters();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public UpperCaseTextField(final String tcText)
  {
    super(tcText);

    this.setupFormatters();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // From https://stackoverflow.com/questions/30884812/javafx-textfield-automatically-transform-text-to-uppercase
  // Previously I used this.textProperty().addListener((observable, oldValue, newValue) -> UpperCaseTextField.this.setText(newValue.toUpperCase())
  // Caused the following error on Ctrl-Z with a field that had been cleared or modified to empty:
  //   Exception in thread "JavaFX Application Thread" java.lang.NullPointerException
  //   at javafx.controls/javafx.scene.control.TextInputControl.updateUndoRedoState(TextInputControl.java:1198)
  protected void setupFormatters()
  {
    this.setTextFormatter(new TextFormatter<>((toChange) ->
    {
      toChange.setText(toChange.getText().toUpperCase());
      return toChange;
    }));
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

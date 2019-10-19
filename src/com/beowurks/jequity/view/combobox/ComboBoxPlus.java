/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.combobox;

import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// So I couldn't find a native way to disable the dropdown list on setEditable(false). And because
// setEditable is final for Node, I can't override it. So I just created a setReadOnly function.
abstract public class ComboBoxPlus<T> extends ComboBox
{
  private EventHandler foOriginalShownEvent;

  // ---------------------------------------------------------------------------------------------------------------------
  public ComboBoxPlus()
  {
    super();

    this.initializeComboBox();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  abstract protected void setupStringConverter();

  // ---------------------------------------------------------------------------------------------------------------------
  private void initializeComboBox()
  {
    this.foOriginalShownEvent = this.getOnShown();

    this.setupStringConverter();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // I got the basic idea from
  // https://stackoverflow.com/questions/58117745/disabling-javafx-combobox-input
  public void setReadOnly(final boolean tlReadOnly)
  {
    this.setEditable(!tlReadOnly);
    this.getEditor().setEditable(false);

    if (tlReadOnly)
    {
      this.setOnShown(loEvent -> this.hide());
      this.getStyleClass().add("hide-combo-box-arrow");
    }
    else
    {
      this.setOnShown(this.foOriginalShownEvent);
      this.getStyleClass().remove("hide-combo-box-arrow");
    }

  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

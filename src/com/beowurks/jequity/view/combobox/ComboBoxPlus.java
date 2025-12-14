/*
 * JEquity
 * Copyright(c) 2008-2025, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.combobox;

import com.beowurks.jequity.view.interfaces.IReadOnly;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// So I couldn't find a native way to disable the dropdown list on setEditable(false). And because
// setEditable is final for Node, I can't override it. So I just created a setReadOnly function.
abstract public class ComboBoxPlus<T> extends ComboBox<T> implements IReadOnly
{
  private EventHandler<javafx.event.Event> foOriginalShownEvent;

  // ---------------------------------------------------------------------------------------------------------------------
  public ComboBoxPlus()
  {
    super();

    this.initializeComboBox();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  abstract public T getSelectedItem();

  // ---------------------------------------------------------------------------------------------------------------------
  abstract protected void setupStringConverter();


  // ---------------------------------------------------------------------------------------------------------------------
  public int getSelectedIndex()
  {
    return (this.getSelectionModel().getSelectedIndex());
  }

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
    // Do not use setEditable: when switching back and forth in edit mode,
    // the displayed value remembers the last editable value, not the current one.
    // But the key is correct.
    // Bizarre.
    this.setDisable(tlReadOnly);

    if (this.getEditor().isEditable())
    {
      this.getEditor().setEditable(false);
    }

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

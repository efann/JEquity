/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.combobox;

import com.beowurks.jequity.dao.combobox.StringKeyItem;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// So I couldn't find a native way to disable the dropdown list on setEditable(false). And because
// setEditable is final for Node, I can't override it. So I just created a setReadOnly function.
public class ComboBoxPlus<T> extends ComboBox
{
  private EventHandler foOriginalShownEvent;

  // ---------------------------------------------------------------------------------------------------------------------
  // Though IntelliJ thinks this constructor is never called, it actually is.
  public ComboBoxPlus()
  {
    super();

    this.initializeComboBox();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public ComboBoxPlus(final ObservableList<StringKeyItem> toItems)
  {
    super(toItems);

    this.initializeComboBox();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void initializeComboBox()
  {
    this.foOriginalShownEvent = this.getOnShown();

    this.setConverter(new StringConverter<StringKeyItem>()
    {
      @Override
      public String toString(final StringKeyItem toItem)
      {
        if (toItem == null)
        {
          return (null);
        }
        return toItem.getDescription();
      }

      @Override
      public StringKeyItem fromString(final String tcString)
      {
        if (tcString == null)
        {
          return (null);
        }

        return ((StringKeyItem) ComboBoxPlus.this.getValue());
      }

    });

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

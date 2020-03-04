/*
 * JEquity
 * Copyright(c) 2008-2020, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.combobox;

import com.beowurks.jequity.dao.combobox.IntegerKeyItem;
import javafx.util.StringConverter;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// So I couldn't find a native way to disable the dropdown list on setEditable(false). And because
// setEditable is final for Node, I can't override it. So I just created a setReadOnly function.
public class ComboBoxIntegerKey extends ComboBoxPlus<IntegerKeyItem>
{

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  public IntegerKeyItem getSelectedItem()
  {
    return ((IntegerKeyItem) this.getValue());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  protected void setupStringConverter()
  {
    this.setConverter(new StringConverter<IntegerKeyItem>()
    {
      @Override
      public String toString(final IntegerKeyItem toItem)
      {
        if (toItem == null)
        {
          return (null);
        }

        return (toItem.getDescription());
      }

      @Override
      public IntegerKeyItem fromString(final String tcString)
      {
        if (tcString == null)
        {
          return (null);
        }

        return (ComboBoxIntegerKey.this.getSelectedItem());
      }
    });

  }
// ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

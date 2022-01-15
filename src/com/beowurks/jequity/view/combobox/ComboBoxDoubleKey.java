/*
 * JEquity
 * Copyright(c) 2008-2022, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.combobox;

import com.beowurks.jequity.dao.combobox.DoubleKeyItem;
import javafx.util.StringConverter;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// So I couldn't find a native way to disable the dropdown list on setEditable(false). And because
// setEditable is final for Node, I can't override it. So I just created a setReadOnly function.
public class ComboBoxDoubleKey extends ComboBoxPlus<DoubleKeyItem>
{

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  public DoubleKeyItem getSelectedItem()
  {
    return (this.getValue());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  protected void setupStringConverter()
  {
    this.setConverter(new StringConverter<DoubleKeyItem>()
    {
      @Override
      public String toString(final DoubleKeyItem toItem)
      {
        if (toItem == null)
        {
          return (null);
        }

        return (toItem.getDescription());
      }

      @Override
      public DoubleKeyItem fromString(final String tcString)
      {
        if (tcString == null)
        {
          return (null);
        }

        return (ComboBoxDoubleKey.this.getSelectedItem());
      }
    });

  }
// ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

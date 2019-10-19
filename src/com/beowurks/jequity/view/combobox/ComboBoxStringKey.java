/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.combobox;

import com.beowurks.jequity.dao.combobox.StringKeyItem;
import javafx.util.StringConverter;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// So I couldn't find a native way to disable the dropdown list on setEditable(false). And because
// setEditable is final for Node, I can't override it. So I just created a setReadOnly function.
public class ComboBoxStringKey extends ComboBoxPlus<StringKeyItem>
{

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  public StringKeyItem getSelectedValue()
  {
    return ((StringKeyItem) this.getValue());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  protected void setupStringConverter()
  {
    this.setConverter(new StringConverter<StringKeyItem>()
    {
      @Override
      public String toString(final StringKeyItem toItem)
      {
        if (toItem == null)
        {
          return (null);
        }
        return (toItem.getDescription());
      }

      @Override
      public StringKeyItem fromString(final String tcString)
      {
        if (tcString == null)
        {
          return (null);
        }

        return (ComboBoxStringKey.this.getSelectedValue());
      }

    });

  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

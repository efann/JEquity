/*
 * JEquity
 * Copyright(c) 2008-2025, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.cell;

import com.beowurks.jequity.utility.Misc;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// From http://code.makery.ch/blog/javafx-8-tableview-cell-renderer/
public class IntegerTableCell extends NumberTableCell<Object, Integer>
{
  // ---------------------------------------------------------------------------------------------------------------------

  @Override
  protected void updateItem(final Integer tnItem, final boolean tlEmpty)
  {
    super.updateItem(tnItem, tlEmpty);

    if ((tnItem == null) || tlEmpty)
    {
      this.setText(null);
      return;
    }

    this.setText(Misc.getIntegerFormat().format(tnItem));
    this.setStyleClass(tnItem);
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

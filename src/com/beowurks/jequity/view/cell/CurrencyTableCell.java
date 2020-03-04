/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
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
public class CurrencyTableCell extends NumberTableCell<Object, Double>
{
  // ---------------------------------------------------------------------------------------------------------------------
  public CurrencyTableCell()
  {
    super();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  protected void updateItem(final Double tnItem, final boolean tlEmpty)
  {
    super.updateItem(tnItem, tlEmpty);

    if ((tnItem == null) || tlEmpty)
    {
      this.setText(null);
      return;
    }

    this.setText(Misc.getCurrencyFormat().format(tnItem));
    this.setStyleClass(tnItem);

  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

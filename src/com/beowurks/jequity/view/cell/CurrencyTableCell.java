/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.cell;

import com.beowurks.jequity.utility.Misc;
import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// From http://code.makery.ch/blog/javafx-8-tableview-cell-renderer/
public class CurrencyTableCell extends TableCell<Object, Double>
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
      return;
    }

    this.setText(Misc.getCurrencyFormat().format(tnItem));
    this.setTextFill((tnItem >= 0) ? Color.BLACK : Color.RED);
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

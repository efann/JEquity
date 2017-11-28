/*
 * JEquity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
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
public class DoubleTableCell extends TableCell<Object, Double>
{
  // ---------------------------------------------------------------------------------------------------------------------
  public DoubleTableCell()
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

    this.setText(Misc.getDoubleFormat().format(tnItem));
    this.setTextFill((tnItem >= 0) ? Color.BLACK : Color.RED);
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

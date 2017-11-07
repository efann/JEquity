/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.controller.cell;

import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;

import java.text.NumberFormat;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// From http://code.makery.ch/blog/javafx-8-tableview-cell-renderer/
public class CurrencyTableCell extends TableCell<Object, Double>
{
  static private final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();

  // ---------------------------------------------------------------------------------------------------------------------

  @Override
  protected void updateItem(final Double tnItem, final boolean tlEmpty)
  {
    super.updateItem(tnItem, tlEmpty);


    if ((tnItem == null) || tlEmpty)
    {
      return;
    }

    this.setText(CurrencyTableCell.CURRENCY_FORMAT.format(tnItem));
    this.setTextFill((tnItem >= 0) ? Color.BLACK : Color.RED);
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

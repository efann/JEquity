/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.view.cell;

import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;

import java.text.NumberFormat;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// From http://code.makery.ch/blog/javafx-8-tableview-cell-renderer/
public class CurrencyTableCell extends TableCell<Object, Double>
{
  static private final NumberFormat foCurrencyFormat = NumberFormat.getCurrencyInstance();

  // ---------------------------------------------------------------------------------------------------------------------
  public CurrencyTableCell()
  {
    super();

    CurrencyTableCell.foCurrencyFormat.setMinimumFractionDigits(4);
    CurrencyTableCell.foCurrencyFormat.setMaximumFractionDigits(4);
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

    this.setText(CurrencyTableCell.foCurrencyFormat.format(tnItem));
    this.setTextFill((tnItem >= 0) ? Color.BLACK : Color.RED);
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

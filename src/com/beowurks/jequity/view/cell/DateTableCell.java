/*
 * JEquity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.view.cell;

import javafx.scene.control.TableCell;

import java.sql.Date;
import java.text.SimpleDateFormat;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// From http://code.makery.ch/blog/javafx-8-tableview-cell-renderer/
public class DateTableCell extends TableCell<Object, Date>
{
  static private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, MMM d, yyyy");

  // ---------------------------------------------------------------------------------------------------------------------

  @Override
  protected void updateItem(final Date tdItem, final boolean tlEmpty)
  {
    super.updateItem(tdItem, tlEmpty);

    if ((tdItem == null) || tlEmpty)
    {
      return;
    }

    this.setText(DateTableCell.DATE_FORMAT.format(tdItem));
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

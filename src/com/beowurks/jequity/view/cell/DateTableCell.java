/*
 * JEquity
 * Copyright(c) 2008-2017, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.view.cell;

import com.beowurks.jequity.utility.Misc;
import javafx.scene.control.TableCell;

import java.sql.Date;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// From http://code.makery.ch/blog/javafx-8-tableview-cell-renderer/
public class DateTableCell extends TableCell<Object, Date>
{
  // ---------------------------------------------------------------------------------------------------------------------

  @Override
  protected void updateItem(final Date tdItem, final boolean tlEmpty)
  {
    super.updateItem(tdItem, tlEmpty);

    if ((tdItem == null) || tlEmpty)
    {
      return;
    }

    this.setText(Misc.getDateFormat().format(tdItem));
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

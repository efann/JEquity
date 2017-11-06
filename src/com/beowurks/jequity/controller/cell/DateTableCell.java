/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.controller.cell;

import com.beowurks.jequity.dao.tableview.FinancialProperty;
import javafx.scene.control.TableCell;

import java.sql.Date;
import java.text.SimpleDateFormat;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// From http://code.makery.ch/blog/javafx-8-tableview-cell-renderer/
public class DateTableCell extends TableCell<FinancialProperty, Date>
{
  static private SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, MMM d, yyyy");

  // ---------------------------------------------------------------------------------------------------------------------

  @Override
  protected void updateItem(Date tdItem, boolean tlEmpty)
  {
    super.updateItem(tdItem, tlEmpty);

    if ((tdItem == null) || tlEmpty)
    {
      return;
    }

    // Format date.
    setText(DATE_FORMAT.format(tdItem));
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

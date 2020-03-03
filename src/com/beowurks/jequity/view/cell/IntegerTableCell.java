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

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// From http://code.makery.ch/blog/javafx-8-tableview-cell-renderer/
public class IntegerTableCell extends TableCell<Object, Integer>
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
    if (tnItem >= 0)
    {
      if (this.getStyleClass().indexOf("FinancialTableNegative") != -1)
      {
        this.getStyleClass().remove("FinancialTableNegative");
      }
      this.getStyleClass().add("FinancialTablePositive");
    }
    else
    {
      if (this.getStyleClass().indexOf("FinancialTablePositive") != -1)
      {
        this.getStyleClass().remove("FinancialTablePositive");
      }
      this.getStyleClass().add("FinancialTableNegative");
    }
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

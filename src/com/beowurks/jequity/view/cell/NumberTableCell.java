/*
 * JEquity
 * Copyright(c) 2008-2020, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.cell;

import javafx.scene.control.TableCell;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class NumberTableCell<S, T> extends TableCell<S, T>
{
  private final static String NEGATIVE_VALUE_STYLE = "FinancialTableNegative";
  private final static String POSITIVE_VALUE_STYLE = "FinancialTablePositive";

  // ---------------------------------------------------------------------------------------------------------------------
  protected void setStyleClass(final Number tnValue)
  {
    if (tnValue.doubleValue() >= 0.0)
    {
      if (this.getStyleClass().indexOf(NumberTableCell.NEGATIVE_VALUE_STYLE) != -1)
      {
        this.getStyleClass().remove(NumberTableCell.NEGATIVE_VALUE_STYLE);
      }
      this.getStyleClass().add(NumberTableCell.POSITIVE_VALUE_STYLE);
    }
    else
    {
      if (this.getStyleClass().indexOf(NumberTableCell.POSITIVE_VALUE_STYLE) != -1)
      {
        this.getStyleClass().remove(NumberTableCell.POSITIVE_VALUE_STYLE);
      }
      this.getStyleClass().add(NumberTableCell.NEGATIVE_VALUE_STYLE);
    }

  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

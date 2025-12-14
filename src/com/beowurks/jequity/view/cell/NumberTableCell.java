/*
 * JEquity
 * Copyright(c) 2008-2025, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.cell;

import javafx.collections.ObservableList;
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
      this.removeClass(NumberTableCell.NEGATIVE_VALUE_STYLE);
      this.addClass(NumberTableCell.POSITIVE_VALUE_STYLE);
    }
    else
    {
      this.removeClass(NumberTableCell.POSITIVE_VALUE_STYLE);
      this.addClass(NumberTableCell.NEGATIVE_VALUE_STYLE);
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void removeClass(final String tcClass)
  {
    final ObservableList<String> loClasses = this.getStyleClass();

    loClasses.remove(tcClass);

  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void addClass(final String tcClass)
  {
    final ObservableList<String> loClasses = this.getStyleClass();

    if (!loClasses.contains(tcClass))
    {
      loClasses.add(tcClass);
    }

  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

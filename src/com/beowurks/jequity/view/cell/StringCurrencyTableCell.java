/*
 * JEquity
 * Copyright(c) 2008-2025, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.cell;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// This class could be currency or blank or some text. It's used in the Amount, $ column of the Summary Table.
public class StringCurrencyTableCell extends NumberTableCell<Object, String>
{
  // ---------------------------------------------------------------------------------------------------------------------
  public StringCurrencyTableCell()
  {
    super();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  protected void updateItem(final String tcItem, final boolean tlEmpty)
  {
    super.updateItem(tcItem, tlEmpty);

    if ((tcItem == null) || tlEmpty)
    {
      // Now mimics the other cells' updateItem.
      this.setText(null);
      return;
    }

    this.setText(tcItem);

    // Strip out all but the numbers, decimal point and minus sign.
    final String lcClean = tcItem.replaceAll("[^\\d.-]", "");

    Double lnValue = 1.0;
    try
    {
      lnValue = Double.parseDouble(lcClean);
    }
    catch (final NumberFormatException loErr)
    {
      // Just set to a non-negative value
      lnValue = 1.0;
    }

    this.setStyleClass(lnValue);

  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

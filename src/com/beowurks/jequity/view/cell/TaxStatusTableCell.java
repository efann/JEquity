/*
 * JEquity
 * Copyright(c) 2008-2023, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.cell;

import com.beowurks.jequity.dao.combobox.StringKeyItem;
import com.beowurks.jequity.dao.combobox.TaxStatusList;
import javafx.scene.control.TableCell;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// From http://code.makery.ch/blog/javafx-8-tableview-cell-renderer/
public class TaxStatusTableCell extends TableCell<Object, String>
{
  // ---------------------------------------------------------------------------------------------------------------------
  public TaxStatusTableCell()
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
      // Now mimics the other cell updateItem.
      this.setText(null);
      return;
    }

    final StringKeyItem loItem = TaxStatusList.INSTANCE.getItem(tcItem);
    this.setText(loItem.getDescription());
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

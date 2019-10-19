/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.cell;

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
      return;
    }

    this.setText(TaxStatusList.INSTANCE.getItem(tcItem).getDescription());
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

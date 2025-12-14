/*
 * JEquity
 * Copyright(c) 2008-2025, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.tableskin;

import com.beowurks.jequity.view.tablecolumnheader.TableColumnHeaderPlus;
import com.beowurks.jequity.view.tableheaderrow.TableHeaderRowPlus;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;

// From https://stackoverflow.com/questions/62679995/javafx-14-resizecolumntofitcontent-method
//   Just brilliant, simple solution by
//   https://stackoverflow.com/users/2011505/tharkius

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class TableViewSkinPlus<T> extends TableViewSkin<T>
{
  // ---------------------------------------------------------------------------------------------------------------------
  public TableViewSkinPlus(final TableView<T> toTableView)
  {
    super(toTableView);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  protected TableHeaderRow createTableHeaderRow()
  {
    return new TableHeaderRowPlus(this);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void resizeColumnToFit()
  {
    for (final TableColumnHeader loHeader : this.getTableHeaderRow().getRootHeader().getColumnHeaders())
    {
      if (loHeader instanceof TableColumnHeaderPlus)
      {
        ((TableColumnHeaderPlus) loHeader).resizeColumnToFitContent();
      }
    }
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

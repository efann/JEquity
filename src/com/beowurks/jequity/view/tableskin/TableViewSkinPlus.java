/*
 * JEquity
 * Copyright(c) 2008-2022, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.tableskin;

import com.beowurks.jequity.view.tablecolumnheader.TableColumnHeaderPlus;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.NestedTableColumnHeader;
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
    return new TableHeaderRow(this)
    {
      // ---------------------------------------------------------------------------------------------------------------------
      @Override
      protected NestedTableColumnHeader createRootHeader()
      {
        return new NestedTableColumnHeader(null)
        {
          // ---------------------------------------------------------------------------------------------------------------------
          @Override
          protected TableColumnHeader createTableColumnHeader(final TableColumnBase col)
          {
            final TableColumnHeaderPlus loColumnHeader = new TableColumnHeaderPlus(col);
            return (loColumnHeader);
          }
          // ---------------------------------------------------------------------------------------------------------------------
        };
      }
      // ---------------------------------------------------------------------------------------------------------------------
    };
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

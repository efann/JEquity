/*
 * JEquity
 * Copyright(c) 2008-2022, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.tableheaderrow;

import com.beowurks.jequity.view.nestedtablecolumnheader.NestedTableColumnHeaderPlus;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkinBase;

// From https://stackoverflow.com/questions/62679995/javafx-14-resizecolumntofitcontent-method
//   Just brilliant, simple solution by
//   https://stackoverflow.com/users/2011505/tharkius

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class TableHeaderRowPlus extends TableHeaderRow
{
  // ---------------------------------------------------------------------------------------------------------------------
  public TableHeaderRowPlus(final TableViewSkinBase toSkin)
  {
    super(toSkin);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  protected NestedTableColumnHeader createRootHeader()
  {
    return (new NestedTableColumnHeaderPlus(null));
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------





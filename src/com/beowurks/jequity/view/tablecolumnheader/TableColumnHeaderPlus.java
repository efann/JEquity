/*
 * JEquity
 * Copyright(c) 2008-2025, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.tablecolumnheader;

import javafx.scene.control.TableColumnBase;
import javafx.scene.control.skin.TableColumnHeader;

// From https://stackoverflow.com/questions/62679995/javafx-14-resizecolumntofitcontent-method
//   Just brilliant, simple solution by
//   https://stackoverflow.com/users/2011505/tharkius

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class TableColumnHeaderPlus extends TableColumnHeader
{
  // ---------------------------------------------------------------------------------------------------------------------
  public TableColumnHeaderPlus(final TableColumnBase toColumn)
  {
    super(toColumn);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void resizeColumnToFitContent()
  {
    super.resizeColumnToFitContent(-1);
  }
  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

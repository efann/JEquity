/*
 * JEquity
 * Copyright(c) 2008-2020, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */
package com.beowurks.jequity.view.tablerow;

import com.beowurks.jequity.dao.tableview.SummaryProperty;
import com.beowurks.jequity.utility.Constants;
import javafx.collections.ObservableList;
import javafx.scene.control.TableRow;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// From http://stackoverflow.com/questions/32001/resettable-java-timer
public class SummaryTableRow extends TableRow<SummaryProperty>
{
  private final static String SUMMARY_TOTAL = "SummaryTableTotal";
  private final static String SUMMARY_RETIREMENT = "SummaryTableRetirements";
  private final static String SUMMARY_TAX = "SummaryTaxStatus";
  private final static String SUMMARY_OWNER = "SummaryTableOwnership";
  private final static String SUMMARY_ACCOUNT = "SummaryTableAccount";
  private final static String SUMMARY_TYPE = "SummaryTableType";
  private final static String SUMMARY_CATEGORY = "SummaryTableCategory";
  private final static String SUMMARY_REGULAR = "SummaryTableRegular";

  // ---------------------------------------------------------------------------------------------------------------------
  public SummaryTableRow()
  {
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  public void updateItem(final SummaryProperty toItem, final boolean tlEmpty)
  {
    super.updateItem(toItem, tlEmpty);
    if (toItem == null)
    {
      this.setStyle("");
      this.getStyleClass().clear();
      return;
    }

    final String lcDescription = toItem.getSummaryDescription();

    // Fixed the issues with row highlighting.
    if (lcDescription.equals(Constants.SUMMARY_TABLE_TOTAL))
    {
      this.addClass(SummaryTableRow.SUMMARY_TOTAL);
    }
    else if ((lcDescription.equals(Constants.SUMMARY_TABLE_RETIREMENT)) || (lcDescription.equals(Constants.SUMMARY_TABLE_NON_RETIREMENT)))
    {
      this.addClass(SummaryTableRow.SUMMARY_RETIREMENT);
    }
    // Tax Status
    else if ((lcDescription.startsWith("Tax")) && (lcDescription.contains("(Total)")))
    {
      this.addClass(SummaryTableRow.SUMMARY_TAX);
    }
    else if (lcDescription.equals(Constants.SUMMARY_TABLE_OWNERSHIP))
    {
      this.addClass(SummaryTableRow.SUMMARY_OWNER);
    }
    else if (lcDescription.equals(Constants.SUMMARY_TABLE_ACCOUNT))
    {
      this.addClass(SummaryTableRow.SUMMARY_ACCOUNT);
    }
    else if (lcDescription.equals(Constants.SUMMARY_TABLE_TYPE))
    {
      this.addClass(SummaryTableRow.SUMMARY_TYPE);
    }
    else if (lcDescription.equals(Constants.SUMMARY_TABLE_CATEGORY))
    {
      this.addClass(SummaryTableRow.SUMMARY_CATEGORY);
    }
    // This is key: otherwise, some non-visible cell (till you scroll) might get assigned
    // one of the above styles. Weird.
    else
    {
      this.addClass(SummaryTableRow.SUMMARY_REGULAR);
    }
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

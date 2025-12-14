/*
 * JEquity
 * Copyright(c) 2008-2025, Beowurks
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
public class SummaryTableRow extends TableRow<SummaryProperty>
{
  private final static String SUMMARY_PREFIX = "Summary";

  private final static String SUMMARY_TOTAL = String.format("%sTableTotal", SummaryTableRow.SUMMARY_PREFIX);
  private final static String SUMMARY_RETIREMENT = String.format("%sTableRetirements", SummaryTableRow.SUMMARY_PREFIX);
  private final static String SUMMARY_TAX = String.format("%sTaxStatus", SummaryTableRow.SUMMARY_PREFIX);
  private final static String SUMMARY_OWNER = String.format("%sTableOwnership", SummaryTableRow.SUMMARY_PREFIX);
  private final static String SUMMARY_ACCOUNT = String.format("%sTableAccount", SummaryTableRow.SUMMARY_PREFIX);
  private final static String SUMMARY_TYPE = String.format("%sTableType", SummaryTableRow.SUMMARY_PREFIX);
  private final static String SUMMARY_CATEGORY = String.format("%sTableCategory", SummaryTableRow.SUMMARY_PREFIX);
  private final static String SUMMARY_REGULAR = String.format("%sTableRegular", SummaryTableRow.SUMMARY_PREFIX);

  // ---------------------------------------------------------------------------------------------------------------------
  public SummaryTableRow()
  {
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  protected void updateItem(final SummaryProperty toItem, final boolean tlEmpty)
  {
    super.updateItem(toItem, tlEmpty);
    if (toItem == null)
    {
      this.setStyle("");
      return;
    }

    this.updateStyle(toItem);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void updateStyle(final SummaryProperty toItem)
  {
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
    else if (lcDescription.contains(Constants.SUMMARY_TABLE_OWNERSHIP_FRAGMENT))
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

    /*
      From https://stackoverflow.com/questions/1196586/calling-remove-in-foreach-loop-in-java
      Not sure why this is happening. But it is. Now, the summary grid
      is not randomly changing class styles when scrolling up & down. I'm wondering if TableRow relates to
      rows being displayed, and the items are continuously updated with data.

      Woah, this is a cool short-cut. It replaces code involving
      final Iterator<String> loLoop = loClasses.iterator() and
      while (loLoop.hasNext()) and
      String lcClass = loLoop.next() and
      loLoop.remove()
    */
    loClasses.removeIf(lcClass -> lcClass.startsWith(SummaryTableRow.SUMMARY_PREFIX) && !lcClass.equals(tcClass));

  }
  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

/*
 * JEquity
 * Copyright(c) 2008-2023, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.dao.combobox;

import com.beowurks.jequity.utility.Constants;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class TaxStatusList
{
  public static final int TAX_STATUS_NOT_FOUND = -1;
  // Here's a discussion of when INSTANCE will be initialized:
  // http://stackoverflow.com/questions/13724230/singleton-and-public-static-variable-java
  // Accordingly, it should be initialized when first accessed.
  public static final TaxStatusList INSTANCE = new TaxStatusList();

  private final ObservableList<StringKeyItem> faOptions = FXCollections.observableArrayList();

  // ---------------------------------------------------------------------------------------------------------------------
  private TaxStatusList()
  {
    this.faOptions.addAll(Constants.TAX_STATUS_OPTIONS);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public ObservableList<StringKeyItem> getList()
  {
    return (this.faOptions);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public int getCount()
  {
    return (this.faOptions.size());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public int getIndex(final String tcKey)
  {
    int lnIndex = TaxStatusList.TAX_STATUS_NOT_FOUND;
    for (final StringKeyItem loOption : this.faOptions)
    {
      if (loOption.getKey().equals(tcKey))
      {
        lnIndex = this.faOptions.indexOf(loOption);
        break;
      }
    }

    return (lnIndex);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public StringKeyItem getItem(final String tcKey)
  {
    StringKeyItem loItem = Constants.BLANK_STRINGKEYITEM;
    for (final StringKeyItem loOption : this.faOptions)
    {
      if (loOption.getKey().equals(tcKey))
      {
        loItem = loOption;
        break;
      }
    }

    return (loItem);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getDescription(final int tnIndex)
  {
    if ((tnIndex >= 0) && (tnIndex < this.faOptions.size()))
    {
      return (this.faOptions.get(tnIndex).getDescription());
    }

    return ("");
  }

  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

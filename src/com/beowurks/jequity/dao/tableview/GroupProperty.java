/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.dao.tableview;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// An excellent explanation on properties and TableViews
// https://stackoverflow.com/questions/13381067/simplestringproperty-and-simpleintegerproperty-tableview-javafx
public class GroupProperty
{
  private final IntegerProperty id;
  private final StringProperty description;

  // ---------------------------------------------------------------------------------------------------------------------
  public GroupProperty(final int tnID, final String tcDescription)
  {
    this.id = new SimpleIntegerProperty(tnID);
    this.description = new SimpleStringProperty(tcDescription);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public IntegerProperty idProperty()
  {
    return (this.id);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public StringProperty descriptionProperty()
  {
    return (this.description);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public int getID()
  {
    return this.id.get();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setID(final int toID)
  {
    this.id.set(toID);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getDescription()
  {
    return (this.description.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setDescription(final String toDescription)
  {
    this.description.set(toDescription);
  }

  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

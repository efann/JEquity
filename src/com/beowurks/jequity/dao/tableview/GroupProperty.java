/*
 * JEquity
 * Copyright(c) 2008-2022, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.dao.tableview;

import com.beowurks.jequity.dao.hibernate.GroupEntity;
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
  public GroupProperty()
  {
    this.id = new SimpleIntegerProperty();
    this.description = new SimpleStringProperty();
  }

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
  public GroupEntity toEntity()
  {
    final GroupEntity loEntity = new GroupEntity();

    loEntity.setGroupID(this.getID());
    loEntity.setDescription(this.getDescription());

    return (loEntity);
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

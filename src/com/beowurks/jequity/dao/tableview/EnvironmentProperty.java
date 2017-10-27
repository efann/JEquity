/*
 * J'EquityFX
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.dao.tableview;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// An excellent explanation on properties and TableViews
// https://stackoverflow.com/questions/13381067/simplestringproperty-and-simpleintegerproperty-tableview-javafx
public class EnvironmentProperty
{
  private final StringProperty foKey;
  private final StringProperty foValue;

  // ---------------------------------------------------------------------------------------------------------------------
  public EnvironmentProperty(final String tcKey, final String tcValue)
  {
    this.foKey = new SimpleStringProperty(tcKey);
    this.foValue = new SimpleStringProperty(tcValue);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getKey()
  {
    return (this.foKey.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setKey(final String tcKey)
  {
    this.foKey.set(tcKey);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public StringProperty keyProperty()
  {
    return (this.foKey);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getValue()
  {
    return (this.foValue.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setValue(final String tcValue)
  {
    this.foValue.set(tcValue);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public StringProperty valueProperty()
  {
    return (this.foValue);
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

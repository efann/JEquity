/*
 * JEquity
 * Copyright(c) 2008-2018, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
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
  public StringProperty keyProperty()
  {
    return (this.foKey);
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

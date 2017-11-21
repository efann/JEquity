/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.dao.tableview;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// An excellent explanation on properties and TableViews
// https://stackoverflow.com/questions/13381067/simplestringproperty-and-simpleintegerproperty-tableview-javafx
public class SummaryProperty
{
  private final StringProperty summarydescription;
  private final DoubleProperty summaryamount;

  // ---------------------------------------------------------------------------------------------------------------------
  public SummaryProperty(final String tcSummaryDescription, final Double tnSummaryAmount)
  {
    this.summarydescription = new SimpleStringProperty(tcSummaryDescription);
    this.summaryamount = new SimpleDoubleProperty(tnSummaryAmount);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getSummaryDescription()
  {
    return (this.summarydescription.get());
  }
  // ---------------------------------------------------------------------------------------------------------------------

  public StringProperty summarydescriptionProperty()
  {
    return (this.summarydescription);
  }
  // ---------------------------------------------------------------------------------------------------------------------

  public void setSummarySescription(final String tcSummaryDescription)
  {
    this.summarydescription.set(tcSummaryDescription);
  }
  // ---------------------------------------------------------------------------------------------------------------------

  public double getSummaryAmount()
  {
    return (this.summaryamount.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public DoubleProperty summaryamountProperty()
  {
    return (this.summaryamount);
  }
  // ---------------------------------------------------------------------------------------------------------------------

  public void setSummaryAmount(final double tnSummaryAmount)
  {
    this.summaryamount.set(tnSummaryAmount);
  }


  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

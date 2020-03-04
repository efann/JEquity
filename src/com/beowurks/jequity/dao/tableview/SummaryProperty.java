/*
 * JEquity
 * Copyright(c) 2008-2020, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.dao.tableview;

import com.beowurks.jequity.utility.Misc;
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
  private final StringProperty summaryamount;

  // ---------------------------------------------------------------------------------------------------------------------
  public SummaryProperty(final String tcSummaryDescription, final Double tnSummaryAmount)
  {
    this.summarydescription = new SimpleStringProperty(tcSummaryDescription);
    this.summaryamount = new SimpleStringProperty(this.formatDouble(tnSummaryAmount));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public SummaryProperty(final String tcSummaryDescription)
  {
    this.summarydescription = new SimpleStringProperty(tcSummaryDescription);
    this.summaryamount = new SimpleStringProperty("");
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

  public String getSummaryAmount()
  {
    return (this.summaryamount.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public StringProperty summaryamountProperty()
  {
    return (this.summaryamount);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setSummaryAmount(final double tnSummaryAmount)
  {
    this.summaryamount.set(this.formatDouble(tnSummaryAmount));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private String formatDouble(final double tnSummaryAmount)
  {
    return (Misc.getCurrencyFormat().format(tnSummaryAmount));
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

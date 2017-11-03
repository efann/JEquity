/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.dao.tableview;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// An excellent explanation on properties and TableViews
// https://stackoverflow.com/questions/13381067/simplestringproperty-and-simpleintegerproperty-tableview-javafx
public class FinancialProperty
{
  private final IntegerProperty id;
  private final StringProperty description;

  private final StringProperty account;
  private final StringProperty type;
  private final StringProperty category;
  private final DoubleProperty shares;
  private final DoubleProperty price;
  private final StringProperty valuationdate;
  private final BooleanProperty retirement;
  private final StringProperty symbol;
  private final DoubleProperty total;

  private final DateFormat foDateFormat = new SimpleDateFormat("M/dd/yyyy");

  // ---------------------------------------------------------------------------------------------------------------------
  public FinancialProperty(final int tnID, final String tcDescription, final String tcAccount, final String tcType, final String tcCategory,
                           final double tnShares, final double tnPrice, final java.sql.Date tdDate, final boolean tlRetirement, final String tcSymbol)
  {
    this.id = new SimpleIntegerProperty(tnID);
    this.description = new SimpleStringProperty(tcDescription);
    this.account = new SimpleStringProperty(tcAccount);
    this.type = new SimpleStringProperty(tcType);
    this.category = new SimpleStringProperty(tcCategory);
    this.shares = new SimpleDoubleProperty(tnShares);
    this.price = new SimpleDoubleProperty(tnPrice);
    this.valuationdate = new SimpleStringProperty(this.foDateFormat.format(tdDate));
    this.retirement = new SimpleBooleanProperty(tlRetirement);
    this.symbol = new SimpleStringProperty(tcSymbol);
    this.total = new SimpleDoubleProperty(tnShares * tnPrice);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public int getId()
  {
    return (this.id.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public IntegerProperty idProperty()
  {
    return (this.id);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setId(final int tnID)
  {
    this.id.set(tnID);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getDescription()
  {
    return (this.description.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public StringProperty descriptionProperty()
  {
    return (this.description);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setDescription(final String tcDescription)
  {
    this.description.set(tcDescription);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getAccount()
  {
    return (this.account.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public StringProperty accountProperty()
  {
    return (this.account);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setAccount(final String tcAccount)
  {
    this.account.set(tcAccount);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getType()
  {
    return (this.type.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public StringProperty typeProperty()
  {
    return (this.type);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setType(final String tcType)
  {
    this.type.set(tcType);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getCategory()
  {
    return (this.category.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public StringProperty categoryProperty()
  {
    return (this.category);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setCategory(final String tcCategory)
  {
    this.category.set(tcCategory);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public double getShares()
  {
    return (this.shares.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public DoubleProperty sharesProperty()
  {
    return (this.shares);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setShares(final double tnShares)
  {
    this.shares.set(tnShares);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public double getPrice()
  {
    return (this.price.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public DoubleProperty priceProperty()
  {
    return (this.price);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setPrice(final double tnPrice)
  {
    this.price.set(tnPrice);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getValuationDate()
  {
    return (this.valuationdate.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public StringProperty valuationdateProperty()
  {
    return (this.valuationdate);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setValuationDate(final String tcValuationDate)
  {
    this.valuationdate.set(tcValuationDate);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public boolean isRetirement()
  {
    return (this.retirement.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public BooleanProperty retirementProperty()
  {
    return (this.retirement);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setRetirement(final boolean tlRetirement)
  {
    this.retirement.set(tlRetirement);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getSymbol()
  {
    return (this.symbol.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public StringProperty symbolProperty()
  {
    return (this.symbol);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setSymbol(final String tcSymbol)
  {
    this.symbol.set(tcSymbol);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public double getTotal()
  {
    return (this.total.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public DoubleProperty totalProperty()
  {
    return (this.total);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setTotal(final double tnTotal)
  {
    this.total.set(tnTotal);
  }

  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

/*
 * JEquity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.dao.tableview;

import com.beowurks.jequity.dao.hibernate.FinancialEntity;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// An excellent explanation on properties and TableViews
// https://stackoverflow.com/questions/13381067/simplestringproperty-and-simpleintegerproperty-tableview-javafx
public class FinancialProperty
{
  private final IntegerProperty financialid;
  private final IntegerProperty groupid;
  private final StringProperty description;
  private final StringProperty account;
  private final StringProperty type;
  private final StringProperty category;
  private final DoubleProperty shares;
  private final DoubleProperty price;
  private final ObjectProperty<Date> valuationdate;
  private final BooleanProperty retirement;
  private final StringProperty symbol;
  private final DoubleProperty total;

  private final StringProperty comments;

  private final DateFormat foDateFormat = new SimpleDateFormat("M/dd/yyyy");

  // ---------------------------------------------------------------------------------------------------------------------
  public FinancialProperty(final int tnGroupID, final int tnFinancialID, final String tcDescription, final String tcAccount, final String tcType, final String tcCategory,
                           final double tnShares, final double tnPrice, final java.sql.Date tdDate, final boolean tlRetirement,
                           final String tcSymbol, final String tcComments)
  {
    this.groupid = new SimpleIntegerProperty(tnGroupID);
    this.financialid = new SimpleIntegerProperty(tnFinancialID);
    this.description = new SimpleStringProperty(tcDescription);
    this.account = new SimpleStringProperty(tcAccount);
    this.type = new SimpleStringProperty(tcType);
    this.category = new SimpleStringProperty(tcCategory);
    this.shares = new SimpleDoubleProperty(tnShares);
    this.price = new SimpleDoubleProperty(tnPrice);
    this.valuationdate = new SimpleObjectProperty<>(tdDate);
    this.retirement = new SimpleBooleanProperty(tlRetirement);
    this.symbol = new SimpleStringProperty(tcSymbol);
    this.comments = new SimpleStringProperty(tcComments);
    this.total = new SimpleDoubleProperty(tnShares * tnPrice);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public int getGroupID()
  {
    return (this.groupid.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public IntegerProperty groupidProperty()
  {
    return (this.groupid);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setGroupID(final int tnGroupID)
  {
    this.groupid.set(tnGroupID);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public int getFinancialID()
  {
    return (this.financialid.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public IntegerProperty financialidProperty()
  {
    return (this.financialid);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setFinancialID(final int tnID)
  {
    this.financialid.set(tnID);
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
  public Date getValuationDate()
  {
    return (this.valuationdate.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public ObjectProperty<Date> valuationdateProperty()
  {
    return (this.valuationdate);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setValuationDate(final Date tdValuationDate)
  {
    this.valuationdate.set(tdValuationDate);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public boolean getRetirement()
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
  public String getComments()
  {
    return (this.comments.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public StringProperty commentsProperty()
  {
    return (this.comments);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setComments(final String comments)
  {
    this.comments.set(comments);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public FinancialEntity toEntity()
  {
    final FinancialEntity loEntity = new FinancialEntity();

    loEntity.setGroupID(this.getGroupID());
    loEntity.setFinancialID(this.getFinancialID());
    loEntity.setDescription(this.getDescription());
    loEntity.setAccount(this.getAccount());
    loEntity.setType(this.getType());
    loEntity.setCategory(this.getCategory());
    loEntity.setShares(this.getShares());
    loEntity.setPrice(this.getPrice());
    loEntity.setValuationDate(this.getValuationDate());
    loEntity.setSymbol(this.getSymbol());
    loEntity.setRetirement(this.getRetirement());
    loEntity.setComments(this.getComments());

    return (loEntity);
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

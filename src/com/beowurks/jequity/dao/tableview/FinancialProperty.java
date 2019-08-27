/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.dao.tableview;

import com.beowurks.jequity.dao.hibernate.FinancialEntity;
import javafx.beans.property.*;

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
  private final BooleanProperty taxable1099;
  private final StringProperty symbol;
  private final DoubleProperty total;

  private final StringProperty comments;

  private final DateFormat foDateFormat = new SimpleDateFormat("M/dd/yyyy");

  // ---------------------------------------------------------------------------------------------------------------------
  public FinancialProperty()
  {
    this.groupid = new SimpleIntegerProperty();
    this.financialid = new SimpleIntegerProperty();
    this.description = new SimpleStringProperty();
    this.account = new SimpleStringProperty();
    this.type = new SimpleStringProperty();
    this.category = new SimpleStringProperty();
    this.shares = new SimpleDoubleProperty();
    this.price = new SimpleDoubleProperty();
    this.valuationdate = new SimpleObjectProperty<>();
    this.retirement = new SimpleBooleanProperty(false);
    this.taxable1099 = new SimpleBooleanProperty(false);
    this.symbol = new SimpleStringProperty();
    this.comments = new SimpleStringProperty();
    this.total = new SimpleDoubleProperty();

    this.setupListeners();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public FinancialProperty(final int tnGroupID, final int tnFinancialID, final String tcDescription, final String tcAccount, final String tcType, final String tcCategory,
                           final double tnShares, final double tnPrice, final java.sql.Date tdDate, final boolean tlRetirement, final boolean tlTaxable1099,
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
    this.taxable1099 = new SimpleBooleanProperty(tlTaxable1099);
    this.symbol = new SimpleStringProperty(tcSymbol);
    this.comments = new SimpleStringProperty(tcComments);
    this.total = new SimpleDoubleProperty(tnShares * tnPrice);

    this.setupListeners();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupListeners()
  {
    this.shares.addListener((observable, oldValue, newValue) -> this.setTotal(this.getShares() * this.getPrice()));

    this.price.addListener((observable, oldValue, newValue) -> this.setTotal(this.getShares() * this.getPrice()));
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
  public boolean getTaxable1099()
  {
    return (this.taxable1099.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public BooleanProperty taxable1099Property()
  {
    return (this.taxable1099);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setTaxable1099(final boolean tlTaxable1099)
  {
    this.taxable1099.set(tlTaxable1099);
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
    loEntity.setTaxable1099(this.getTaxable1099());
    loEntity.setComments(this.getComments());

    return (loEntity);
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

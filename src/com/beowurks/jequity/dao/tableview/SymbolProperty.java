/*
 * JEquity
 * Copyright(c) 2008-2021, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.dao.tableview;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// An excellent explanation on properties and TableViews
// https://stackoverflow.com/questions/13381067/simplestringproperty-and-simpleintegerproperty-tableview-javafx
public class SymbolProperty
{
  private final StringProperty symbol;
  private final StringProperty description;
  private final DoubleProperty lasttrade;
  private final ObjectProperty<Timestamp> tradetime;
  private final StringProperty comments;
  private final StringProperty historicalinfo;

  private final DateFormat foDateFormat = new SimpleDateFormat("M/dd/yyyy h:mm:ss a");

  // ---------------------------------------------------------------------------------------------------------------------
  public SymbolProperty(final String tcSymbol, final String tcDescription, final double tnLastTrade, final Timestamp ttTradeTime,
                        final String tcComments, final String tcHistoricalInfo)
  {
    this.symbol = new SimpleStringProperty(tcSymbol);
    this.description = new SimpleStringProperty(tcDescription);
    this.lasttrade = new SimpleDoubleProperty(tnLastTrade);
    this.tradetime = new SimpleObjectProperty<>(ttTradeTime);
    this.comments = new SimpleStringProperty(tcComments);
    this.historicalinfo = new SimpleStringProperty(tcHistoricalInfo);
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
  public double getLasttrade()
  {
    return (this.lasttrade.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public DoubleProperty lasttradeProperty()
  {
    return (this.lasttrade);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setLasttrade(final double tnLastTrade)
  {
    this.lasttrade.set(tnLastTrade);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Timestamp getTradetime()
  {
    return (this.tradetime.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public ObjectProperty<Timestamp> tradetimeProperty()
  {
    return (this.tradetime);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setTradetime(final Timestamp ttTradeTime)
  {
    this.tradetime.set(ttTradeTime);
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
  public void setComments(final String tcComments)
  {
    this.comments.set(tcComments);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getHistoricalinfo()
  {
    return (this.historicalinfo.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public StringProperty historicalinfoProperty()
  {
    return (this.historicalinfo);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setHistoricalinfo(final String tcHistoricalInfo)
  {
    this.historicalinfo.set(tcHistoricalInfo);
  }

  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

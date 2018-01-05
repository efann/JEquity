/*
 * JEquity
 * Copyright(c) 2008-2018, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.dao.tableview;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
  private final DoubleProperty differential;
  private final DoubleProperty previousclose;
  private final DoubleProperty opened;
  private final DoubleProperty bidding;
  private final DoubleProperty asking;
  private final DoubleProperty targetestimate;
  private final StringProperty dayrange;
  private final StringProperty yearrange;
  private final IntegerProperty volume;
  private final IntegerProperty averagevolume;
  private final StringProperty marketcap;
  private final DoubleProperty priceearnings;
  private final DoubleProperty earningspershare;
  private final StringProperty dividendyield;
  private final StringProperty comments;
  private final StringProperty historicalinfo;

  private final DateFormat foDateFormat = new SimpleDateFormat("M/dd/yyyy h:mm:ss a");

  // ---------------------------------------------------------------------------------------------------------------------
  public SymbolProperty(final String tcSymbol, final String tcDescription, final double tnLastTrade, final Timestamp ttTradeTime, final double tnDifferential,
                        final double tnPreviousClose, final double tnOpened, final double tnBidding, final double tnAsking, final double tnTargetEstimate,
                        final String tcDayRange, final String tcYearRange, final int tnVolume, final int tnAverageVolume, final String tcMarketCap,
                        final double tnPriceEarnings, final double tnEarningsPerShare, final String tcDividendYield, final String tcComments,
                        final String tcHistoricalInfo)
  {
    this.symbol = new SimpleStringProperty(tcSymbol);
    this.description = new SimpleStringProperty(tcDescription);
    this.lasttrade = new SimpleDoubleProperty(tnLastTrade);
    this.tradetime = new SimpleObjectProperty<>(ttTradeTime);
    this.differential = new SimpleDoubleProperty(tnDifferential);
    this.previousclose = new SimpleDoubleProperty(tnPreviousClose);
    this.opened = new SimpleDoubleProperty(tnOpened);
    this.bidding = new SimpleDoubleProperty(tnBidding);
    this.asking = new SimpleDoubleProperty(tnAsking);
    this.targetestimate = new SimpleDoubleProperty(tnTargetEstimate);
    this.dayrange = new SimpleStringProperty(tcDayRange);
    this.yearrange = new SimpleStringProperty(tcYearRange);
    this.volume = new SimpleIntegerProperty(tnVolume);
    this.averagevolume = new SimpleIntegerProperty(tnAverageVolume);
    this.marketcap = new SimpleStringProperty(tcMarketCap);
    this.priceearnings = new SimpleDoubleProperty(tnPriceEarnings);
    this.earningspershare = new SimpleDoubleProperty(tnEarningsPerShare);
    this.dividendyield = new SimpleStringProperty(tcDividendYield);
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
  public double getDifferential()
  {
    return (this.differential.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public DoubleProperty differentialProperty()
  {
    return (this.differential);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setDifferential(final double tnDifferential)
  {
    this.differential.set(tnDifferential);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public double getPreviousclose()
  {
    return (this.previousclose.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public DoubleProperty previouscloseProperty()
  {
    return (this.previousclose);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setPreviousclose(final double tnPreviousClose)
  {
    this.previousclose.set(tnPreviousClose);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public double getOpened()
  {
    return (this.opened.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public DoubleProperty openedProperty()
  {
    return (this.opened);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setOpened(final double tnOpened)
  {
    this.opened.set(tnOpened);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public double getBidding()
  {
    return (this.bidding.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public DoubleProperty biddingProperty()
  {
    return (this.bidding);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setBidding(final double tnBidding)
  {
    this.bidding.set(tnBidding);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public double getAsking()
  {
    return (this.asking.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public DoubleProperty askingProperty()
  {
    return (this.asking);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setAsking(final double tnAsking)
  {
    this.asking.set(tnAsking);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public double getTargetestimate()
  {
    return (this.targetestimate.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public DoubleProperty targetestimateProperty()
  {
    return (this.targetestimate);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setTargetestimate(final double tnTargetEstimate)
  {
    this.targetestimate.set(tnTargetEstimate);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getDayrange()
  {
    return (this.dayrange.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public StringProperty dayrangeProperty()
  {
    return (this.dayrange);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setDayrange(final String tcDayRange)
  {
    this.dayrange.set(tcDayRange);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getYearrange()
  {
    return (this.yearrange.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public StringProperty yearrangeProperty()
  {
    return (this.yearrange);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setYearrange(final String tcYearRange)
  {
    this.yearrange.set(tcYearRange);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public int getVolume()
  {
    return (this.volume.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public IntegerProperty volumeProperty()
  {
    return (this.volume);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setVolume(final int tnVolume)
  {
    this.volume.set(tnVolume);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public int getAveragevolume()
  {
    return (this.averagevolume.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public IntegerProperty averagevolumeProperty()
  {
    return (this.averagevolume);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setAveragevolume(final int tnAverageVolume)
  {
    this.averagevolume.set(tnAverageVolume);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getMarketcap()
  {
    return (this.marketcap.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public StringProperty marketcapProperty()
  {
    return (this.marketcap);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setMarketcap(final String tcMarketCap)
  {
    this.marketcap.set(tcMarketCap);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public double getPriceearnings()
  {
    return (this.priceearnings.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public DoubleProperty priceearningsProperty()
  {
    return (this.priceearnings);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setPriceearnings(final double tnPriceEarnings)
  {
    this.priceearnings.set(tnPriceEarnings);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public double getEarningspershare()
  {
    return (this.earningspershare.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public DoubleProperty earningspershareProperty()
  {
    return (this.earningspershare);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setEarningspershare(final double tnEarningsPerShare)
  {
    this.earningspershare.set(tnEarningsPerShare);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getDividendyield()
  {
    return (this.dividendyield.get());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public StringProperty dividendyieldProperty()
  {
    return (this.dividendyield);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setDividendyield(final String tcDividendYield)
  {
    this.dividendyield.set(tcDividendYield);
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

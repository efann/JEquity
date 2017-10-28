/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */
// Generated by IntelliJ
// View | Tool Windows | Persistence
// Make sure that you have a hibernate.cfg.xml file in project
package com.beowurks.jequity.dao.hibernate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

// The schema is set in the configuration file. This way, the schema can be specific
// to the database. For example, MySQL doesn't have a schema in the traditional sense:
// The schema is the database.
// Case-sensitive issue solution: https://forum.hibernate.org/viewtopic.php?f=1&t=972808
@Entity
@Table(name = "`Symbol`", catalog = "")
public class SymbolEntity implements Serializable
{

  private static final long serialVersionUID = 1L;
  private String symbol = "";

  @Id
  @Column(name = "SYMBOL", nullable = false, insertable = true, updatable = true, length = 10)
  public String getSymbol()
  {
    return (this.symbol);
  }

  public void setSymbol(final String symbol)
  {
    this.symbol = symbol;
  }

  private String description = "";

  @Basic
  @Column(name = "DESCRIPTION", nullable = false, insertable = true, updatable = true, length = 255)
  public String getDescription()
  {
    return (this.description);
  }

  public void setDescription(final String description)
  {
    this.description = description;
  }

  private Double lasttrade = 0.0;

  @Basic
  @Column(name = "LASTTRADE", nullable = false, insertable = true, updatable = true, precision = 0)
  public Double getLastTrade()
  {
    return (this.lasttrade);
  }

  public void setLastTrade(final Double lasttrade)
  {
    this.lasttrade = lasttrade;
  }

  private Timestamp tradetime = new Timestamp(0);

  @Basic
  @Column(name = "TRADETIME", nullable = false, insertable = true, updatable = true)
  public Timestamp getTradeTime()
  {
    return (this.tradetime);
  }

  public void setTradeTime(final Timestamp tradetime)
  {
    this.tradetime = tradetime;
  }

  private Double differential = 0.0;

  @Basic
  @Column(name = "DIFFERENTIAL", nullable = false, insertable = true, updatable = true, precision = 0)
  public Double getDifferential()
  {
    return (this.differential);
  }

  public void setDifferential(final Double differential)
  {
    this.differential = differential;
  }

  private Double previousclose = 0.0;

  @Basic
  @Column(name = "PREVIOUSCLOSE", nullable = false, insertable = true, updatable = true, precision = 0)
  public Double getPreviousClose()
  {
    return (this.previousclose);
  }

  public void setPreviousClose(final Double previousclose)
  {
    this.previousclose = previousclose;
  }

  private Double opened = 0.0;

  @Basic
  @Column(name = "OPENED", nullable = false, insertable = true, updatable = true, precision = 0)
  public Double getOpened()
  {
    return (this.opened);
  }

  public void setOpened(final Double opened)
  {
    this.opened = opened;
  }

  private Double bidding = 0.0;

  @Basic
  @Column(name = "BIDDING", nullable = false, insertable = true, updatable = true, precision = 0)
  public Double getBidding()
  {
    return (this.bidding);
  }

  public void setBidding(final Double bidding)
  {
    this.bidding = bidding;
  }

  private Double asking = 0.0;

  @Basic
  @Column(name = "ASKING", nullable = false, insertable = true, updatable = true, precision = 0)
  public Double getAsking()
  {
    return (this.asking);
  }

  public void setAsking(final Double asking)
  {
    this.asking = asking;
  }

  private Double targetestimate = 0.0;

  @Basic
  @Column(name = "TARGETESTIMATE", nullable = false, insertable = true, updatable = true, precision = 0)
  public Double getTargetEstimate()
  {
    return (this.targetestimate);
  }

  public void setTargetEstimate(final Double targetestimate)
  {
    this.targetestimate = targetestimate;
  }

  private String dayrange = "";

  @Basic
  @Column(name = "DAYRANGE", nullable = false, insertable = true, updatable = true, length = 20)
  public String getDayRange()
  {
    return (this.dayrange);
  }

  public void setDayRange(final String dayrange)
  {
    this.dayrange = dayrange;
  }

  private String yearrange = "";

  @Basic
  @Column(name = "YEARRANGE", nullable = false, insertable = true, updatable = true, length = 20)
  public String getYearRange()
  {
    return (this.yearrange);
  }

  public void setYearRange(final String yearrange)
  {
    this.yearrange = yearrange;
  }

  private Integer volume = 0;

  @Basic
  @Column(name = "VOLUME", nullable = false, insertable = true, updatable = true)
  public Integer getVolume()
  {
    return (this.volume);
  }

  public void setVolume(final Integer volume)
  {
    this.volume = volume;
  }

  private Integer averagevolume = 0;

  @Basic
  @Column(name = "AVERAGEVOLUME", nullable = false, insertable = true, updatable = true)
  public Integer getAverageVolume()
  {
    return (this.averagevolume);
  }

  public void setAverageVolume(final Integer averagevolume)
  {
    this.averagevolume = averagevolume;
  }

  private String marketcap = "";

  @Basic
  @Column(name = "MARKETCAP", nullable = false, insertable = true, updatable = true, length = 20)
  public String getMarketCap()
  {
    return (this.marketcap);
  }

  public void setMarketCap(final String marketcap)
  {
    this.marketcap = marketcap;
  }

  private Double priceearnings = 0.0;

  @Basic
  @Column(name = "PRICEEARNINGS", nullable = false, insertable = true, updatable = true, precision = 0)
  public Double getPriceEarnings()
  {
    return (this.priceearnings);
  }

  public void setPriceEarnings(final Double priceearnings)
  {
    this.priceearnings = priceearnings;
  }

  private Double earningspershare = 0.0;

  @Basic
  @Column(name = "EARNINGSPERSHARE", nullable = false, insertable = true, updatable = true, precision = 0)
  public Double getEarningsPerShare()
  {
    return (this.earningspershare);
  }

  public void setEarningsPerShare(final Double earningspershare)
  {
    this.earningspershare = earningspershare;
  }

  private String dividendyield = "";

  @Basic
  @Column(name = "DIVIDENDYIELD", nullable = false, insertable = true, updatable = true, length = 20)
  public String getDividendYield()
  {
    return (this.dividendyield);
  }

  public void setDividendYield(final String dividendyield)
  {
    this.dividendyield = dividendyield;
  }

  private String comments = "";

  @Basic
  @Column(name = "COMMENTS", nullable = false, insertable = true, updatable = true, length = 32700)
  public String getComments()
  {
    return (this.comments);
  }

  public void setComments(final String comments)
  {
    this.comments = comments;
  }

  private String historicalinfo = "";

  @Basic
  @Column(name = "HISTORICALINFO", nullable = false, insertable = true, updatable = true, length = 8192)
  public String getHistoricalInfo()
  {
    return (this.historicalinfo);
  }

  public void setHistoricalInfo(final String historicalinfo)
  {
    this.historicalinfo = historicalinfo;
  }
  
  @Override
  public boolean equals(final Object o)
  {
    if (this == o)
    {
      return (true);
    }
    if (o == null || this.getClass() != o.getClass())
    {
      return (false);
    }

    final SymbolEntity that = (SymbolEntity) o;

    if (this.asking != null ? !this.asking.equals(that.asking) : that.asking != null)
    {
      return (false);
    }
    if (this.averagevolume != null ? !this.averagevolume.equals(that.averagevolume) : that.averagevolume != null)
    {
      return (false);
    }
    if (this.bidding != null ? !this.bidding.equals(that.bidding) : that.bidding != null)
    {
      return (false);
    }
    if (this.differential != null ? !this.differential.equals(that.differential) : that.differential != null)
    {
      return (false);
    }
    if (this.dayrange != null ? !this.dayrange.equals(that.dayrange) : that.dayrange != null)
    {
      return (false);
    }
    if (this.description != null ? !this.description.equals(that.description) : that.description != null)
    {
      return (false);
    }
    if (this.dividendyield != null ? !this.dividendyield.equals(that.dividendyield) : that.dividendyield != null)
    {
      return (false);
    }
    if (this.earningspershare != null ? !this.earningspershare.equals(that.earningspershare) : that.earningspershare != null)
    {
      return (false);
    }
    if (this.lasttrade != null ? !this.lasttrade.equals(that.lasttrade) : that.lasttrade != null)
    {
      return (false);
    }
    if (this.marketcap != null ? !this.marketcap.equals(that.marketcap) : that.marketcap != null)
    {
      return (false);
    }
    if (this.opened != null ? !this.opened.equals(that.opened) : that.opened != null)
    {
      return (false);
    }
    if (this.previousclose != null ? !this.previousclose.equals(that.previousclose) : that.previousclose != null)
    {
      return (false);
    }
    if (this.priceearnings != null ? !this.priceearnings.equals(that.priceearnings) : that.priceearnings != null)
    {
      return (false);
    }
    if (this.symbol != null ? !this.symbol.equals(that.symbol) : that.symbol != null)
    {
      return (false);
    }
    if (this.targetestimate != null ? !this.targetestimate.equals(that.targetestimate) : that.targetestimate != null)
    {
      return (false);
    }
    if (this.tradetime != null ? !this.tradetime.equals(that.tradetime) : that.tradetime != null)
    {
      return (false);
    }
    if (this.volume != null ? !this.volume.equals(that.volume) : that.volume != null)
    {
      return (false);
    }
    if (this.yearrange != null ? !this.yearrange.equals(that.yearrange) : that.yearrange != null)
    {
      return (false);
    }
    if (this.comments != null ? !this.comments.equals(that.comments) : that.comments != null)
    {
      return (false);
    }
    if (this.historicalinfo != null ? !this.historicalinfo.equals(that.historicalinfo) : that.historicalinfo != null)
    {
      return (false);
    }
    
    return (true);
  }

  @Override
  public int hashCode()
  {
    int lnResult = this.symbol != null ? this.symbol.hashCode() : 0;

    lnResult = 31 * lnResult + (this.description != null ? this.description.hashCode() : 0);
    lnResult = 31 * lnResult + (this.lasttrade != null ? this.lasttrade.hashCode() : 0);
    lnResult = 31 * lnResult + (this.tradetime != null ? this.tradetime.hashCode() : 0);
    lnResult = 31 * lnResult + (this.differential != null ? this.differential.hashCode() : 0);
    lnResult = 31 * lnResult + (this.previousclose != null ? this.previousclose.hashCode() : 0);
    lnResult = 31 * lnResult + (this.opened != null ? this.opened.hashCode() : 0);
    lnResult = 31 * lnResult + (this.bidding != null ? this.bidding.hashCode() : 0);
    lnResult = 31 * lnResult + (this.asking != null ? this.asking.hashCode() : 0);
    lnResult = 31 * lnResult + (this.targetestimate != null ? this.targetestimate.hashCode() : 0);
    lnResult = 31 * lnResult + (this.dayrange != null ? this.dayrange.hashCode() : 0);
    lnResult = 31 * lnResult + (this.yearrange != null ? this.yearrange.hashCode() : 0);
    lnResult = 31 * lnResult + (this.volume != null ? this.volume.hashCode() : 0);
    lnResult = 31 * lnResult + (this.averagevolume != null ? this.averagevolume.hashCode() : 0);
    lnResult = 31 * lnResult + (this.marketcap != null ? this.marketcap.hashCode() : 0);
    lnResult = 31 * lnResult + (this.priceearnings != null ? this.priceearnings.hashCode() : 0);
    lnResult = 31 * lnResult + (this.earningspershare != null ? this.earningspershare.hashCode() : 0);
    lnResult = 31 * lnResult + (this.dividendyield != null ? this.dividendyield.hashCode() : 0);
    lnResult = 31 * lnResult + (this.comments != null ? this.comments.hashCode() : 0);
    lnResult = 31 * lnResult + (this.historicalinfo != null ? this.historicalinfo.hashCode() : 0);

    return (lnResult);
  }
}
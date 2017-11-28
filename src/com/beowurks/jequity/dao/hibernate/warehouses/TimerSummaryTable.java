/*
 * JEquity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */
package com.beowurks.jequity.dao.hibernate.warehouses;

import com.beowurks.jequity.dao.hibernate.FinancialEntity;
import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.tableview.SummaryProperty;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.view.table.TableViewPlus;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// From http://stackoverflow.com/questions/32001/resettable-java-timer
public class TimerSummaryTable
{
  class SummarySubAmount
  {
    protected Boolean flRetirement;
    protected String fcType;
    protected String fcCategory;
    protected Double fnSubTotal;
  }

  class SummarySubList
  {
    protected String fcLabel;
    protected String fcConverted;
    protected Double fnSubTotal;
  }

  public static TimerSummaryTable INSTANCE = new TimerSummaryTable();

  private final Vector<SummarySubAmount> foSummaryList = new Vector<>();

  private Timer foTimer = null;

  private final ObservableList<SummaryProperty> foDataList = FXCollections.observableArrayList();

  private TableViewPlus foSummaryTable;

  // ---------------------------------------------------------------------------------------------------------------------
  private TimerSummaryTable()
  {
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setTable(final TableViewPlus toSummaryTable)
  {
    this.foSummaryTable = toSummaryTable;

    if (this.foSummaryTable.getItems() != this.foDataList)
    {
      this.foSummaryTable.setItems(this.foDataList);
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void scheduleDataRefresh(final String tcType, final String tcCategory)
  {
    Platform.runLater(() ->
        this.foSummaryTable.setStyle("-fx-opacity: 0.25;"));

    if (this.foTimer != null)
    {
      this.foTimer.cancel();
    }

    // From
    // http://stackoverflow.com/questions/1041675/java-timer
    // and
    // http://stackoverflow.com/questions/10335784/restart-timer-in-java
    this.foTimer = new Timer();
    this.foTimer.schedule(
        new TimerTask()
        {
          @Override
          public void run()
          {
            TimerSummaryTable.this.refreshSummaryTable(tcType, tcCategory);
          }
        }, Constants.TIMER_SUMMARY_UPDATE_DELAY);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void refreshSummaryTable(final String tcType, final String tcCategory)
  {
    this.refreshSummaryList();

    this.updateDataList(tcType, tcCategory);

    Platform.runLater(() ->
        this.foSummaryTable.setStyle("-fx-opacity: 1.0;"));
  }

  // -----------------------------------------------------------------------------
  private void refreshSummaryList()
  {
    this.foSummaryList.clear();

    final Session loSession = HibernateUtil.INSTANCE.getSession();

    final List<FinancialEntity> loList = this.getQuery(loSession).list();

    for (final FinancialEntity loRow : loList)
    {
      final String lcType = loRow.getType();
      final String lcCategory = loRow.getCategory();
      final double lnPrice = loRow.getPrice();
      final double lnShares = loRow.getShares();
      final boolean llRetirement = loRow.getRetirement();

      final SummarySubAmount loSumAmount = new SummarySubAmount();

      loSumAmount.fcType = this.standardizeDelimitedString(lcType, true);
      loSumAmount.fcCategory = this.standardizeDelimitedString(lcCategory, true);
      loSumAmount.fnSubTotal = lnPrice * lnShares;
      loSumAmount.flRetirement = llRetirement;

      this.foSummaryList.add(loSumAmount);
    }

    loSession.close();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private NativeQuery getQuery(final Session toSession)
  {
    final HibernateUtil loHibernate = HibernateUtil.INSTANCE;

    final String lcSQL = String.format("SELECT * FROM %s WHERE groupid = :groupid", loHibernate.getTableFinancial());
    final NativeQuery loQuery = toSession.createNativeQuery(lcSQL)
        .addEntity(FinancialEntity.class)
        .setParameter("groupid", loHibernate.getGroupID().intValue());

    return (loQuery);
  }

  // -----------------------------------------------------------------------------
  private void updateDataList(final String tcType, final String tcCategory)
  {
    final Vector<SummarySubList> loType = this.setSubListVector(tcType);
    final Vector<SummarySubList> loCategory = this.setSubListVector(tcCategory);

    // Now calculate all of the sub totals.
    double lnTotal = 0;
    double lnRetirement = 0;
    double lnNonRetirement = 0;

    final int lnCount = this.foSummaryList.size();
    for (final SummarySubAmount loSumAmount : this.foSummaryList)
    {
      lnTotal += loSumAmount.fnSubTotal;
      if (loSumAmount.flRetirement)
      {
        lnRetirement += loSumAmount.fnSubTotal;
      }
      else
      {
        lnNonRetirement += loSumAmount.fnSubTotal;
      }

      if (loType != null)
      {
        final int lnSize = loType.size();
        for (final SummarySubList loSubList : loType)
        {
          if (loSumAmount.fcType.contains(loSubList.fcConverted))
          {
            loSubList.fnSubTotal += loSumAmount.fnSubTotal;
          }
        }
      }

      if (loCategory != null)
      {
        final int lnSize = loCategory.size();
        for (final SummarySubList loSubList : loCategory)
        {
          if (loSumAmount.fcCategory.contains(loSubList.fcConverted))
          {
            loSubList.fnSubTotal += loSumAmount.fnSubTotal;
          }
        }
      }
    }

    this.foDataList.clear();
    this.foDataList.add(new SummaryProperty("Total", lnTotal));
    this.foDataList.add(new SummaryProperty("Retirement (Total)", lnRetirement));
    this.foDataList.add(new SummaryProperty("Non-Retirement (Total)", lnNonRetirement));

    this.addSummaryToList(loType);
    this.addSummaryToList(loCategory);

    this.foSummaryTable.resizeColumnsToFit();
  }

  // -----------------------------------------------------------------------------
  private void addSummaryToList(final Vector<SummarySubList> toSubList)
  {
    if (toSubList != null)
    {
      final int lnSize = toSubList.size();
      for (final SummarySubList loSubList : toSubList)
      {
        this.foDataList.add(new SummaryProperty(loSubList.fcLabel, loSubList.fnSubTotal));
      }
    }

  }

  // -----------------------------------------------------------------------------
  private Vector<SummarySubList> setSubListVector(final String tcValue)
  {
    if (tcValue == null)
    {
      return (null);
    }

    final String[] laStrings = this.standardizeDelimitedString(tcValue, false).split(Constants.CATEGORY_TYPE_DELIMITER, 0);
    Vector<SummarySubList> loVector = null;

    if (!laStrings[0].isEmpty())
    {
      final int lnCount = laStrings.length;
      loVector = new Vector<>();

      final StringBuilder lcValue = new StringBuilder("");
      for (int i = 0; i < lnCount; ++i)
      {
        lcValue.append(laStrings[i].trim());

        final SummarySubList loSubList = new SummarySubList();
        final String lcTempValue = lcValue.toString();

        loSubList.fcLabel = lcTempValue;
        loSubList.fcConverted = lcTempValue.toUpperCase();
        loSubList.fnSubTotal = 0.0;
        loVector.add(loSubList);

        if (i < (lnCount - 1))
        {
          lcValue.append(Constants.CATEGORY_TYPE_DELIMITER);
        }
      }
    }

    return (loVector);
  }


  // ---------------------------------------------------------------------------------------------------------------------
  private static String standardizeDelimitedString(final String tcValue, final boolean tlUpperCase)
  {
    if (tcValue == null)
    {
      return ("");
    }

    String lcValue = tcValue.trim();

    // Get rid of any spaces next to the delimiters.
    while (lcValue.contains(Constants.CATEGORY_TYPE_DELIMITER + " "))
    {
      lcValue = lcValue.replace(Constants.CATEGORY_TYPE_DELIMITER + " ", Constants.CATEGORY_TYPE_DELIMITER);
    }
    while (lcValue.contains(" " + Constants.CATEGORY_TYPE_DELIMITER))
    {
      lcValue = lcValue.replace(Constants.CATEGORY_TYPE_DELIMITER + " ", Constants.CATEGORY_TYPE_DELIMITER);
    }

    if (tlUpperCase)
    {
      return (lcValue.trim().toUpperCase());
    }

    return (lcValue.trim());
  }

// ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

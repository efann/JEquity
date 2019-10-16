/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */
package com.beowurks.jequity.dao.hibernate.threads;

import com.beowurks.jequity.dao.hibernate.FinancialEntity;
import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.tableview.SummaryProperty;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import com.beowurks.jequity.view.table.TableViewPlus;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableRow;
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
    protected Boolean flTaxable1099;
    protected String fcAccount;
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

  public static final TimerSummaryTable INSTANCE = new TimerSummaryTable();

  private final Vector<SummarySubAmount> foSummaryList = new Vector<>();

  private Timer foTimer = null;

  private final ObservableList<SummaryProperty> foDataList = FXCollections.observableArrayList();

  private TableViewPlus foSummaryTable;

  private String fcCurrentStyle = "";

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

    if (this.foSummaryTable.getRowFactory() != null)
    {
      return;
    }

    this.foSummaryTable.setRowFactory(loTable -> new TableRow<SummaryProperty>()
    {
      @Override
      public void updateItem(final SummaryProperty toItem, final boolean tlEmpty)
      {
        super.updateItem(toItem, tlEmpty);
        if (toItem == null)
        {
          this.setStyle("");
          return;
        }

        final TimerSummaryTable loThis = TimerSummaryTable.this;
        final String lcDescription = toItem.getSummaryDescription();

        if (lcDescription.equals(Constants.SUMMARY_TABLE_TOTAL))
        {
          this.setStyle("-fx-background-color: wheat;");
        }
        else if ((lcDescription.equals(Constants.SUMMARY_TABLE_RETIREMENT)) || (lcDescription.equals(Constants.SUMMARY_TABLE_NON_RETIREMENT)))
        {
          this.setStyle("-fx-background-color: white;");
        }
        // Tax Status
        else if (lcDescription.contains("Tax "))
        {
          this.setStyle("-fx-background-color: lightyellow;");
        }
        else if (lcDescription.equals(Constants.SUMMARY_TABLE_ACCOUNT))
        {
          loThis.fcCurrentStyle = "-fx-background-color: lightcyan;";
          this.setStyle(loThis.fcCurrentStyle + " -fx-font-weight: bold;");
        }
        else if (lcDescription.equals(Constants.SUMMARY_TABLE_TYPE))
        {
          loThis.fcCurrentStyle = "-fx-background-color: lightgray;";
          this.setStyle(loThis.fcCurrentStyle + " -fx-font-weight: bold;");
        }
        else if (lcDescription.equals(Constants.SUMMARY_TABLE_CATEGORY))
        {
          loThis.fcCurrentStyle = "-fx-background-color: white;";
          this.setStyle(loThis.fcCurrentStyle + " -fx-font-weight: bold;");
        }
        else
        {
          this.setStyle(loThis.fcCurrentStyle);
        }
      }
    });

  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void scheduleDataRefresh(final String tcAccount, final String tcType, final String tcCategory)
  {
    Misc.setStatusText(ProgressBar.INDETERMINATE_PROGRESS);
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
            TimerSummaryTable.this.refreshSummaryTable(tcAccount, tcType, tcCategory);
          }
        }, Constants.TIMER_SUMMARY_UPDATE_DELAY);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void refreshSummaryTable(final String tcAccount, final String tcType, final String tcCategory)
  {
    this.refreshSummaryList();

    this.updateDataList(tcAccount, tcType, tcCategory);

    Platform.runLater(() ->
        this.foSummaryTable.setStyle("-fx-opacity: 1.0;"));

    Misc.setStatusText(0.0);
  }

  // -----------------------------------------------------------------------------
  private void refreshSummaryList()
  {
    this.foSummaryList.clear();

    final Session loSession = HibernateUtil.INSTANCE.getSession();

    final List<FinancialEntity> loList = this.getQuery(loSession).list();

    for (final FinancialEntity loRow : loList)
    {
      final String lcAccount = loRow.getAccount().trim();
      final String lcType = loRow.getType();
      final String lcCategory = loRow.getCategory();
      final double lnPrice = loRow.getPrice();
      final double lnShares = loRow.getShares();
      final boolean llRetirement = loRow.getRetirement();
      final boolean llTaxable1099 = loRow.getTaxable1099();

      final SummarySubAmount loSumAmount = new SummarySubAmount();

      loSumAmount.fcAccount = lcAccount;
      loSumAmount.fcType = TimerSummaryTable.standardizeDelimitedString(lcType, true);
      loSumAmount.fcCategory = TimerSummaryTable.standardizeDelimitedString(lcCategory, true);
      loSumAmount.fnSubTotal = lnPrice * lnShares;
      loSumAmount.flRetirement = llRetirement;
      loSumAmount.flTaxable1099 = llTaxable1099;

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
  private void updateDataList(final String tcAccount, final String tcType, final String tcCategory)
  {
    final Vector<SummarySubList> loType = this.setSubListVector(tcType);
    final Vector<SummarySubList> loCategory = this.setSubListVector(tcCategory);

    // Now calculate all of the sub totals.
    double lnTotal = 0;
    double lnRetirement = 0;
    double lnNonRetirement = 0;
    double lnTaxable1099 = 0;
    double lnNonTaxable1099 = 0;
    double lnAccount = 0;

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

      if (loSumAmount.flTaxable1099)
      {
        lnTaxable1099 += loSumAmount.fnSubTotal;
      }
      else
      {
        lnNonTaxable1099 += loSumAmount.fnSubTotal;
      }

      if ((tcAccount != null) && (!tcAccount.isEmpty()) && (tcAccount.compareTo(loSumAmount.fcAccount) == 0))
      {
        lnAccount += loSumAmount.fnSubTotal;
      }

      if (loType != null)
      {
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
    this.foDataList.add(new SummaryProperty(Constants.SUMMARY_TABLE_TOTAL, lnTotal));

    this.foDataList.add(new SummaryProperty(Constants.SUMMARY_TABLE_RETIREMENT, lnRetirement));
    this.foDataList.add(new SummaryProperty(Constants.SUMMARY_TABLE_NON_RETIREMENT, lnNonRetirement));

    this.foDataList.add(new SummaryProperty(Constants.SUMMARY_TABLE_TAXABLE1099, lnTaxable1099));
    this.foDataList.add(new SummaryProperty(Constants.SUMMARY_TABLE_NON_TAXABLE1099, lnNonTaxable1099));

    if ((tcAccount != null) && (!tcAccount.isEmpty()))
    {
      this.foDataList.add(new SummaryProperty(Constants.SUMMARY_TABLE_ACCOUNT));
      this.foDataList.add(new SummaryProperty(tcAccount, lnAccount));
    }

    if (loType != null)
    {
      this.foDataList.add(new SummaryProperty(Constants.SUMMARY_TABLE_TYPE));
      this.addSummaryToList(loType);
    }
    if (loCategory != null)
    {
      this.foDataList.add(new SummaryProperty(Constants.SUMMARY_TABLE_CATEGORY));
      this.addSummaryToList(loCategory);
    }

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

    final String[] laStrings = TimerSummaryTable.standardizeDelimitedString(tcValue, false).split(Constants.CATEGORY_TYPE_DELIMITER, 0);
    Vector<SummarySubList> loVector = null;

    if (!laStrings[0].isEmpty())
    {
      final int lnCount = laStrings.length;
      loVector = new Vector<>();

      final StringBuilder lcValue = new StringBuilder();
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

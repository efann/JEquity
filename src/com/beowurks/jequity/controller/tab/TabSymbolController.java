/*
 * JEquity
 * Copyright(c) 2008-2018, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.controller.tab;

import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.hibernate.SymbolEntity;
import com.beowurks.jequity.dao.tableview.SymbolProperty;
import com.beowurks.jequity.view.cell.CurrencyTableCell;
import com.beowurks.jequity.view.cell.DoubleTableCell;
import com.beowurks.jequity.view.cell.IntegerTableCell;
import com.beowurks.jequity.view.table.TableViewPlus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;

import java.util.List;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class TabSymbolController extends TabBaseController
{
  private final ObservableList<SymbolProperty> foDataList = FXCollections.observableArrayList();

  @FXML
  private TableViewPlus tblSymbol;
  @FXML
  private TableColumn colSymbol;
  @FXML
  private TableColumn colDescription;
  @FXML
  private TableColumn colAsking;
  @FXML
  private TableColumn colAverageVolume;
  @FXML
  private TableColumn colBidding;
  @FXML
  private TableColumn colDifferential;
  @FXML
  private TableColumn colDayRange;
  @FXML
  private TableColumn colDividendYield;
  @FXML
  private TableColumn colEarningsPerShare;
  @FXML
  private TableColumn colLastTrade;
  @FXML
  private TableColumn colMarketCap;
  @FXML
  private TableColumn colOpened;
  @FXML
  private TableColumn colPreviousClose;
  @FXML
  private TableColumn colPriceEarnings;
  @FXML
  private TableColumn colTargetEstimate;
  @FXML
  private TableColumn colTradeTime;
  @FXML
  private TableColumn colVolume;
  @FXML
  private TableColumn colYearRange;
  @FXML
  private TableColumn colComments;
  @FXML
  private TableColumn colHistoricalInfo;


  // ---------------------------------------------------------------------------------------------------------------------
  // From https://stackoverflow.com/questions/34785417/javafx-fxml-controller-constructor-vs-initialize-method
  @FXML
  public void initialize()
  {
    this.setupTables();
  }

  // -----------------------------------------------------------------------------
  public void refreshData()
  {
    final SymbolProperty loCurrent = (SymbolProperty) this.tblSymbol.getSelectionModel().getSelectedItem();

    final Session loSession = HibernateUtil.INSTANCE.getSession();

    final List<SymbolEntity> loList = this.getQuery(loSession).list();

    this.foDataList.clear();
    for (final SymbolEntity loRow : loList)
    {
      this.foDataList.add(new SymbolProperty(loRow.getSymbol(), loRow.getDescription(), loRow.getLastTrade(), loRow.getTradeTime(), loRow.getDifferential(),
          loRow.getPreviousClose(), loRow.getOpened(), loRow.getBidding(), loRow.getAsking(), loRow.getTargetEstimate(),
          loRow.getDayRange(), loRow.getYearRange(), loRow.getVolume(), loRow.getAverageVolume(), loRow.getMarketCap(),
          loRow.getPriceEarnings(), loRow.getEarningsPerShare(), loRow.getDividendYield(), loRow.getComments(), loRow.getHistoricalInfo()));
    }

    if (this.tblSymbol.getItems() != this.foDataList)
    {
      this.tblSymbol.setItems(this.foDataList);
    }

    if (loCurrent != null)
    {
      final int lnRows = this.tblSymbol.getItems().size();
      for (int i = 0; i < lnRows; ++i)
      {
        final String lcSymbol = ((SymbolProperty) this.tblSymbol.getItems().get(i)).getSymbol();
        if (loCurrent.getSymbol().equals(lcSymbol))
        {
          this.tblSymbol.getSelectionModel().select(i);
          this.tblSymbol.scrollTo(i);
          break;
        }
      }
    }

    this.tblSymbol.resizeColumnsToFit();

    loSession.close();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void setupTables()
  {
    this.colSymbol.setCellValueFactory(new PropertyValueFactory<SymbolProperty, String>("symbol"));
    this.colDescription.setCellValueFactory(new PropertyValueFactory<SymbolProperty, String>("description"));
    this.colAsking.setCellValueFactory(new PropertyValueFactory<SymbolProperty, Double>("asking"));
    this.colAverageVolume.setCellValueFactory(new PropertyValueFactory<SymbolProperty, Integer>("averagevolume"));
    this.colBidding.setCellValueFactory(new PropertyValueFactory<SymbolProperty, Double>("bidding"));
    this.colDifferential.setCellValueFactory(new PropertyValueFactory<SymbolProperty, Double>("differential"));
    this.colDayRange.setCellValueFactory(new PropertyValueFactory<SymbolProperty, String>("dayrange"));
    this.colDividendYield.setCellValueFactory(new PropertyValueFactory<SymbolProperty, String>("dividendyield"));
    this.colEarningsPerShare.setCellValueFactory(new PropertyValueFactory<SymbolProperty, Double>("earningspershare"));
    this.colLastTrade.setCellValueFactory(new PropertyValueFactory<SymbolProperty, Double>("lasttrade"));
    this.colMarketCap.setCellValueFactory(new PropertyValueFactory<SymbolProperty, String>("marketcap"));
    this.colOpened.setCellValueFactory(new PropertyValueFactory<SymbolProperty, Double>("opened"));
    this.colPreviousClose.setCellValueFactory(new PropertyValueFactory<SymbolProperty, Double>("previousclose"));
    this.colPriceEarnings.setCellValueFactory(new PropertyValueFactory<SymbolProperty, Double>("priceearnings"));
    this.colTargetEstimate.setCellValueFactory(new PropertyValueFactory<SymbolProperty, Double>("targetestimate"));
    this.colTradeTime.setCellValueFactory(new PropertyValueFactory<SymbolProperty, String>("tradetime"));
    this.colVolume.setCellValueFactory(new PropertyValueFactory<SymbolProperty, Integer>("volume"));
    this.colYearRange.setCellValueFactory(new PropertyValueFactory<SymbolProperty, String>("yearrange"));
    this.colComments.setCellValueFactory(new PropertyValueFactory<SymbolProperty, String>("comments"));
    this.colHistoricalInfo.setCellValueFactory(new PropertyValueFactory<SymbolProperty, String>("historicalinfo"));

    this.colAverageVolume.setCellFactory(tc -> new IntegerTableCell());
    this.colVolume.setCellFactory(tc -> new IntegerTableCell());

    this.colDifferential.setCellFactory(tc -> new DoubleTableCell());

    this.colAsking.setCellFactory(tc -> new CurrencyTableCell());
    this.colBidding.setCellFactory(tc -> new CurrencyTableCell());
    this.colEarningsPerShare.setCellFactory(tc -> new CurrencyTableCell());
    this.colLastTrade.setCellFactory(tc -> new CurrencyTableCell());
    this.colOpened.setCellFactory(tc -> new CurrencyTableCell());
    this.colPreviousClose.setCellFactory(tc -> new CurrencyTableCell());
    this.colPriceEarnings.setCellFactory(tc -> new CurrencyTableCell());
    this.colTargetEstimate.setCellFactory(tc -> new CurrencyTableCell());

    this.tblSymbol.getItems().clear();
    this.tblSymbol.setColumnResizePolicy((param -> true));
  }

  // -----------------------------------------------------------------------------
  protected NativeQuery getQuery(final Session toSession)
  {
    final String lcSQL = String.format("SELECT * FROM %s", HibernateUtil.INSTANCE.getTableSymbol());

    final NativeQuery loQuery = toSession.createNativeQuery(lcSQL)
        .addEntity(SymbolEntity.class);

    return (loQuery);
  }

  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

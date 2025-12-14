/*
 * JEquity
 * Copyright(c) 2008-2025, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.controller.tab;

import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.hibernate.SymbolEntity;
import com.beowurks.jequity.dao.tableview.SymbolProperty;
import com.beowurks.jequity.view.cell.CurrencyTableCell;
import com.beowurks.jequity.view.cell.HyperLinkLabelTableCell;
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
  private TableColumn colLastTrade;
  @FXML
  private TableColumn colTradeTime;
  @FXML
  private TableColumn colComments;

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
      this.foDataList.add(new SymbolProperty(loRow.getSymbol(), loRow.getDescription(), loRow.getLastTrade(), loRow.getTradeTime(),
        loRow.getComments(), loRow.getHistoricalInfo()));
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
    // In case of data refresh and column(s) have already been sorted.
    // And it's okay if no column(s) have been sorted.
    this.tblSymbol.sort();

    loSession.close();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void setupTables()
  {
    this.colSymbol.setCellValueFactory(new PropertyValueFactory<SymbolProperty, String>("symbol"));
    this.colDescription.setCellValueFactory(new PropertyValueFactory<SymbolProperty, String>("description"));
    this.colLastTrade.setCellValueFactory(new PropertyValueFactory<SymbolProperty, Double>("lasttrade"));
    this.colTradeTime.setCellValueFactory(new PropertyValueFactory<SymbolProperty, String>("tradetime"));
    this.colComments.setCellValueFactory(new PropertyValueFactory<SymbolProperty, String>("comments"));
    /*
      Not showing historicalinfo as it contains XML info for the Historical tab. Plus it's multi-line
      which makes the rows awkwardly tall.
    */

    this.colLastTrade.setCellFactory(tc -> new CurrencyTableCell());
    this.colComments.setCellFactory(tc -> new HyperLinkLabelTableCell());

    this.tblSymbol.getItems().clear();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public TableViewPlus getTable()
  {
    return (this.tblSymbol);
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

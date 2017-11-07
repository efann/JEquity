/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.controller.table;

import com.beowurks.jequity.controller.cell.CurrencyTableCell;
import com.beowurks.jequity.controller.cell.DateTableCell;
import com.beowurks.jequity.controller.cell.DoubleTableCell;
import com.beowurks.jequity.dao.hibernate.FinancialEntity;
import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.tableview.FinancialProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;

import java.util.List;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class TableFinancialController extends TableBaseController
{
  private final ObservableList<FinancialProperty> foDataList = FXCollections.observableArrayList();

  @FXML
  private TableView tblFinancial;

  @FXML
  private TableColumn colID;

  @FXML
  private TableColumn colDescription;

  @FXML
  private TableColumn colAccount;
  @FXML
  private TableColumn colType;
  @FXML
  private TableColumn colCategory;
  @FXML
  private TableColumn colShares;
  @FXML
  private TableColumn colPrice;
  @FXML
  private TableColumn colValuationDate;
  @FXML
  private TableColumn colRetirement;
  @FXML
  private TableColumn colSymbol;
  @FXML
  private TableColumn colSharesPrice;


  // ---------------------------------------------------------------------------------------------------------------------
  // From https://stackoverflow.com/questions/34785417/javafx-fxml-controller-constructor-vs-initialize-method
  @FXML
  public void initialize()
  {
    this.setupTable();
  }

  // -----------------------------------------------------------------------------
  public void refreshData()
  {
    this.foDataList.clear();

    final Session loSession = HibernateUtil.INSTANCE.getSession();

    final List<FinancialEntity> loList = this.getQuery(loSession).list();

    for (final FinancialEntity loRow : loList)
    {
      this.foDataList.add(new FinancialProperty(loRow.getFinancialID(), loRow.getDescription(), loRow.getAccount(),
          loRow.getType(), loRow.getCategory(), loRow.getShares(), loRow.getPrice(), loRow.getValuationDate(), loRow.getRetirement(),
          loRow.getSymbol()));
    }

    this.tblFinancial.setItems(this.foDataList);

    loSession.close();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void setupTable()
  {
    this.tblFinancial.setEditable(false);

    this.colID.setCellValueFactory(new PropertyValueFactory<FinancialProperty, Integer>("id"));
    this.colDescription.setCellValueFactory(new PropertyValueFactory<FinancialProperty, String>("description"));
    this.colAccount.setCellValueFactory(new PropertyValueFactory<FinancialProperty, String>("account"));
    this.colType.setCellValueFactory(new PropertyValueFactory<FinancialProperty, String>("type"));
    this.colCategory.setCellValueFactory(new PropertyValueFactory<FinancialProperty, String>("category"));
    this.colShares.setCellValueFactory(new PropertyValueFactory<FinancialProperty, Double>("shares"));
    this.colPrice.setCellValueFactory(new PropertyValueFactory<FinancialProperty, Double>("price"));
    this.colValuationDate.setCellValueFactory(new PropertyValueFactory<FinancialProperty, String>("valuationdate"));
    this.colRetirement.setCellValueFactory(new PropertyValueFactory<FinancialProperty, Boolean>("retirement"));
    this.colSymbol.setCellValueFactory(new PropertyValueFactory<FinancialProperty, String>("symbol"));
    this.colSharesPrice.setCellValueFactory(new PropertyValueFactory<FinancialProperty, Double>("total"));

    // Add check box to the grid.
    this.colRetirement.setCellFactory(tc -> new CheckBoxTableCell<FinancialProperty, Boolean>());

    this.colValuationDate.setCellFactory(tc -> new DateTableCell());

    this.colPrice.setCellFactory(tc -> new CurrencyTableCell());
    this.colSharesPrice.setCellFactory(tc -> new CurrencyTableCell());

    this.colShares.setCellFactory(tc -> new DoubleTableCell());

    this.tblFinancial.getItems().clear();
    this.tblFinancial.setColumnResizePolicy((param -> true));
  }

  // -----------------------------------------------------------------------------
  protected NativeQuery getQuery(final Session toSession)
  {
    final HibernateUtil loHibernate = HibernateUtil.INSTANCE;

    final String lcSQL = String.format("SELECT * FROM %s WHERE groupid = :groupid", loHibernate.getTableFinancial());
    final NativeQuery loQuery = toSession.createNativeQuery(lcSQL)
        .addEntity(FinancialEntity.class)
        .setParameter("groupid", loHibernate.getGroupID().intValue());

    return (loQuery);
  }

  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

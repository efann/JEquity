/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.controller.table;

import com.beowurks.jequity.dao.hibernate.FinancialEntity;
import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.tableview.FinancialProperty;
import com.beowurks.jequity.utility.Misc;
import com.beowurks.jequity.view.cell.CurrencyTableCell;
import com.beowurks.jequity.view.cell.DateTableCell;
import com.beowurks.jequity.view.cell.DoubleTableCell;
import com.beowurks.jequity.view.table.TableViewPlus;
import com.beowurks.jequity.view.textfield.NumberTextField;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;

import java.sql.Date;
import java.util.List;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class TableFinancialController extends TableModifyController
{
  private final ObservableList<FinancialProperty> foDataList = FXCollections.observableArrayList();

  @FXML
  private TableViewPlus tblFinancial;

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

  @FXML
  private TextField txtDescription;
  @FXML
  private TextField txtAccount;
  @FXML
  private TextField txtType;
  @FXML
  private TextField txtCategory;
  @FXML
  private NumberTextField txtShares;
  @FXML
  private NumberTextField txtPrice;
  @FXML
  private DatePicker txtDate;
  @FXML
  private TextField txtSymbol;
  @FXML
  private Label lblSymbolURL;

  @FXML
  private CheckBox chkRetirement;

  @FXML
  private TextArea txtComments;

  private FinancialProperty foCurrentFinancialProperty = null;

  // ---------------------------------------------------------------------------------------------------------------------
  // From https://stackoverflow.com/questions/34785417/javafx-fxml-controller-constructor-vs-initialize-method
  @FXML
  public void initialize()
  {
    this.setupTable();

    this.setupListeners();
    this.setupTooltips();

    this.resetComponentsOnModify(false);

    this.resetComponentsOnModify(false);

  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void setupListeners()
  {
    this.btnModify.setOnAction(toActionEvent -> TableFinancialController.this.modifyRow(this.foCurrentFinancialProperty));
    this.btnSave.setOnAction(toActionEvent -> TableFinancialController.this.saveRow());
    this.btnCancel.setOnAction(toActionEvent -> TableFinancialController.this.cancelRow());

    this.btnCreate.setOnAction(toActionEvent -> TableFinancialController.this.insertRow());
    this.btnRemove.setOnAction(toActionEvent -> TableFinancialController.this.removeRow());

    this.tblFinancial.getSelectionModel().selectedItemProperty().addListener((ChangeListener<FinancialProperty>) (observable, toOldRow, toNewRow) -> {
      if (toNewRow != null)
      {
        this.foCurrentFinancialProperty = toNewRow;
        TableFinancialController.this.updateComponentsContent();
      }
    });

  }

  // -----------------------------------------------------------------------------
  public void refreshData()
  {
    this.foDataList.clear();

    final Session loSession = HibernateUtil.INSTANCE.getSession();

    final List<FinancialEntity> loList = this.getQuery(loSession).list();

    for (final FinancialEntity loRow : loList)
    {
      this.foDataList.add(new FinancialProperty(loRow.getGroupID(), loRow.getFinancialID(), loRow.getDescription(), loRow.getAccount(),
          loRow.getType(), loRow.getCategory(), loRow.getShares(), loRow.getPrice(), loRow.getValuationDate(), loRow.getRetirement(),
          loRow.getSymbol(), loRow.getComments()));
    }

    this.tblFinancial.setItems(this.foDataList);
    this.tblFinancial.resizeColumnsToFit();

    loSession.close();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void setupTable()
  {
    this.colID.setCellValueFactory(new PropertyValueFactory<FinancialProperty, Integer>("financialid"));
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
  protected void insertRow()
  {
    final FinancialEntity loNewEntity = new FinancialEntity();
    final HibernateUtil loHibernate = HibernateUtil.INSTANCE;
    loNewEntity.setGroupID(loHibernate.getGroupID());

    if (loHibernate.insertRow(loNewEntity))
    {
      final FinancialProperty loRecord = new FinancialProperty(loNewEntity.getGroupID(), loNewEntity.getFinancialID(), loNewEntity.getDescription(), loNewEntity.getAccount(),
          loNewEntity.getType(), loNewEntity.getCategory(), loNewEntity.getShares(), loNewEntity.getPrice(), loNewEntity.getValuationDate(), loNewEntity.getRetirement(),
          loNewEntity.getSymbol(), loNewEntity.getComments());

      this.foDataList.add(loRecord);
      this.tblFinancial.getSelectionModel().select(loRecord);
      this.tblFinancial.scrollTo(loRecord);

      this.foCurrentFinancialProperty = loRecord;
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void saveRow()
  {
    final FinancialProperty loProp = this.foCurrentFinancialProperty;

    loProp.setDescription(this.txtDescription.getText().trim());
    loProp.setAccount(this.txtAccount.getText().trim());
    loProp.setType(this.txtType.getText().trim());
    loProp.setCategory(this.txtCategory.getText().trim());
    loProp.setShares(Double.parseDouble(this.txtShares.getText().trim()));
    loProp.setPrice(Double.parseDouble(this.txtPrice.getText().trim()));
    loProp.setValuationDate(Date.valueOf(this.txtDate.getValue()));
    loProp.setSymbol(this.txtSymbol.getText().trim());
    loProp.setRetirement(this.chkRetirement.isSelected());
    loProp.setComments(this.txtComments.getText().trim());

    this.resetComponentsOnModify(false);
    if (HibernateUtil.INSTANCE.updateRow(loProp.toEntity()))
    {
      Misc.setStatusText("The data has been saved.");
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void removeRow()
  {
    if (this.foCurrentFinancialProperty == null)
    {
      Misc.errorMessage("You need to select a record before modifying it.");
      return;
    }

    final FinancialProperty loProp = this.foCurrentFinancialProperty;
    final String lcMessage = String.format("Do you want to remove the following record?\n\n%s (# %d)\n\n", loProp.getDescription(), loProp.getFinancialID());
    if (Misc.yesNo(lcMessage))
    {
      if (HibernateUtil.INSTANCE.removeRow(loProp.toEntity()))
      {
        // You must reset this.foCurrentFinancialProperty before calling remove.
        // The grid removes then selects another row and then resets this.foCurrentFinancialProperty
        this.foCurrentFinancialProperty = null;

        this.tblFinancial.getItems().remove(loProp);
      }
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void updateComponentsContent()
  {
    final FinancialProperty loProp = this.foCurrentFinancialProperty;

    this.txtDescription.setText(loProp.getDescription());
    this.txtAccount.setText(loProp.getAccount());
    this.txtType.setText(loProp.getType());
    this.txtCategory.setText(loProp.getCategory());
    this.txtShares.setText(Double.toString(loProp.getShares()));
    this.txtPrice.setText(Double.toString(loProp.getPrice()));
    this.txtDate.setValue(loProp.getValuationDate().toLocalDate());
    this.txtSymbol.setText(loProp.getSymbol());
    this.chkRetirement.setSelected(loProp.getRetirement());
    this.txtComments.setText(loProp.getComments());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void resetComponentsOnModify(final boolean tlModify)
  {
    super.resetComponentsOnModify(tlModify);

    this.tblFinancial.setDisable(tlModify);

    // Signifies editing is enabled so move cursor to the first component.
    if (tlModify)
    {
      this.txtDescription.requestFocus();
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void resetTextFields(final boolean tlModify)
  {
    this.setEditable(this.txtDescription, tlModify);
    this.setEditable(this.txtAccount, tlModify);
    this.setEditable(this.txtType, tlModify);
    this.setEditable(this.txtCategory, tlModify);
    this.setEditable(this.txtShares, tlModify);
    this.setEditable(this.txtPrice, tlModify);
    this.setEditable(this.txtDate, tlModify);
    this.setEditable(this.txtSymbol, tlModify);
    this.setEditable(this.chkRetirement, tlModify);
    this.setEditable(this.txtComments, tlModify);
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

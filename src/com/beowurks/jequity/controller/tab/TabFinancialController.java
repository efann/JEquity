/*
 * JEquity
 * Copyright(c) 2008-2017, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.controller.tab;

import com.beowurks.jequity.dao.hibernate.FinancialEntity;
import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.hibernate.warehouses.ThreadDownloadSymbolInfo;
import com.beowurks.jequity.dao.hibernate.warehouses.TimerSummaryTable;
import com.beowurks.jequity.dao.tableview.FinancialProperty;
import com.beowurks.jequity.dao.tableview.SummaryProperty;
import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.Misc;
import com.beowurks.jequity.view.cell.CurrencyTableCell;
import com.beowurks.jequity.view.cell.DateTableCell;
import com.beowurks.jequity.view.cell.DoubleTableCell;
import com.beowurks.jequity.view.table.TableViewPlus;
import com.beowurks.jequity.view.textfield.NumberTextField;
import com.beowurks.jequity.view.textfield.UpperCaseTextField;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class TabFinancialController extends TabModifyController implements EventHandler<ActionEvent>
{
  private final ObservableList<FinancialProperty> foDataList = FXCollections.observableArrayList();

  //------------------------
  // tblFinancial
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

  //------------------------

  // tblSummary
  @FXML
  private TableViewPlus tblSummary;
  @FXML
  private TableColumn colSummaryDescription;

  @FXML
  private TableColumn colSummaryAmount;

  //------------------------

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
  private Label lblTotal;
  @FXML
  private DatePicker txtDate;
  @FXML
  private UpperCaseTextField txtSymbol;
  @FXML
  private Hyperlink lnkSymbolURL;

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
    this.setupTables();

    this.setupListeners();
    this.setupTooltips();
    this.setupTextComponents();

    this.resetComponentsOnModify(false);

    this.resetComponentsOnModify(false);

    TimerSummaryTable.INSTANCE.setTable(this.tblSummary);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void setupListeners()
  {
    this.btnModify.setOnAction(toActionEvent -> TabFinancialController.this.modifyRow(this.foCurrentFinancialProperty));
    this.btnSave.setOnAction(toActionEvent -> TabFinancialController.this.saveRow());
    this.btnCancel.setOnAction(toActionEvent -> TabFinancialController.this.cancelRow());

    this.btnCreate.setOnAction(toActionEvent -> TabFinancialController.this.createRow());
    this.btnClone.setOnAction(toActionEvent -> TabFinancialController.this.cloneRow(this.foCurrentFinancialProperty));
    this.btnRemove.setOnAction(toActionEvent -> TabFinancialController.this.removeRow());

    this.txtShares.textProperty().addListener((observable, oldValue, newValue) -> this.updateTotalLabel());
    this.txtPrice.textProperty().addListener((observable, oldValue, newValue) -> this.updateTotalLabel());
    this.txtSymbol.textProperty().addListener((observable, oldValue, newValue) -> this.updateSymbolHyperlink());

    this.lnkSymbolURL.setOnAction(this);

    // Setup the summary table update on scroll.
    this.tblFinancial.getSelectionModel().selectedItemProperty().addListener((ChangeListener<FinancialProperty>) (observable, toOldRow, toNewRow) ->
    {

      String lcAccount = null;
      String lcCategory = null;
      String lcType = null;
      if (toNewRow != null)
      {
        this.foCurrentFinancialProperty = toNewRow;
        TabFinancialController.this.updateComponentsContent(false);

        lcAccount = toNewRow.getAccount();
        lcCategory = toNewRow.getCategory();
        lcType = toNewRow.getType();
      }

      TimerSummaryTable.INSTANCE.scheduleDataRefresh(lcAccount, lcType, lcCategory);
    });

  }

  // ---------------------------------------------------------------------------------------------------------------------
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

    if (this.tblFinancial.getItems() != this.foDataList)
    {
      this.tblFinancial.setItems(this.foDataList);
    }
    this.tblFinancial.resizeColumnsToFit();

    loSession.close();

    TimerSummaryTable.INSTANCE.scheduleDataRefresh(null, null, null);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupTextComponents()
  {
    this.txtComments.setWrapText(true);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void setupTables()
  {
    //------------------
    // tblFinancial
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

    //------------------
    // tblSummary
    this.colSummaryDescription.setCellValueFactory(new PropertyValueFactory<SummaryProperty, String>("summarydescription"));
    this.colSummaryAmount.setCellValueFactory(new PropertyValueFactory<SummaryProperty, String>("summaryamount"));

    this.colSummaryDescription.setSortable(false);
    this.colSummaryAmount.setSortable(false);

    this.tblSummary.getItems().clear();
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
  protected void saveRow()
  {
    final boolean llCreatingRow = this.flCreatingRow;
    this.flCreatingRow = false;

    final FinancialProperty loProp = llCreatingRow ? new FinancialProperty() : this.foCurrentFinancialProperty;

    final double lnShares = this.getDoubleFromTextFieldl(this.txtShares);
    final double lnPrice = this.getDoubleFromTextFieldl(this.txtPrice);

    loProp.setDescription(this.txtDescription.getText().trim());
    loProp.setAccount(this.txtAccount.getText().trim());
    loProp.setType(this.txtType.getText().trim());
    loProp.setCategory(this.txtCategory.getText().trim());
    loProp.setShares(lnShares);
    loProp.setPrice(lnPrice);
    loProp.setValuationDate(Date.valueOf(this.txtDate.getValue()));
    loProp.setSymbol(this.txtSymbol.getText().trim());
    loProp.setRetirement(this.chkRetirement.isSelected());
    loProp.setComments(this.txtComments.getText().trim());

    boolean llSaved = false;
    if (!llCreatingRow)
    {
      llSaved = HibernateUtil.INSTANCE.updateRow(loProp.toEntity());
    }
    else
    {
      final FinancialEntity loNewEntity = loProp.toEntity();
      final HibernateUtil loHibernate = HibernateUtil.INSTANCE;
      loNewEntity.setGroupID(loHibernate.getGroupID());

      llSaved = loHibernate.insertRow(loNewEntity);
      if (llSaved)
      {
        final FinancialProperty loNewRecord = new FinancialProperty(loNewEntity.getGroupID(), loNewEntity.getFinancialID(), loNewEntity.getDescription(), loNewEntity.getAccount(),
            loNewEntity.getType(), loNewEntity.getCategory(), loNewEntity.getShares(), loNewEntity.getPrice(), loNewEntity.getValuationDate(), loNewEntity.getRetirement(),
            loNewEntity.getSymbol(), loNewEntity.getComments());

        this.foDataList.add(loNewRecord);
        this.tblFinancial.getSelectionModel().select(loNewRecord);
        this.tblFinancial.scrollTo(loNewRecord);

        this.foCurrentFinancialProperty = loNewRecord;
      }
    }

    if (llSaved)
    {
      Misc.setStatusText(llCreatingRow ? "Record has been added" : "Information has been saved");
    }
    else
    {
      Misc.errorMessage("The information was unable to be saved.");
    }

    this.resetComponentsOnModify(false);
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
  // Realize that the Total label and the Symbol URL is updated through change listeners.
  protected void updateComponentsContent(final boolean tlUseEmptyFields)
  {
    final FinancialProperty loProp = this.foCurrentFinancialProperty;

    this.txtDescription.setText(tlUseEmptyFields ? "" : loProp.getDescription());
    this.txtAccount.setText(tlUseEmptyFields ? "" : loProp.getAccount());
    this.txtType.setText(tlUseEmptyFields ? "" : loProp.getType());
    this.txtCategory.setText(tlUseEmptyFields ? "" : loProp.getCategory());
    this.txtShares.setText(tlUseEmptyFields ? "0.0" : Double.toString(loProp.getShares()));
    this.txtPrice.setText(tlUseEmptyFields ? "0.0" : Double.toString(loProp.getPrice()));
    this.txtDate.setValue(tlUseEmptyFields ? LocalDate.now() : loProp.getValuationDate().toLocalDate());
    this.txtSymbol.setText(tlUseEmptyFields ? "" : loProp.getSymbol().trim());
    this.chkRetirement.setSelected(tlUseEmptyFields ? false : loProp.getRetirement());
    this.txtComments.setText(tlUseEmptyFields ? "" : loProp.getComments());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void resetComponentsOnModify(final boolean tlModify)
  {
    super.resetComponentsOnModify(tlModify);

    this.tblFinancial.setDisable(tlModify);

    if (!tlModify)
    {
      return;
    }

    // Signifies editing is enabled so move cursor to the first enabled component.
    if (this.txtDescription.isEditable())
    {
      this.txtDescription.requestFocus();
    }
    else
    {
      this.txtAccount.requestFocus();
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void resetTextFields(final boolean tlModify)
  {
    final boolean llEmpty = this.txtSymbol.getText().isEmpty();

    this.setEditable(this.txtDescription, tlModify && llEmpty);
    this.setEditable(this.txtAccount, tlModify);
    this.setEditable(this.txtType, tlModify);
    this.setEditable(this.txtCategory, tlModify);
    this.setEditable(this.txtShares, tlModify);
    this.setEditable(this.txtPrice, tlModify && llEmpty);
    this.setEditable(this.txtDate, tlModify && llEmpty);
    this.setEditable(this.txtSymbol, tlModify);
    this.setEditable(this.chkRetirement, tlModify);
    this.setEditable(this.txtComments, tlModify);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void updateSymbolHyperlink()
  {
    final String lcSymbol = this.txtSymbol.getText().trim();

    this.lnkSymbolURL.setText(lcSymbol.isEmpty() ? "" : ThreadDownloadSymbolInfo.getSymbolDailyURL(lcSymbol));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void updateTotalLabel()
  {
    final double lnShares = this.getDoubleFromTextFieldl(this.txtShares);
    final double lnPrice = this.getDoubleFromTextFieldl(this.txtPrice);

    final double lnTotal = lnShares * lnPrice;

    this.lblTotal.setText(Misc.getCurrencyFormat().format(lnTotal));
    this.lblTotal.setTextFill((lnTotal >= 0) ? Color.BLACK : Color.RED);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private double getDoubleFromTextFieldl(final TextField toField)
  {
    double lnValue = 0.0;
    try
    {
      lnValue = Double.parseDouble(toField.getText().trim());
    }
    catch (final NumberFormatException ignored)
    {
    }

    return (lnValue);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  public void handle(final ActionEvent toEvent)
  {
    final Object loSource = toEvent.getSource();
    if (loSource instanceof Hyperlink)
    {
      final Hyperlink loHyperLink = (Hyperlink) loSource;
      final String lcURL = loHyperLink.getText().trim();

      if (!lcURL.isEmpty())
      {
        Main.getMainHostServices().showDocument(lcURL);
      }

    }
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

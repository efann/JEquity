/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.controller.tab;

import com.beowurks.jequity.dao.combobox.StringKeyItem;
import com.beowurks.jequity.dao.combobox.TaxStatusList;
import com.beowurks.jequity.dao.hibernate.FinancialEntity;
import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.hibernate.threads.SingleSymbolInfo;
import com.beowurks.jequity.dao.hibernate.threads.ThreadDownloadSingleSymbol;
import com.beowurks.jequity.dao.hibernate.threads.TimerSummaryTable;
import com.beowurks.jequity.dao.tableview.FinancialProperty;
import com.beowurks.jequity.dao.tableview.SummaryProperty;
import com.beowurks.jequity.dao.web.PageScraping;
import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import com.beowurks.jequity.view.cell.CurrencyTableCell;
import com.beowurks.jequity.view.cell.DateTableCell;
import com.beowurks.jequity.view.cell.DoubleTableCell;
import com.beowurks.jequity.view.cell.TaxStatusTableCell;
import com.beowurks.jequity.view.combobox.ComboBoxPlus;
import com.beowurks.jequity.view.table.TableViewPlus;
import com.beowurks.jequity.view.textfield.DatePickerPlus;
import com.beowurks.jequity.view.textfield.NumberTextField;
import com.beowurks.jequity.view.textfield.UpperCaseTextField;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
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
  private TableColumn colOwnership;
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
  private TableColumn colTaxStatus;
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
  private TextField txtOwnership;
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
  private DatePickerPlus txtDate;
  @FXML
  private UpperCaseTextField txtSymbol;
  @FXML
  private Hyperlink lnkSymbolURL;
  @FXML
  private CheckBox chkRetirement;
  @FXML
  private ComboBoxPlus<StringKeyItem> cboTaxStatus;
  @FXML
  private TextArea txtComments;

  //------------------------

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
    this.setupComboBoxes();

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
    this.txtSymbol.textProperty().addListener((observable, oldValue, newValue) ->
    {
      this.updateTextFieldsWithSymbol();
      this.updateSymbolHyperlink();
    });

    this.txtSymbol.focusedProperty().addListener((observable, oldValue, newValue) ->
    {
      // Signifies that focus has been lost.
      if (oldValue && (!newValue))
      {
        ThreadDownloadSingleSymbol.INSTANCE.start(SingleSymbolInfo.INSTANCE);
      }
    });

    this.lnkSymbolURL.setOnAction(this);

    // Setup the summary table update on scroll.
    this.tblFinancial.getSelectionModel().selectedItemProperty().addListener((ChangeListener<FinancialProperty>) (observable, toOldRow, toNewRow) ->
    {
      if (toNewRow != null)
      {
        this.foCurrentFinancialProperty = toNewRow;
        this.refreshCalculationsAndSummary();
      }

    });

    this.setupQuickModify(this.tblFinancial);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void refreshCalculationsAndSummary()
  {
    if (this.foCurrentFinancialProperty == null)
    {
      return;
    }

    final FinancialProperty loCurrentRow = this.foCurrentFinancialProperty;

    TabFinancialController.this.updateComponentsContent(false);

    final String lcAccount = loCurrentRow.getAccount();
    final String lcCategory = loCurrentRow.getCategory();
    final String lcType = loCurrentRow.getType();

    TimerSummaryTable.INSTANCE.scheduleDataRefresh(lcAccount, lcType, lcCategory);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void refreshData()
  {
    final FinancialProperty loCurrent = (FinancialProperty) this.tblFinancial.getSelectionModel().getSelectedItem();

    final Session loSession = HibernateUtil.INSTANCE.getSession();

    final List<FinancialEntity> loList = this.getQuery(loSession).list();

    this.foDataList.clear();
    for (final FinancialEntity loRow : loList)
    {
      this.foDataList.add(new FinancialProperty(loRow.getGroupID(), loRow.getFinancialID(), loRow.getDescription(), loRow.getAccount(),
          loRow.getType(), loRow.getCategory(), loRow.getShares(), loRow.getPrice(), loRow.getValuationDate(), loRow.getRetirement(), loRow.getOwnership(),
          loRow.getTaxStatus(), loRow.getSymbol(), loRow.getComments()));
    }

    if (this.tblFinancial.getItems() != this.foDataList)
    {
      this.tblFinancial.setItems(this.foDataList);
    }

    if (loCurrent != null)
    {
      final int lnRows = this.tblFinancial.getItems().size();
      for (int i = 0; i < lnRows; ++i)
      {
        final int lnID = ((FinancialProperty) this.tblFinancial.getItems().get(i)).getFinancialID();
        if (loCurrent.getFinancialID() == lnID)
        {
          this.tblFinancial.getSelectionModel().select(i);
          this.tblFinancial.scrollTo(i);
          break;
        }
      }
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
  private void setupComboBoxes()
  {
    this.cboTaxStatus.getItems().addAll(TaxStatusList.INSTANCE.getList());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void setupTables()
  {
    //------------------
    // tblFinancial
    this.colID.setCellValueFactory(new PropertyValueFactory<FinancialProperty, Integer>("financialid"));
    this.colDescription.setCellValueFactory(new PropertyValueFactory<FinancialProperty, String>("description"));
    this.colOwnership.setCellValueFactory(new PropertyValueFactory<FinancialProperty, String>("ownership"));
    this.colAccount.setCellValueFactory(new PropertyValueFactory<FinancialProperty, String>("account"));
    this.colType.setCellValueFactory(new PropertyValueFactory<FinancialProperty, String>("type"));
    this.colCategory.setCellValueFactory(new PropertyValueFactory<FinancialProperty, String>("category"));
    this.colShares.setCellValueFactory(new PropertyValueFactory<FinancialProperty, Double>("shares"));
    this.colPrice.setCellValueFactory(new PropertyValueFactory<FinancialProperty, Double>("price"));
    this.colValuationDate.setCellValueFactory(new PropertyValueFactory<FinancialProperty, String>("valuationdate"));
    this.colRetirement.setCellValueFactory(new PropertyValueFactory<FinancialProperty, Boolean>("retirement"));
    this.colTaxStatus.setCellValueFactory(new PropertyValueFactory<FinancialProperty, String>("taxstatus"));
    this.colSymbol.setCellValueFactory(new PropertyValueFactory<FinancialProperty, String>("symbol"));
    this.colSharesPrice.setCellValueFactory(new PropertyValueFactory<FinancialProperty, Double>("total"));

    // Add check box to the grid.
    this.colRetirement.setCellFactory(tc -> new CheckBoxTableCell<FinancialProperty, Boolean>());

    this.colValuationDate.setCellFactory(tc -> new DateTableCell());

    this.colPrice.setCellFactory(tc -> new CurrencyTableCell());
    this.colSharesPrice.setCellFactory(tc -> new CurrencyTableCell());

    this.colShares.setCellFactory(tc -> new DoubleTableCell());
    this.colTaxStatus.setCellFactory(tc -> new TaxStatusTableCell());

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
  protected boolean modifyRow()
  {
    if (this.isEditing() || (this.foCurrentFinancialProperty == null))
    {
      return (false);
    }

    return (this.modifyRow(this.foCurrentFinancialProperty));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void saveRow()
  {
    final boolean llCreatingRow = this.flCreatingRow;
    this.flCreatingRow = false;

    final FinancialProperty loProp = llCreatingRow ? new FinancialProperty() : this.foCurrentFinancialProperty;

    final double lnShares = Misc.getDoubleFromTextField(this.txtShares);
    final double lnPrice = Misc.getDoubleFromTextField(this.txtPrice);

    loProp.setSymbol(this.txtSymbol.getText().trim());
    loProp.setDescription(this.txtDescription.getText().trim());
    loProp.setOwnership(this.txtOwnership.getText().trim());
    loProp.setAccount(this.txtAccount.getText().trim());
    loProp.setType(this.txtType.getText().trim());
    loProp.setCategory(this.txtCategory.getText().trim());
    loProp.setShares(lnShares);
    loProp.setPrice(lnPrice);
    loProp.setValuationDate(Date.valueOf(this.txtDate.getValue()));
    loProp.setRetirement(this.chkRetirement.isSelected());
    loProp.setTaxStatus(((StringKeyItem) this.cboTaxStatus.getValue()).getKey());
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
            loNewEntity.getType(), loNewEntity.getCategory(), loNewEntity.getShares(), loNewEntity.getPrice(), loNewEntity.getValuationDate(), loNewEntity.getRetirement(), loNewEntity.getOwnership(),
            loNewEntity.getTaxStatus(), loNewEntity.getSymbol(), loNewEntity.getComments());

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

    this.refreshCalculationsAndSummary();
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
    final boolean llUseEmptyFields = (tlUseEmptyFields || (loProp == null));

    this.txtDescription.setText(llUseEmptyFields ? "" : loProp.getDescription());
    this.txtOwnership.setText(llUseEmptyFields ? "" : loProp.getOwnership());
    this.txtAccount.setText(llUseEmptyFields ? "" : loProp.getAccount());
    this.txtType.setText(llUseEmptyFields ? "" : loProp.getType());
    this.txtCategory.setText(llUseEmptyFields ? "" : loProp.getCategory());
    this.txtShares.setText(llUseEmptyFields ? "0.0" : Double.toString(loProp.getShares()));
    this.txtPrice.setText(llUseEmptyFields ? "0.0" : Double.toString(loProp.getPrice()));
    this.txtDate.setValue(llUseEmptyFields ? LocalDate.now() : loProp.getValuationDate().toLocalDate());
    this.txtSymbol.setText(llUseEmptyFields ? "" : loProp.getSymbol().trim());
    this.chkRetirement.setSelected(!llUseEmptyFields && loProp.getRetirement());
    this.cboTaxStatus.setValue(TaxStatusList.INSTANCE.getItem(loProp.getTaxStatus()));

    this.txtComments.setText(llUseEmptyFields ? "" : loProp.getComments());
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

    SingleSymbolInfo.INSTANCE.setInformation(this.txtSymbol.getText(), this.txtSymbol,
        this.txtDescription, this.txtPrice, this.txtDate, this.btnSave);

    if (this.findFocused() == null)
    {
      // Signifies editing is enabled so move cursor to the first enabled component.
      this.txtSymbol.requestFocus();
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void resetTextFields(final boolean tlModify)
  {
    super.resetTextFields(tlModify);

    final boolean llEmpty = this.txtSymbol.getText().isEmpty();

    this.setEditable(this.txtDescription, tlModify && llEmpty);
    this.setEditable(this.txtPrice, tlModify && llEmpty);
    this.setEditable(this.txtDate, tlModify && llEmpty);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void updateTextFieldsWithSymbol()
  {
    if (!this.isEditing())
    {
      return;
    }

    this.resetTextFields(true);

    final String lcSymbol = this.txtSymbol.getText().trim();
    final boolean llSymbolEmpty = lcSymbol.isEmpty();
    if (llSymbolEmpty && this.txtDescription.getText().equals(Constants.BLANK_DESCRIPTION_FOR_SYMBOL))
    {
      this.txtDescription.setText("");
    }
    else if (!llSymbolEmpty && this.txtDescription.getText().trim().isEmpty())
    {
      this.txtDescription.setText(Constants.BLANK_DESCRIPTION_FOR_SYMBOL);
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void updateSymbolHyperlink()
  {
    final String lcSymbol = this.txtSymbol.getText().trim();

    this.lnkSymbolURL.setText(lcSymbol.isEmpty() ? "" : PageScraping.INSTANCE.getDailyStockURL(lcSymbol));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void updateTotalLabel()
  {
    final double lnShares = Misc.getDoubleFromTextField(this.txtShares);
    final double lnPrice = Misc.getDoubleFromTextField(this.txtPrice);

    final double lnTotal = lnShares * lnPrice;

    this.lblTotal.setText(Misc.getCurrencyFormat().format(lnTotal));
    this.lblTotal.setTextFill((lnTotal >= 0) ? Color.BLACK : Color.RED);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public TableViewPlus getTable()
  {
    return (this.tblFinancial);
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

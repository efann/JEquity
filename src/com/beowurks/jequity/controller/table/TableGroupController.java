/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.controller.table;

import com.beowurks.jequity.dao.hibernate.GroupEntity;
import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.tableview.GroupProperty;
import com.beowurks.jequity.utility.Misc;
import com.beowurks.jequity.view.table.TableViewPlus;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class TableGroupController extends TableBaseController
{
  private final ObservableList<GroupProperty> foDataList = FXCollections.observableArrayList();

  @FXML
  private TableViewPlus tblGroup;

  @FXML
  private TableColumn colID;

  @FXML
  private TableColumn colDescription;

  @FXML
  private TextField txtDescription;

  @FXML
  private Button btnModify;

  @FXML
  private Button btnSave;

  @FXML
  private Button btnCancel;

  @FXML
  private Button btnCreate;

  @FXML
  private Button btnRemove;

  private GroupProperty foCurrentGroupProperty = null;


  // ---------------------------------------------------------------------------------------------------------------------
  // From https://stackoverflow.com/questions/34785417/javafx-fxml-controller-constructor-vs-initialize-method
  @FXML
  public void initialize()
  {
    this.setupTable();
    this.setupListeners();
    this.setupTooltips();

    this.resetComponentsOnModify(false);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupTooltips()
  {
    this.btnCreate.setTooltip(new Tooltip("Create a new group"));
    this.btnRemove.setTooltip(new Tooltip("Remove the currently selected group"));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupListeners()
  {
    this.btnModify.setOnAction(toActionEvent -> TableGroupController.this.modifyRow());
    this.btnSave.setOnAction(toActionEvent -> TableGroupController.this.saveRow());
    this.btnCancel.setOnAction(toActionEvent -> TableGroupController.this.cancelRow());

    this.btnCreate.setOnAction(toActionEvent -> TableGroupController.this.insertRow());
    this.btnRemove.setOnAction(toActionEvent -> TableGroupController.this.removeRow());

    this.tblGroup.getSelectionModel().selectedItemProperty().addListener((ChangeListener<GroupProperty>) (observable, toOldRow, toNewRow) -> {
      if (toNewRow != null)
      {
        this.foCurrentGroupProperty = toNewRow;
        TableGroupController.this.txtDescription.setText(toNewRow.getDescription());
      }
    });

  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void refreshData()
  {
    this.foDataList.clear();

    final Session loSession = HibernateUtil.INSTANCE.getSession();

    final List<GroupEntity> loList = this.getQuery(loSession).list();

    for (final GroupEntity loRow : loList)
    {
      this.foDataList.add(new GroupProperty(loRow.getGroupID(), loRow.getDescription()));
    }

    this.tblGroup.setItems(this.foDataList);
    this.tblGroup.resizeColumnsToFit();

    loSession.close();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void setupTable()
  {
    this.colID.setCellValueFactory(new PropertyValueFactory<GroupProperty, Integer>("id"));
    this.colDescription.setCellValueFactory(new PropertyValueFactory<GroupProperty, String>("description"));

    this.tblGroup.getItems().clear();
    this.tblGroup.setColumnResizePolicy((param -> true));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected NativeQuery getQuery(final Session toSession)
  {
    final String lcSQL = String.format("SELECT * FROM %s", HibernateUtil.INSTANCE.getTableGroup());

    final NativeQuery loQuery = toSession.createNativeQuery(lcSQL)
        .addEntity(GroupEntity.class);

    return (loQuery);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void modifyRow()
  {
    if (this.tblGroup.getSelectionModel().getSelectedItem() == null)
    {
      Misc.errorMessage("You need to select a record before modifying it.");
      return;
    }

    this.resetComponentsOnModify(true);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void saveRow()
  {
    final GroupProperty loProp = this.foCurrentGroupProperty;
    loProp.setDescription(this.txtDescription.getText().trim());

    this.resetComponentsOnModify(false);
    if (HibernateUtil.INSTANCE.updateRow(loProp.toEntity()))
    {
      Misc.setStatusText("The data has been saved.");
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void cancelRow()
  {
    this.resetComponentsOnModify(false);

    Misc.setStatusText("Your modifications have been cancelled.");
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void insertRow()
  {
    final GroupEntity loNewEntity = new GroupEntity();
    final HibernateUtil loHibernate = HibernateUtil.INSTANCE;

    final Date loDate = new Date(Calendar.getInstance().getTimeInMillis());
    final Timestamp loTimestamp = new Timestamp(loDate.getTime());

    // The description field for GroupEntity cannot be duplicated.
    loNewEntity.setDescription(String.format("<new record on %s>", loTimestamp.toString()));

    if (loHibernate.insertRow(loNewEntity))
    {
      final GroupProperty loRecord = new GroupProperty(loNewEntity.getGroupID(), loNewEntity.getDescription());

      this.foDataList.add(loRecord);
      this.tblGroup.getSelectionModel().select(loRecord);

      this.foCurrentGroupProperty = loRecord;
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void removeRow()
  {
    if (this.foCurrentGroupProperty == null)
    {
      Misc.errorMessage("You need to select a record before modifying it.");
      return;
    }

    final GroupProperty loProp = this.foCurrentGroupProperty;
    final String lcMessage = String.format("Do you want to remove the following record?\n\n%s (# %d)\n\n", loProp.getDescription(), loProp.getID());
    if (Misc.yesNo(lcMessage))
    {
      if (HibernateUtil.INSTANCE.removeRow(loProp.toEntity()))
      {
        // You must reset this.foCurrentGroupProperty before calling remove.
        // The grid removes then selects another row and then resets this.foCurrentGroupProperty
        this.foCurrentGroupProperty = null;

        this.tblGroup.getItems().remove(loProp);
      }
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void resetComponentsOnModify(final boolean tlModify)
  {
    this.resetButtons(tlModify);
    this.resetTextFields(tlModify);

    this.tblGroup.setDisable(tlModify);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void resetButtons(final boolean tlModify)
  {
    this.btnModify.setDisable(tlModify);
    this.btnCreate.setDisable(tlModify);
    this.btnRemove.setDisable(tlModify);

    this.btnSave.setVisible(tlModify);
    this.btnCancel.setVisible(tlModify);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void resetTextFields(final boolean tlModify)
  {
    this.setEditable(this.txtDescription, tlModify);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // Unfortunately, I can't create an inherited class from TextField and override setEditable: it's a final method.
  // Oh well. . . .
  private void setEditable(final TextField toField, final boolean tlEditable)
  {
    toField.setEditable(tlEditable);

    toField.setStyle(tlEditable ? "" : "-fx-background-color: lightgrey;");
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void updateJTableRow(final GroupEntity toEntity)
  {
    /*
    final GroupTable loTable = this.grdGroupTable;
    final SortingTableModel loSortingModel = loTable.getSortModel();
    final int lnIDCol = loTable.getModelColumnID();
    final int lnID = toEntity.getGroupID();

    final int lnCount = loSortingModel.getRowCount();
    int lnRow = Constants.ROW_UNKNOWN;
    for (int i = 0; i < lnCount; ++i)
    {
      // SortingTableModel acts differently than the regular TableModel,
      // so lnRow will match the Sorting Table.
      if (Integer.parseInt(loSortingModel.getValueAt(i, lnIDCol).toString()) == lnID)
      {
        lnRow = i;
        break;
      }
    }

    if (lnRow != Constants.ROW_UNKNOWN)
    {
      loTable.setColumnValue(toEntity.getDescription(), lnRow, Constants.GROUP_DESCRIPTION);

      loSortingModel.fireTableRowsUpdated(lnRow, lnRow);
    }
    */
  }

  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

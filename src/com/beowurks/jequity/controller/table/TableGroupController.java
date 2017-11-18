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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
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
public class TableGroupController extends TableModifyController
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
  protected void setupListeners()
  {
    this.btnModify.setOnAction(toActionEvent -> TableGroupController.this.modifyRow(this.foCurrentGroupProperty));
    this.btnSave.setOnAction(toActionEvent -> TableGroupController.this.saveRow());
    this.btnCancel.setOnAction(toActionEvent -> TableGroupController.this.cancelRow());

    this.btnCreate.setOnAction(toActionEvent -> TableGroupController.this.insertRow());
    this.btnRemove.setOnAction(toActionEvent -> TableGroupController.this.removeRow());

    this.tblGroup.getSelectionModel().selectedItemProperty().addListener((ChangeListener<GroupProperty>) (observable, toOldRow, toNewRow) -> {
      if (toNewRow != null)
      {
        this.foCurrentGroupProperty = toNewRow;
        TableGroupController.this.updateComponentsContent();
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
  protected void insertRow()
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
      this.tblGroup.scrollTo(loRecord);

      this.foCurrentGroupProperty = loRecord;
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void removeRow()
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
  protected void saveRow()
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
  protected void updateComponentsContent()
  {
    final GroupProperty loProp = this.foCurrentGroupProperty;
    this.txtDescription.setText(loProp.getDescription());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void resetComponentsOnModify(final boolean tlModify)
  {
    super.resetComponentsOnModify(tlModify);

    this.tblGroup.setDisable(tlModify);

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
  }

  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

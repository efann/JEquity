/*
 * JEquity
 * Copyright(c) 2008-2023, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.controller.tab;

import com.beowurks.jequity.dao.hibernate.GroupEntity;
import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.tableview.GroupProperty;
import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.Misc;
import com.beowurks.jequity.view.table.TableViewPlus;
import com.beowurks.jequity.view.textfield.TextFieldPlus;
import javafx.beans.value.ChangeListener;
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
public class TabGroupController extends TabModifyController
{
  private final ObservableList<GroupProperty> foDataList = FXCollections.observableArrayList();

  @FXML
  private TableViewPlus tblGroup;

  @FXML
  private TableColumn colID;

  @FXML
  private TableColumn colDescription;

  @FXML
  private TextFieldPlus txtDescription;

  private GroupProperty foCurrentGroupProperty = null;

  // ---------------------------------------------------------------------------------------------------------------------
  // From https://stackoverflow.com/questions/34785417/javafx-fxml-controller-constructor-vs-initialize-method
  @FXML
  public void initialize()
  {
    this.setupTables();
    this.setupListeners();
    this.setupTooltips();

    // Main.initializeEnvironment now calls resetComponentsOnModify(false) as Main.getController() will not be null.
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void setupListeners()
  {
    this.btnModify.setOnAction(toActionEvent -> TabGroupController.this.modifyRow(this.foCurrentGroupProperty));
    this.btnSave.setOnAction(toActionEvent -> TabGroupController.this.saveRow());
    this.btnCancel.setOnAction(toActionEvent -> TabGroupController.this.cancelRow());

    this.btnCreate.setOnAction(toActionEvent -> TabGroupController.this.createRow(false));
    this.btnClone.setOnAction(toActionEvent -> TabGroupController.this.cloneRow(this.foCurrentGroupProperty, false));
    this.btnRemove.setOnAction(toActionEvent -> TabGroupController.this.removeRow());

    this.tblGroup.getSelectionModel().selectedItemProperty().addListener((ChangeListener<GroupProperty>) (observable, toOldRow, toNewRow) ->
    {
      if (toNewRow != null)
      {
        this.foCurrentGroupProperty = toNewRow;
        TabGroupController.this.updateComponentsContent(false);
      }
    });

    this.setupQuickModify(this.tblGroup);

  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void initializeComponentsNeedingAppProperties()
  {
    this.resetComponentsOnModify(false);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void refreshData()
  {
    final GroupProperty loCurrent = (GroupProperty) this.tblGroup.getSelectionModel().getSelectedItem();

    final Session loSession = HibernateUtil.INSTANCE.getSession();

    final List<GroupEntity> loList = this.getQuery(loSession).list();

    this.foDataList.clear();
    for (final GroupEntity loRow : loList)
    {
      this.foDataList.add(new GroupProperty(loRow.getGroupID(), loRow.getDescription()));
    }

    if (this.tblGroup.getItems() != this.foDataList)
    {
      this.tblGroup.setItems(this.foDataList);
    }

    if (loCurrent != null)
    {
      final int lnRows = this.tblGroup.getItems().size();
      for (int i = 0; i < lnRows; ++i)
      {
        final int lnID = ((GroupProperty) this.tblGroup.getItems().get(i)).getID();
        if (loCurrent.getID() == lnID)
        {
          this.tblGroup.getSelectionModel().select(i);
          this.tblGroup.scrollTo(i);
          break;
        }
      }
    }

    this.tblGroup.resizeColumnsToFit();
    // In case of data refresh and column(s) have already been sorted.
    // And it's okay if no column(s) have been sorted.
    this.tblGroup.sort();

    loSession.close();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public TableViewPlus getTable()
  {
    return (this.tblGroup);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void setupTables()
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

        Main.getController().refreshAllComponents(true);
      }
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected boolean modifyRow()
  {
    if (this.isEditing() || (this.foCurrentGroupProperty == null))
    {
      // Rare case: program starts and user clicks editable component first with no grid row selected.
      // If the grid.requestFocus is not called, then when a grid row is selected,
      // the modifyRow routine is immediately called.
      // Turns out, the editable component still has focus for some reason, thus calling modifyRow.
      // Weird.
      this.tblGroup.requestFocus();

      return (false);
    }

    return (this.modifyRow(this.foCurrentGroupProperty));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void saveRow()
  {
    final boolean llCreatingRow = this.flCreatingRow;
    this.flCreatingRow = false;

    final GroupProperty loProp = llCreatingRow ? new GroupProperty() : this.foCurrentGroupProperty;

    loProp.setDescription(this.txtDescription.getText().trim());

    boolean llSaved = false;
    if (!llCreatingRow)
    {
      llSaved = HibernateUtil.INSTANCE.updateRow(loProp.toEntity());
    }
    else
    {
      final GroupEntity loNewEntity = loProp.toEntity();
      final HibernateUtil loHibernate = HibernateUtil.INSTANCE;
      loNewEntity.setGroupID(loHibernate.getGroupID());

      llSaved = loHibernate.insertRow(loNewEntity);
      if (llSaved)
      {
        final GroupProperty loNewRecord = new GroupProperty(loNewEntity.getGroupID(), loNewEntity.getDescription());

        this.foDataList.add(loNewRecord);
        this.tblGroup.getSelectionModel().select(loNewRecord);
        this.tblGroup.scrollTo(loNewRecord);

        this.foCurrentGroupProperty = loNewRecord;

        Main.getController().refreshAllComponents(true);
      }
    }

    if (llSaved)
    {
      Main.getController().refreshAllComponents(true);
      Misc.setStatusText(llCreatingRow ? "Record has been added" : "Information has been saved");
    }
    else
    {
      Misc.errorMessage("The information was unable to be saved. By the way, the description cannot be a duplicate.");
    }

    this.resetComponentsOnModify(false);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void updateComponentsContent(final boolean tlUseEmptyFields)
  {
    final GroupProperty loProp = this.foCurrentGroupProperty;
    final boolean llUseEmptyFields = (tlUseEmptyFields || (loProp == null));

    this.txtDescription.setText(llUseEmptyFields ? "" : loProp.getDescription());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void resetComponentsOnModify(final boolean tlModify)
  {
    super.resetComponentsOnModify(tlModify);

    this.tblGroup.setDisable(tlModify);

    if (!tlModify)
    {
      return;
    }

    // Signifies editing is enabled so move cursor to the first component.
    this.txtDescription.requestFocus();
  }

  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

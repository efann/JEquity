/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.controller;

import com.beowurks.jequity.dao.hibernate.GroupEntity;
import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.tableview.GroupProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;

import java.util.List;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class TableGroupController extends TableBaseController
{
  private final ObservableList<GroupProperty> foDataList = FXCollections.observableArrayList();

  @FXML
  private TableView tblGroup;

  @FXML
  private TableColumn colID;

  @FXML
  private TableColumn colDescription;


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

    final List<GroupEntity> loList = this.getQuery(loSession).list();

    for (final GroupEntity loRow : loList)
    {
      this.foDataList.add(new GroupProperty(loRow.getGroupID(), loRow.getDescription()));
    }

    this.tblGroup.setItems(this.foDataList);

    loSession.close();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void setupTable()
  {
    this.tblGroup.setEditable(false);

    this.colID.setCellValueFactory(new PropertyValueFactory<GroupProperty, Integer>("id"));
    this.colDescription.setCellValueFactory(new PropertyValueFactory<GroupProperty, String>("description"));

    this.tblGroup.getItems().clear();
    this.tblGroup.setColumnResizePolicy((param -> true));
  }

  // -----------------------------------------------------------------------------
  protected NativeQuery getQuery(final Session toSession)
  {
    final String lcSQL = String.format("SELECT * FROM %s", HibernateUtil.INSTANCE.getTableGroup());

    final NativeQuery loQuery = toSession.createNativeQuery(lcSQL)
        .addEntity(GroupEntity.class);

    return (loQuery);
  }

  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

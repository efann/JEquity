/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.controller;

import com.beowurks.jequity.dao.tableview.EnvironmentProperty;
import com.beowurks.jequity.utility.Misc;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Enumeration;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class GroupTableController
{
  private final ObservableList<EnvironmentProperty> foDataList = FXCollections.observableArrayList();

  @FXML
  private TableView tblGroup;

  @FXML
  private TableColumn colKey;

  @FXML
  private TableColumn colValue;


  // ---------------------------------------------------------------------------------------------------------------------
  // From https://stackoverflow.com/questions/34785417/javafx-fxml-controller-constructor-vs-initialize-method
  @FXML
  public void initialize()
  {
    this.setupTable();
  }
  // ---------------------------------------------------------------------------------------------------------------------
  private void setupTable()
  {
    this.tblGroup.setEditable(false);

    // "key" relates to EnvironmentProperty.keyProperty and "value" relates to EnvironmentProperty.valueProperty
    this.colKey.setCellValueFactory(new PropertyValueFactory<EnvironmentProperty, String>("key"));
    this.colValue.setCellValueFactory(new PropertyValueFactory<EnvironmentProperty, String>("value"));

    final Enumeration<?> loEnum = System.getProperties().propertyNames();
    final StringBuilder lcValue = new StringBuilder();

    while (loEnum.hasMoreElements())
    {
      final String lcKey = loEnum.nextElement().toString();

      Misc.clearStringBuilder(lcValue);
      lcValue.append(System.getProperty(lcKey));
      int lnPos;

      if ((lnPos = lcValue.toString().indexOf(0x0A)) != -1)
      {
        lcValue.replace(lnPos, lnPos + 1, "\\n");
      }

      if ((lnPos = lcValue.toString().indexOf(0x0D)) != -1)
      {
        lcValue.replace(lnPos, lnPos + 1, "\\r");
      }

      this.foDataList.add(new EnvironmentProperty(lcKey, lcValue.toString()));
    }

    this.tblGroup.setItems(this.foDataList);
    this.tblGroup.setColumnResizePolicy((param -> true));
  }

  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

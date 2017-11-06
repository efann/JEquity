/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.controller;

import com.beowurks.jequity.dao.combobox.IntegerKeyItem;
import com.beowurks.jequity.utility.AppProperties;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class OptionsController implements EventHandler<ActionEvent>
{

  @FXML
  private ComboBox<IntegerKeyItem> cboDriver;
  @FXML
  private TextField txtHost;
  @FXML
  private TextField txtDatabase;
  @FXML
  private TextField txtUser;
  @FXML
  private PasswordField txtPassword;
  @FXML
  private Button btnDefault;

  @FXML
  private DatePicker txtHistoricalStart;
  @FXML
  private ComboBox cboDailyDownloadStart;
  @FXML
  private ComboBox cboDailyDownloadInterval;

  @FXML
  private CheckBox chkMigrationStatus;

  // ---------------------------------------------------------------------------------------------------------------------
  // From https://stackoverflow.com/questions/34785417/javafx-fxml-controller-constructor-vs-initialize-method
  @FXML
  public void initialize()
  {
    final AppProperties loApp = AppProperties.INSTANCE;

    this.setupComboBoxes(loApp);
    this.setupTextBoxes(loApp);
    this.setupCheckBoxes(loApp);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupCheckBoxes(final AppProperties toApp)
  {
    this.chkMigrationStatus.setSelected(toApp.getFlywayAlwaysCheck());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupTextBoxes(final AppProperties toApp)
  {
    this.txtHost.setText(toApp.getConnectionHost());
    this.txtDatabase.setText(toApp.getConnectionDatabase());
    this.txtUser.setText(toApp.getConnectionUser());
    this.txtPassword.setText(toApp.getConnectionPassword());

    this.txtHistoricalStart.setValue(toApp.getHistoricalStartDefault().toLocalDate());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupComboBoxes(final AppProperties toApp)
  {
    final IntegerKeyItem[] laDrivers = toApp.getRDBMS_Types();
    final IntegerKeyItem[] laDailyStarts = toApp.getDailyStarts();
    final IntegerKeyItem[] laDailyIntervals = toApp.getDailyIntervals();

    int lnLength = laDrivers.length;
    this.cboDriver.getItems().clear();
    for (int i = 0; i < lnLength; ++i)
    {
      this.cboDriver.getItems().add(laDrivers[i]);
    }

    lnLength = laDailyStarts.length;
    this.cboDailyDownloadStart.getItems().clear();
    for (int i = 0; i < lnLength; ++i)
    {
      this.cboDailyDownloadStart.getItems().add(laDailyStarts[i]);
    }

    lnLength = laDailyIntervals.length;
    this.cboDailyDownloadInterval.getItems().clear();
    for (int i = 0; i < lnLength; ++i)
    {
      this.cboDailyDownloadInterval.getItems().add(laDailyIntervals[i]);
    }

    this.cboDriver.getSelectionModel().select(toApp.convertKeyToIndex(toApp.getRDBMS_Types(), toApp.getConnectionRDBMS_Key()));
    this.cboDailyDownloadStart.getSelectionModel().select(toApp.convertKeyToIndex(laDailyStarts, toApp.getDailyStartKey()));
    this.cboDailyDownloadInterval.getSelectionModel().select(toApp.convertKeyToIndex(laDailyIntervals, toApp.getDailyIntervalKey()));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void handle(final ActionEvent toEvent)
  {
  }

  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

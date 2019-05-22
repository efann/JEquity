/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.controller;


import com.beowurks.jequity.dao.combobox.IntegerKeyItem;
import com.beowurks.jequity.dao.hibernate.threads.TimerSymbolInfo;
import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.AppProperties;
import com.beowurks.jequity.utility.Constants;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

import java.sql.Date;

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
  private ComboBox<IntegerKeyItem> cboDailyDownloadInterval;

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

    this.setupButtons();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void updateAppProperties()
  {
    final AppProperties loApp = AppProperties.INSTANCE;

    //***************************************
    // Connections tab
    final int lnKey = loApp.convertIndexToKey(loApp.getRDBMS_Types(), this.cboDriver.getSelectionModel().getSelectedIndex());

    loApp.setConnectionRDBMS_Key(lnKey);
    loApp.setConnectionHost(this.txtHost.getText().trim());
    loApp.setConnectionDatabase(this.txtDatabase.getText().trim());
    loApp.setConnectionUser(this.txtUser.getText().trim());
    // Don't trim the password as it could contain special characters or spaces at the end-points.
    loApp.setConnectionPassword(this.txtPassword.getText());

    //***************************************
    // Historical tab
    loApp.setDailyIntervalKey(loApp.convertIndexToKey(loApp.getDailyIntervals(), this.cboDailyDownloadInterval.getSelectionModel().getSelectedIndex()));
    loApp.setHistoricalStartDefault(Date.valueOf(this.txtHistoricalStart.getValue()));

    // TimerSymbolInfo uses loApp.getDailyIntervalKey and loApp.getDailyStartKey
    TimerSymbolInfo.INSTANCE.reSchedule();

    //***************************************
    // Miscellaneous tab
    loApp.setFlywayAlwaysCheck(this.chkMigrationStatus.isSelected());
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
    final IntegerKeyItem[] laDailyIntervals = toApp.getDailyIntervals();

    int lnLength = laDrivers.length;
    this.cboDriver.getItems().clear();
    for (int i = 0; i < lnLength; ++i)
    {
      this.cboDriver.getItems().add(laDrivers[i]);
    }

    lnLength = laDailyIntervals.length;
    this.cboDailyDownloadInterval.getItems().clear();
    for (int i = 0; i < lnLength; ++i)
    {
      this.cboDailyDownloadInterval.getItems().add(laDailyIntervals[i]);
    }

    this.cboDriver.getSelectionModel().select(toApp.convertKeyToIndex(toApp.getRDBMS_Types(), toApp.getConnectionRDBMS_Key()));
    this.cboDailyDownloadInterval.getSelectionModel().select(toApp.convertKeyToIndex(laDailyIntervals, toApp.getDailyIntervalKey()));

    this.cboDriver.setOnAction(this);
    this.resetTextFields();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupButtons()
  {
    this.btnDefault.setTooltip(new Tooltip("Reset all of the above settings to the default"));

    this.btnDefault.setOnAction(this);
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
  private void resetTextFields()
  {
    final IntegerKeyItem loItem = this.cboDriver.getSelectionModel().getSelectedItem();

    final String lcDescription = loItem.getDescription();
    final boolean llApacheDerby = lcDescription.equals(Constants.DRIVER_VALUE_DERBY);
    final boolean llEditable = !llApacheDerby;

    this.setEditable(this.txtHost, llEditable);
    this.setEditable(this.txtUser, llEditable);
    this.setEditable(this.txtPassword, llEditable);

    if (!Main.isDevelopmentEnvironment())
    {
      return;
    }

    final boolean llMySQL = lcDescription.equals(Constants.DRIVER_VALUE_MYSQL5_PLUS);
    final boolean llPostgreSQL = lcDescription.equals(Constants.DRIVER_VALUE_POSTGRESQL9_PLUS);

    if (llMySQL || llPostgreSQL)
    {
      this.txtHost.setText(Constants.DEVELOPMENT_SERVER);
      this.txtDatabase.setText(Constants.DEVELOPMENT_DATABASE);
      this.txtUser.setText(Constants.DEVELOPMENT_USER);
      this.txtPassword.setText(Constants.DEVELOPMENT_PASSWORD);
    }
    else if (llApacheDerby)
    {
      this.resetDefaultValues();
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void resetDefaultValues()
  {
    final AppProperties loApp = AppProperties.INSTANCE;

    this.cboDriver.getSelectionModel().select(loApp.convertKeyToIndex(loApp.getRDBMS_Types(), loApp.getDefaultDriverKey()));

    this.txtHost.setText(loApp.getDefaultHost());
    this.txtDatabase.setText(loApp.getDefaultDatabase());
    this.txtUser.setText(loApp.getDefaultDerbyUser());
    this.txtPassword.setText(loApp.getDefaultDerbyPassword());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void handle(final ActionEvent toEvent)
  {
    final Object loObject = toEvent.getSource();
    if (loObject == this.btnDefault)
    {
      this.resetDefaultValues();
    }
    else if (loObject == this.cboDriver)
    {
      this.resetTextFields();
    }
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

/*
 * JEquity
 * Copyright(c) 2008-2020, Beowurks
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
import com.beowurks.jequity.view.checkbox.CheckBoxPlus;
import com.beowurks.jequity.view.combobox.ComboBoxIntegerKey;
import com.beowurks.jequity.view.textfield.PasswordFieldPlus;
import com.beowurks.jequity.view.textfield.TextFieldPlus;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class OptionsController implements EventHandler<ActionEvent>
{

  @FXML
  private ComboBoxIntegerKey cboDriver;
  @FXML
  private TextFieldPlus txtHost;
  @FXML
  private TextFieldPlus txtDatabase;
  @FXML
  private TextFieldPlus txtUser;
  @FXML
  private PasswordFieldPlus txtPassword;
  @FXML
  private Button btnDefault;

  @FXML
  private CheckBoxPlus chkManualFinancialData;

  @FXML
  private PasswordFieldPlus txtAlphaVantageAPIKey;
  @FXML
  private ComboBoxIntegerKey cboDailyDownloadInterval;

  @FXML
  private CheckBoxPlus chkMigrationStatus;

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
    final int lnKey = loApp.convertIndexToKey(loApp.getRDBMS_Types(), this.cboDriver.getSelectedIndex());

    loApp.setConnectionRDBMS_Key(lnKey);
    loApp.setConnectionHost(this.txtHost.getText().trim());
    loApp.setConnectionDatabase(this.txtDatabase.getText().trim());
    loApp.setConnectionUser(this.txtUser.getText().trim());
    // Don't trim the password as it could contain special characters or spaces at the end-points.
    loApp.setConnectionPassword(this.txtPassword.getText());

    //***************************************
    // Stock Data tab
    loApp.setDailyManualFinancialData(this.chkManualFinancialData.isSelected());

    loApp.setDailyIntervalKey(loApp.convertIndexToKey(loApp.getDailyIntervals(), this.cboDailyDownloadInterval.getSelectedIndex()));
    loApp.setAlphaVantageAPIKey(this.txtAlphaVantageAPIKey.getText().trim());

    // TimerSymbolInfo uses loApp.getDailyIntervalKey and loApp.getDailyStartKey
    TimerSymbolInfo.INSTANCE.reSchedule();

    //***************************************
    // Miscellaneous tab
    loApp.setFlywayAlwaysCheck(this.chkMigrationStatus.isSelected());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupCheckBoxes(final AppProperties toApp)
  {
    this.chkManualFinancialData.setSelected(toApp.getDailyManualFinancialData());
    this.chkMigrationStatus.setSelected(toApp.getFlywayAlwaysCheck());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupTextBoxes(final AppProperties toApp)
  {
    this.txtHost.setText(toApp.getConnectionHost());
    this.txtDatabase.setText(toApp.getConnectionDatabase());
    this.txtUser.setText(toApp.getConnectionUser());
    this.txtPassword.setText(toApp.getConnectionPassword());

    this.txtAlphaVantageAPIKey.setText(toApp.getAlphaVantageAPIKey());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupComboBoxes(final AppProperties toApp)
  {
    final ObservableList<IntegerKeyItem> laDrivers = toApp.getRDBMS_Types();
    final ObservableList<IntegerKeyItem> laDailyIntervals = toApp.getDailyIntervals();

    this.cboDriver.getItems().clear();
    this.cboDriver.getItems().addAll(laDrivers);

    this.cboDailyDownloadInterval.getItems().clear();
    this.cboDailyDownloadInterval.getItems().addAll(laDailyIntervals);

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
  private void resetTextFields()
  {
    final IntegerKeyItem loItem = this.cboDriver.getSelectedItem();

    final String lcDescription = loItem.getDescription();
    final boolean llApacheDerby = lcDescription.equals(Constants.DRIVER_VALUE_DERBY);
    final boolean llEditable = !llApacheDerby;

    this.txtHost.setReadOnly(!llEditable);
    this.txtUser.setReadOnly(!llEditable);
    this.txtPassword.setReadOnly(!llEditable);

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

/*
 * JEquity
 * Copyright(c) 2008-2023, Beowurks
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
import com.beowurks.jequity.view.spinner.SpinnerPlus;
import com.beowurks.jequity.view.textfield.PasswordFieldPlus;
import com.beowurks.jequity.view.textfield.TextFieldPlus;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;

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
  private CheckBoxPlus chkAutosetValuationDate;
  @FXML
  private PasswordFieldPlus txtAlphaVantageAPIKey;
  @FXML
  private TextFieldPlus txtAlphaVantageURL;
  @FXML
  private Label lblDailyDownloadInterval;
  @FXML
  private Label lblUpdateInterval;
  @FXML
  private Label lblUpdateIntervalDefault;
  @FXML
  private ComboBoxIntegerKey cboDailyDownloadInterval;
  @FXML
  private SpinnerPlus<Integer> spnUpdateInterval;

  @FXML
  private ComboBoxIntegerKey cboWebMarkerSource;

  @FXML
  private TextFieldPlus txtMarkerDescription;
  @FXML
  private TextFieldPlus txtMarkerLastTrade;

  @FXML
  private TextFieldPlus txtWebPageURL;

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
    this.setupSpinners(loApp);

    this.setupButtons();

    this.setupListeners();

    this.updateComponents();
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
    // Stock tab
    loApp.setManualFinancialData(this.chkManualFinancialData.isSelected());
    loApp.setAutosetValuationDate(this.chkAutosetValuationDate.isSelected());

    loApp.setDailyIntervalKey(loApp.convertIndexToKey(loApp.getDailyIntervals(), this.cboDailyDownloadInterval.getSelectedIndex()));
    loApp.setUpdateIntervalKey(this.spnUpdateInterval.getValue());
    loApp.setAlphaVantageAPIKey(this.txtAlphaVantageAPIKey.getText().trim());

    final int lnSource = loApp.convertIndexToKey(loApp.getWebMarkerSources(), this.cboWebMarkerSource.getSelectedIndex());
    // Set MarkerSource first.
    loApp.setWebMarkerSource(lnSource);
    loApp.setMarkerDescription(this.txtMarkerDescription.getText().trim());
    loApp.setMarkerLastTrade(this.txtMarkerLastTrade.getText().trim());

    loApp.setWebPageURL(this.txtWebPageURL.getText().trim());
    loApp.setAlphaVantageURL(this.txtAlphaVantageURL.getText().trim());

    // TimerSymbolInfo uses loApp.getDailyIntervalKey and loApp.getDailyStartKey
    TimerSymbolInfo.INSTANCE.reSchedule();

    //***************************************
    // Miscellaneous tab
    loApp.setFlywayAlwaysCheck(this.chkMigrationStatus.isSelected());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupCheckBoxes(final AppProperties toApp)
  {
    this.chkManualFinancialData.setSelected(toApp.getManualFinancialData());
    this.chkAutosetValuationDate.setSelected(toApp.getAutosetValuationDate());
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

    this.txtMarkerDescription.setText(toApp.getWebMarkerDescription());
    this.txtMarkerLastTrade.setText(toApp.getWebMarkerLastTrade());
    this.txtWebPageURL.setText(toApp.getWebPageURL());
    this.txtAlphaVantageURL.setText(toApp.getAlphaVantageURL());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupComboBoxes(final AppProperties toApp)
  {
    final ObservableList<IntegerKeyItem> laDrivers = toApp.getRDBMS_Types();
    final ObservableList<IntegerKeyItem> laDailyIntervals = toApp.getDailyIntervals();
    final ObservableList<IntegerKeyItem> laWebMarkerSources = toApp.getWebMarkerSources();

    // Initialize the combo boxes.
    this.cboDriver.getItems().clear();
    this.cboDriver.getItems().addAll(laDrivers);

    this.cboDailyDownloadInterval.getItems().clear();
    this.cboDailyDownloadInterval.getItems().addAll(laDailyIntervals);

    this.cboWebMarkerSource.getItems().clear();
    this.cboWebMarkerSource.getItems().addAll(laWebMarkerSources);

    // Now set the selected value.
    this.cboDriver.getSelectionModel().select(toApp.convertKeyToIndex(toApp.getRDBMS_Types(), toApp.getConnectionRDBMS_Key()));
    this.cboDailyDownloadInterval.getSelectionModel().select(toApp.convertKeyToIndex(laDailyIntervals, toApp.getDailyIntervalKey()));
    this.cboWebMarkerSource.getSelectionModel().select(toApp.convertKeyToIndex(toApp.getWebMarkerSources(), toApp.getWebMarkerSource()));

    // Now set any OnAction events.
    this.cboDriver.setOnAction(this);
    this.cboWebMarkerSource.setOnAction(this);

    // Now set the text fields to their initial correct state.
    this.resetTextFields();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupSpinners(final AppProperties toApp)
  {
    // Value factory.
    final SpinnerValueFactory<Integer> loValueFactory = //
      new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 30, Constants.UPDATE_INTERVAL_DEFAULT);

    this.spnUpdateInterval.setValueFactory(loValueFactory);
    this.spnUpdateInterval.getValueFactory().setValue(toApp.getUpdateIntervalKey());

    this.lblUpdateIntervalDefault.setText(String.format("Default is %d seconds", Constants.UPDATE_INTERVAL_DEFAULT));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupButtons()
  {
    this.btnDefault.setTooltip(new Tooltip("Reset all of the above settings to the default"));

    this.btnDefault.setOnAction(this);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupListeners()
  {
    this.chkManualFinancialData.selectedProperty().addListener((observable, oldValue, newValue) -> this.updateComponents());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void updateComponents()
  {
    final boolean llManualData = this.chkManualFinancialData.isSelected();

    this.cboDailyDownloadInterval.setReadOnly(llManualData);

    this.spnUpdateInterval.setReadOnly(llManualData);

    this.lblDailyDownloadInterval.setTextFill(llManualData ? Color.web("gray") : Color.web("black"));
    this.lblUpdateInterval.setTextFill(llManualData ? Color.web("gray") : Color.web("black"));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void resetTextFields()
  {
    // Database
    final IntegerKeyItem loDriverItem = this.cboDriver.getSelectedItem();

    final String lcDescription = loDriverItem.getDescription();
    final boolean llApacheDerby = lcDescription.equals(Constants.DRIVER_VALUE_DERBY);
    final boolean llDriverEditable = !llApacheDerby;

    this.txtHost.setReadOnly(!llDriverEditable);
    this.txtUser.setReadOnly(!llDriverEditable);
    this.txtPassword.setReadOnly(!llDriverEditable);

    // Markers
    final IntegerKeyItem loMarkerItem = this.cboWebMarkerSource.getSelectedItem();

    final int lnSource = loMarkerItem.getKey();
    final boolean llMarkerEditable = (lnSource == Constants.WEB_MARKER_SOURCE_MANUAL);
    this.txtMarkerDescription.setReadOnly(!llMarkerEditable);
    this.txtMarkerLastTrade.setReadOnly(!llMarkerEditable);
    this.txtWebPageURL.setReadOnly(!llMarkerEditable);
    this.txtAlphaVantageURL.setReadOnly(!llMarkerEditable);

    final AppProperties loApp = AppProperties.INSTANCE;
    this.txtMarkerDescription.setText(loApp.getWebMarkerDescription(lnSource));
    this.txtMarkerLastTrade.setText(loApp.getWebMarkerLastTrade(lnSource));
    this.txtWebPageURL.setText((loApp.getWebPageURL(lnSource)));
    this.txtAlphaVantageURL.setText(loApp.getAlphaVantageURL(lnSource));

    // If not in development environment, then stop here.
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
    else if (loObject == this.cboWebMarkerSource)
    {
      this.resetTextFields();
    }
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

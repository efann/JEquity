/*
 * J'EquityFX
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequityfx.controller;

import com.beowurks.jequityfx.main.Main;
import com.beowurks.jequityfx.utility.Misc;
import com.beowurks.jequityfx.view.dialog.AboutDialog;
import com.beowurks.jequityfx.view.misc.CheckForUpdates;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.controlsfx.control.StatusBar;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class MainFormController implements EventHandler<WindowEvent>
{
  @FXML
  private MenuBar menuBar;

  @FXML
  private MenuItem menuExit;

  @FXML
  private StatusBar statusBar;

  @FXML
  private Tab tabGroup;

  @FXML
  private Button btnUpdate;

  @FXML
  private Button btnRefresh;

@FXML
private ComboBox cboGroup;

  // ---------------------------------------------------------------------------------------------------------------------
  // From https://stackoverflow.com/questions/34785417/javafx-fxml-controller-constructor-vs-initialize-method
  @FXML
  public void initialize()
  {
    this.btnRefresh.setTooltip(new Tooltip("Refresh (reload) all of the data"));

    this.btnUpdate.setTooltip(new Tooltip("Update the daily stock information"));

    this.cboGroup.setTooltip(new Tooltip("Select the current group to display"));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public StatusBar getStatusBar()
  {
    return (this.statusBar);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @FXML
  private void exitApplication()
  {
    Misc.startShutdown();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @FXML
  private void showCredits()
  {
    final String lcTitle = String.format("Credits for %s", Main.getApplicationFullName());
    Misc.displayWebContent(lcTitle, "http://www.beowurks.com/ajax/node/32");
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @FXML
  private void showUpdates()
  {
    new CheckForUpdates();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @FXML
  private void showAbout()
  {
    final AboutDialog loDialog = new AboutDialog();
    loDialog.showAndWait();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  public void handle(WindowEvent toEvent)
  {
    Object loSource = toEvent.getSource();
    if (loSource instanceof Tooltip)
    {
      String lcText = ((Tooltip) loSource).getText();
      this.statusBar.setText(lcText);
    }

  }
// ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

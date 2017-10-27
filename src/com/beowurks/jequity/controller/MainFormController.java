/*
 * J'EquityFX
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.controller;

import com.beowurks.jequity.dao.hibernate.backuprestore.ThreadRestore;
import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import com.beowurks.jequity.view.dialog.AboutDialog;
import com.beowurks.jequity.view.misc.CheckForUpdates;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.stage.WindowEvent;
import org.apache.commons.io.FileUtils;
import org.controlsfx.control.StatusBar;

import java.io.File;
import java.io.IOException;
import java.net.URL;

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
  @FXML
  private void downloadSampleData()
  {
    if (Misc.yesNo("Do you want to add the Sample Data?\n\nBy the way, you can always remove this set at a later time."))
    {
      try
      {
        final URL loURL = new URL(Constants.SAMPLE_DATA_URL);
        final File loFile = new File(Constants.SAMPLE_DATA_TEMPFILE);

        FileUtils.copyURLToFile(loURL, loFile, 3000, 3000);

        ThreadRestore.INSTANCE.start(loFile);
      }
      catch (IOException loErr)
      {
        Misc.errorMessage(String.format("There was a problem downloading %s.\n\n%s\n\nPlease try again later.",
            Constants.SAMPLE_DATA_URL,
            loErr.getMessage()));
      }
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  public void handle(final WindowEvent toEvent)
  {
    final Object loSource = toEvent.getSource();
    if (loSource instanceof Tooltip)
    {
      final String lcText = ((Tooltip) loSource).getText();
      this.statusBar.setText(lcText);
    }

  }
// ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

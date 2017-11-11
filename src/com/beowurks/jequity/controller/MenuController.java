/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.controller;

import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.hibernate.backuprestore.ThreadRestore;
import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.AppProperties;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import com.beowurks.jequity.view.dialog.AboutDialog;
import com.beowurks.jequity.view.dialog.OptionsDialog;
import com.beowurks.jequity.view.misc.CheckForUpdates;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class MenuController
{
  @FXML
  private MenuItem menuRefresh;

  // ---------------------------------------------------------------------------------------------------------------------
  public MenuItem getMenuRefresh()
  {
    return (this.menuRefresh);
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
  private void showOptions()
  {
    final OptionsDialog loDialog = new OptionsDialog();

    final Optional<ButtonType> loResult = loDialog.showAndWait();

    final ButtonType loButton = loResult.orElse(ButtonType.CANCEL);

    if (loButton == ButtonType.OK)
    {
      loDialog.getController().updateAppProperties();
    }
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
      catch (final IOException loErr)
      {
        Misc.errorMessage(String.format("There was a problem downloading %s.\n\n%s\n\nPlease try again later.",
            Constants.SAMPLE_DATA_URL,
            loErr.getMessage()));
      }
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  @FXML
  private void restoreData()
  {
    final AppProperties loApp = AppProperties.INSTANCE;

    final FileChooser loFileChooser = new FileChooser();
    loFileChooser.setTitle("Restore from Backup File");
    loFileChooser.setInitialDirectory(new File(loApp.getBackupRestoreFolder()));
    loFileChooser.setInitialFileName("backup.xml");
    loFileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

    final File loBackupFile = loFileChooser.showOpenDialog(Main.getPrimaryStage());

    if (loBackupFile != null)
    {
      loApp.setBackupRestoreFolder(loBackupFile.getParent());

      ThreadRestore.INSTANCE.start(loBackupFile);
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  @FXML
  private void backupData()
  {
    final AppProperties loApp = AppProperties.INSTANCE;

    final FileChooser loFileChooser = new FileChooser();
    loFileChooser.setTitle("Save to Backup File");
    loFileChooser.setInitialDirectory(new File(loApp.getBackupRestoreFolder()));
    loFileChooser.setInitialFileName("backup.xml");
    loFileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

    final File loBackupFile = loFileChooser.showSaveDialog(Main.getPrimaryStage());

    if (loBackupFile != null)
    {
      loApp.setBackupRestoreFolder(loBackupFile.getParent());

      HibernateUtil.INSTANCE.backupToXML(loBackupFile);

      Misc.infoMessage(String.format("J'Equity has been saved to %s.", loBackupFile.getPath()));
    }

  }
  // ---------------------------------------------------------------------------------------------------------------------
  @FXML
  private void exportData()
  {
    final AppProperties loApp = AppProperties.INSTANCE;

    final FileChooser loFileChooser = new FileChooser();
    loFileChooser.setTitle("Export to File");
    loFileChooser.setInitialFileName(loApp.getExportFileChooserFilename());
    loFileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

    final File loExportFile = loFileChooser.showSaveDialog(Main.getPrimaryStage());

    if (loExportFile != null)
    {
      loApp.setExportFileChooserFilename(loExportFile.getPath());

      HibernateUtil.INSTANCE.backupToXML(loExportFile);

      Misc.infoMessage(String.format("J'Equity has been saved to %s.", loExportFile.getPath()));
    }


  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

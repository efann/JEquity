/*
 * JEquity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.controller;

import com.beowurks.jequity.dao.hibernate.FinancialEntity;
import com.beowurks.jequity.dao.hibernate.GroupEntity;
import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.hibernate.backuprestore.ThreadRestore;
import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.AppProperties;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import com.beowurks.jequity.view.dialog.AboutDialog;
import com.beowurks.jequity.view.dialog.OptionsDialog;
import com.beowurks.jequity.view.dialog.PasswordConfirmDialog;
import com.beowurks.jequity.view.misc.CheckForUpdates;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Optional;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class MenuController
{
  @FXML
  private MenuItem menuUpdate;

  @FXML
  private MenuItem menuRefresh;

  @FXML
  private MenuItem menuPrint;

  // ---------------------------------------------------------------------------------------------------------------------
  public MenuItem getMenuUpdate()
  {
    return (this.menuUpdate);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public MenuItem getMenuRefresh()
  {
    return (this.menuRefresh);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public MenuItem getMenuPrint()
  {
    return (this.menuPrint);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @FXML
  private void exitApplication()
  {
    Misc.setStatusText("Exiting " + Main.getApplicationName());

    Misc.startShutdown();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @FXML
  private void showCredits()
  {
    Misc.setStatusText("Showing the credits for " + Main.getApplicationName());

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
    Misc.setStatusText("Showing the About dialog for " + Main.getApplicationName());

    final AboutDialog loDialog = new AboutDialog();
    loDialog.showAndWait();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @FXML
  private void showOptions()
  {
    Misc.setStatusText("Showing the Options dialog for " + Main.getApplicationName());

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

      Misc.infoMessage(String.format("%s has been saved to %s.", Main.getApplicationName(), loBackupFile.getPath()));
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  @FXML
  private void exportData()
  {
    final AppProperties loApp = AppProperties.INSTANCE;

    final File loInitFile = new File(loApp.getExportFileChooserFilename());
    final FileChooser loFileChooser = new FileChooser();
    loFileChooser.setTitle("Export to File");
    loFileChooser.setInitialDirectory(loInitFile.getParentFile());
    loFileChooser.setInitialFileName(loInitFile.getName());
    loFileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

    final File loExportFile = loFileChooser.showSaveDialog(Main.getPrimaryStage());

    if (loExportFile != null)
    {
      loApp.setExportFileChooserFilename(loExportFile.getPath());

      this.generateCSV(loExportFile);
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  @FXML
  private void changeMasterPassword()
  {
    final PasswordConfirmDialog loDialog = new PasswordConfirmDialog();
    loDialog.setTitle("Change Master Password");

    final Optional<String> loResults = loDialog.showAndWait();

    final String lcMasterKey = loResults.orElse((null));
    if (lcMasterKey == null)
    {
      Misc.infoMessage("The Master Password was not reset due to one of the following:\n\tOK button not pressed\n\tThe passwords did not match");
      return;
    }

    if (lcMasterKey.isEmpty())
    {
      AppProperties.INSTANCE.setKey(AppProperties.getDefaultMasterKey());
      Misc.infoMessage("The Master Password has been reset to the default.\n\nThis application will no longer require a password at launch:\nNot recommended.");
    }
    else
    {
      AppProperties.INSTANCE.setKey(lcMasterKey);
      Misc.infoMessage("The new Master Password has been successfully applied.");
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  @FXML
  private void printJasperReport()
  {

  }

  // ---------------------------------------------------------------------------------------------------------------------
  @FXML
  private void generateCSV(final File toCSVFile)
  {
    final HibernateUtil loHibernate = HibernateUtil.INSTANCE;

    final Session loSession = loHibernate.getSession();

    final String lcSQL = String.format("SELECT {g.*}, {f.*} FROM %s g, %s f WHERE g.GROUPID = f.GROUPID ORDER BY g.GROUPID, f.DESCRIPTION ",
        loHibernate.getTableGroup(), loHibernate.getTableFinancial());

    final NativeQuery loQuery = loSession.createNativeQuery(lcSQL)
        .addEntity("g", GroupEntity.class)
        .addEntity("f", FinancialEntity.class);

    this.generateFile(loQuery, toCSVFile);

    loSession.close();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void generateFile(final NativeQuery toQuery, final File toFileName)
  {
    PrintWriter loPrintWriter = null;
    try
    {
      loPrintWriter = new PrintWriter(toFileName);
    }
    catch (final FileNotFoundException loErr)
    {
      Misc.showStackTraceInMessage(loErr, "Error in Creating File");
      return;
    }

    final String lcSeparator = "\t";
    final String lcEOL = System.getProperty("line.separator");

    final StringBuilder loContent = new StringBuilder();

    loContent.append(Constants.XML_GROUP_DESCRIPTION);
    loContent.append(lcSeparator);
    loContent.append(Constants.XML_DESCRIPTION);
    loContent.append(lcSeparator);
    loContent.append(Constants.XML_ACCOUNT);
    loContent.append(lcSeparator);
    loContent.append(Constants.XML_CATEGORY);
    loContent.append(lcSeparator);

    loContent.append(Constants.XML_PRICE);
    loContent.append(lcSeparator);
    loContent.append(Constants.XML_SHARES);
    loContent.append(lcSeparator);
    loContent.append(Constants.XML_SYMBOL);
    loContent.append(lcSeparator);
    loContent.append(Constants.XML_TYPE);
    loContent.append(lcSeparator);
    loContent.append(Constants.XML_RETIREMENT);
    loContent.append(lcSeparator);
    loContent.append(Constants.XML_VALUATIONDATE);
    loContent.append(lcSeparator);

    loContent.append(Constants.XML_COMMENTS);
    loContent.append(lcEOL);

    for (final Object loRow : toQuery.list())
    {
      final Object[] laEntities = (Object[]) loRow;
      final GroupEntity loGroupEntity = (GroupEntity) laEntities[0];
      final FinancialEntity loFinancialEntity = (FinancialEntity) laEntities[1];

      loContent.append(loGroupEntity.getDescription());
      loContent.append(lcSeparator);
      loContent.append(loFinancialEntity.getDescription());
      loContent.append(lcSeparator);
      loContent.append(loFinancialEntity.getAccount());
      loContent.append(lcSeparator);
      loContent.append(loFinancialEntity.getCategory());
      loContent.append(lcSeparator);
      loContent.append(loFinancialEntity.getPrice());
      loContent.append(lcSeparator);
      loContent.append(loFinancialEntity.getShares());
      loContent.append(lcSeparator);
      loContent.append(loFinancialEntity.getSymbol());
      loContent.append(lcSeparator);
      loContent.append(loFinancialEntity.getType());
      loContent.append(lcSeparator);
      loContent.append(loFinancialEntity.getRetirement() ? Constants.XML_TRUE : Constants.XML_FALSE);
      loContent.append(lcSeparator);
      loContent.append(loFinancialEntity.getValuationDate().toString());
      loContent.append(lcSeparator);

      // Replace tabs with 2 space characters.
      loContent.append(loFinancialEntity.getComments().replace("\t", "  "));

      loContent.append(lcEOL);
    }

    loPrintWriter.write(loContent.toString());
    loPrintWriter.close();

    Misc.infoMessage(String.format("%s has been saved.", toFileName.getPath()));

  }

  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

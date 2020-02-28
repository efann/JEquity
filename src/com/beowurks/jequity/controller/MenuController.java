/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.controller;

import com.beowurks.jequity.dao.hibernate.FinancialEntity;
import com.beowurks.jequity.dao.hibernate.GroupEntity;
import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.hibernate.threads.ThreadDownloadSymbolInfo;
import com.beowurks.jequity.dao.hibernate.threads.ThreadRestore;
import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.AppProperties;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import com.beowurks.jequity.view.dialog.AboutDialog;
import com.beowurks.jequity.view.dialog.OptionsDialog;
import com.beowurks.jequity.view.dialog.PasswordConfirmDialog;
import com.beowurks.jequity.view.misc.CheckForUpdates;
import de.codecentric.centerdevice.MenuToolkit;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Optional;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class MenuController
{
  @FXML
  private MenuBar menuBar;

  @FXML
  private MenuItem SystemMenu;

  @FXML
  private MenuItem menuAbout;

  @FXML
  private MenuItem menuAboutSys;

  @FXML
  private MenuItem menuOptions;

  @FXML
  private MenuItem menuOptionsSys;

  @FXML
  private MenuItem menuExit;

  @FXML
  private MenuItem menuExitSys;

  @FXML
  private MenuItem menuUpdate;

  @FXML
  private MenuItem menuRefresh;

  @FXML
  private MenuItem menuPrint;

  // ---------------------------------------------------------------------------------------------------------------------
  // From https://stackoverflow.com/questions/34785417/javafx-fxml-controller-constructor-vs-initialize-method
  @FXML
  public void initialize()
  {
    Platform.runLater(() ->
    {
      if (Misc.isMacintosh())
      {
        // Hide the menu items that are under SystemMenu.
        this.menuAbout.setVisible(false);
        this.menuOptions.setVisible(false);
        this.menuExit.setVisible(false);

        this.menuAboutSys.setText("About " + Main.getApplicationName());

        this.menuOptionsSys.setText("Preferences...");
        this.menuOptionsSys.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.META_DOWN));

        this.menuExitSys.setText("Quit " + Main.getApplicationName());
        this.menuExitSys.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.META_DOWN));

        try
        {
          final MenuBar loMenuBar = this.getMainMenu();

          // By the way, if you use loMenuBar.setUseSystemMenuBar(true), then
          // a default application menu is created. Your menu is moved to the top, but you end up
          // with two JEquity main menu items. Not a good look.

          // From https://stackoverflow.com/questions/42157377/osx-system-menubar-not-working-in-javafx
          // DO NOT use a Splash Screen. I've turned it off in the installation settings.
          // Apparently, AWT and JavaFX threads do not mix.
          final MenuToolkit loToolkit = MenuToolkit.toolkit();
          loToolkit.setMenuBar(Main.getPrimaryStage(), loMenuBar);

          // Remove the menu bar after successfully adding the menu to the MenuToolkit. Otherwise,
          // on error, the user would not have a menu. By the way, I have the following options:
          // - Use FXML to add menu and remove menu if Mac OS X
          // - Don't use FXML, and add menu programmatically if not Mac OS X.
          // It's easier to just remove.
          final Parent loParent = loMenuBar.getParent();
          if (loParent instanceof Pane)
          {
            final Pane loPane = (Pane) loParent;
            loPane.getChildren().remove(loMenuBar);
          }
        }
        catch (final Exception loErr)
        {
          Misc.showStackTraceInMessage(loErr, "Oops");
        }
      }
      else
      {
        // Simply hide the first menu element which is designed for use by Mac OS X.
        this.SystemMenu.setVisible(false);
      }
    });

  }

  // ---------------------------------------------------------------------------------------------------------------------
  public MenuBar getMainMenu()
  {
    return (this.menuBar);
  }

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
    Misc.displayWebContent(lcTitle, "http://www.beowurks.com/ajax/node/16");
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @FXML
  private void showHelp()
  {
    final String lcURL = "https://1drv.ms/b/s!Ak4RMu0v512Wgcl3vwvZXv4bmqL3uA?e=HQwrDs";
    Main.getMainHostServices().showDocument(lcURL);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @FXML
  private void showLicense()
  {
    Misc.setStatusText("Showing the open-source license for " + Main.getApplicationName());

    final String lcTitle = String.format("License for %s", Main.getApplicationFullName());
    Misc.displayWebContent(lcTitle, "https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html");
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

        ThreadRestore.INSTANCE.start(loFile, false);
        try
        {
          // Joining forces this thread to wait for ThreadRestore to finish.
          ThreadRestore.INSTANCE.getThread().join();
        }
        catch (final InterruptedException ignore)
        {
        }

        if (ThreadRestore.INSTANCE.isSuccessful())
        {
          if (Misc.yesNo("After successfully downloading the sample data, do you now want to update the stock information?\n\nBy the way, you may update the stock information at any time by pressing the Update button located in the toolbar."))
          {
            ThreadDownloadSymbolInfo.INSTANCE.start(true);
          }
        }
        else
        {
          Misc.errorMessage(String.format("There was an error in importing the Sample Datar:\n\n%s", ThreadRestore.INSTANCE.getErrorMessage()));
        }
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

    final FileChooser loFileChooser = this.getBackupRestoreChooser("Restore from Backup File");

    final File loBackupFile = loFileChooser.showOpenDialog(Main.getPrimaryStage());

    if (loBackupFile != null)
    {
      loApp.setBackupRestoreFolder(loBackupFile.getParent());

      ThreadRestore.INSTANCE.start(loBackupFile, false);
      try
      {
        // Joining forces this thread to wait for ThreadRestore to finish.
        ThreadRestore.INSTANCE.getThread().join();
      }
      catch (final InterruptedException ignore)
      {
      }

      if (ThreadRestore.INSTANCE.isSuccessful())
      {
        if (Misc.yesNo("After successfully restoring from backup, do you now want to update the stock information?\n\nBy the way, you may update the stock information at any time by pressing the Update button located in the toolbar."))
        {
          ThreadDownloadSymbolInfo.INSTANCE.start(true);
        }
      }
      else
      {
        Misc.errorMessage(String.format("There was an error in restoration:\n\n%s", ThreadRestore.INSTANCE.getErrorMessage()));
      }

    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  @FXML
  private FileChooser getBackupRestoreChooser(final String tcTitle)
  {
    final AppProperties loApp = AppProperties.INSTANCE;

    final FileChooser loFileChooser = new FileChooser();
    loFileChooser.setTitle(tcTitle);
    loFileChooser.setInitialDirectory(new File(loApp.getBackupRestoreFolder()));
    loFileChooser.setInitialFileName("backup.xml");
    loFileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

    return (loFileChooser);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @FXML
  private void backupData()
  {
    final AppProperties loApp = AppProperties.INSTANCE;

    final FileChooser loFileChooser = this.getBackupRestoreChooser("Save to Backup File");

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
    loContent.append(Constants.XML_OWNERSHIP);
    loContent.append(lcSeparator);
    loContent.append(Constants.XML_TAXSTATUS);
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
      loContent.append(loFinancialEntity.getOwnership());
      loContent.append(lcSeparator);
      loContent.append(loFinancialEntity.getTaxStatus());
      loContent.append(lcSeparator);
      loContent.append(loFinancialEntity.getValuationDate().toString());
      loContent.append(lcSeparator);

      // Get rid of characters that will effect importing to Excel.
      String lcComments = loFinancialEntity.getComments().replace("\t", " ").replace("\n", " ").replace("\r", " ").trim();

      // Get rid of double spaces
      while (lcComments.contains("  "))
      {
        lcComments = lcComments.replaceAll(" +", " ");
      }

      loContent.append(lcComments);

      loContent.append(lcEOL);
    }

    try
    {
      FileUtils.writeStringToFile(toFileName, loContent.toString(), Charset.defaultCharset());
    }
    catch (final IOException loErr)
    {
      Misc.showStackTraceInMessage(loErr, "Error in Creating File");
      return;
    }

    Misc.infoMessage(String.format("%s has been saved in tab-delimited format.", toFileName.getPath()));

  }

  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

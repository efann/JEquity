/*
 * JEquity
 * Copyright(c) 2008-2023, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */
package com.beowurks.jequity.utility;

import com.beowurks.jequity.dao.combobox.IntegerKeyItem;
import com.beowurks.jequity.dao.web.ConfigJSONSettings;
import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.view.dialog.PasswordDialog;
import javafx.collections.ObservableList;
import javafx.scene.control.ProgressBar;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;

// -----------------------------------------------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------------------------------------------
public final class AppProperties extends BaseProperties
{

  public static final AppProperties INSTANCE = new AppProperties();

  private boolean flSuccessfulRead = false;

  // -----------------------------------------------------------------------------------------------------------------------
  // Always have to start with the default password as the real one is never saved to disk.
  private AppProperties()
  {
    super(Constants.LOCAL_PATH, Constants.USER_FILENAME, "JEquity\u00a9 Parameters - DO NOT EDIT . . . please", AppProperties.getDefaultMasterKey(), false, false);

    // I'm not sure if this is the best strategy, a JDialog box inside of a constructor. However, in order to read
    // this particular property file, a password must be used. So, we'll try this for now.
    int lnPasswordAttempts = 0;
    while (!this.readProperties())
    {
      if (lnPasswordAttempts == 0)
      {
        // Don't worry about resetting this message, as other ones will follow.
        Misc.setStatusText("Initializing. . . .", ProgressBar.INDETERMINATE_PROGRESS);
      }

      if (lnPasswordAttempts++ < 3)
      {
        final String lcPassword = this.requestMasterKey();
        if (lcPassword != null)
        {
          this.setMasterKey(lcPassword);
          continue;
        }
      }

      Misc.startShutdown();
      return;
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  public boolean readProperties()
  {
    this.flSuccessfulRead = super.readProperties();

    return (this.flSuccessfulRead);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  public boolean writeProperties()
  {
    if (this.flSuccessfulRead)
    {
      return (super.writeProperties());
    }

    return (false);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public boolean isSuccessfullyRead()
  {
    return (this.flSuccessfulRead);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public String requestMasterKey()
  {
    Misc.setStatusText("Requesting Master Key. . . .");

    final PasswordDialog loDialog = new PasswordDialog();
    loDialog.setTitle("Enter Master Password");

    final Optional<String> loResults = loDialog.showAndWait();

    return (loResults.orElse((null)));
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public ObservableList<IntegerKeyItem> getRDBMS_Types()
  {
    return (Constants.RDBMS_DRIVERS);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public ObservableList<IntegerKeyItem> getDailyIntervals()
  {
    return (Constants.DAILY_INTERVAL);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public ObservableList<IntegerKeyItem> getWebMarkerSources()
  {
    return (Constants.WEB_SCRAPING_MARKER_SOURCE);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public String getDefaultDerbyUser()
  {
    return (Constants.DERBY_USERNAME);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public String getDerbyBootPassword()
  {
    return (Constants.DERBY_BOOT_PASSWORD);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public String getDefaultDerbyPassword()
  {
    return (Constants.DERBY_DEFAULT_PASSWORD);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public int getDefaultDriverKey()
  {
    return (Constants.DRIVER_KEY_DERBY);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public String getDefaultDatabase()
  {
    return (Constants.DERBY_PATH);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public String getDefaultHost()
  {
    return ("localhost");
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public String getConnectionURL()
  {
    final StringBuilder lcURL = new StringBuilder();
    final int lnKey = this.getConnectionRDBMS_Key();

    switch (lnKey)
    {
      case Constants.DRIVER_KEY_DERBY ->
      {
        final String lcFolder = this.getConnectionDatabase();
        lcURL.append("jdbc:derby:").append(lcFolder)
          .append(";upgrade=true").append(";bootPassword=").append(this.getDerbyBootPassword());
        final boolean llBrandNew = (!Files.isDirectory(Paths.get(lcFolder), LinkOption.NOFOLLOW_LINKS));
        if (!llBrandNew)
        {
          final File loFolder = new File(lcFolder);
          // Just in case it's an empty directory.
          final Collection<File> laFiles = FileUtils.listFilesAndDirs(loFolder, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
          // Will probably contain the lcFolder.
          if (laFiles.size() <= 1)
          {
            Misc.errorMessage(String.format("You must remove the empty folder of %s in order to continue.", lcFolder));
            return ("");
          }
        }
        if (llBrandNew)
        {
          lcURL.append(";create=true")
            .append(";dataEncryption=true");
        }
      }

      // From https://stackoverflow.com/questions/34189756/warning-about-ssl-connection-when-connecting-to-mysql-database
      // Also, with new Jar driver, https://community.oracle.com/message/14819583#14819583
      case Constants.DRIVER_KEY_MYSQL5_PLUS ->
        lcURL.append("jdbc:mysql://").append(this.getConnectionHost()).append("/").append(this.getConnectionDatabase()).append("?autoReconnect=true&useSSL=false&serverTimezone=UTC");
      case Constants.DRIVER_KEY_POSTGRESQL9_PLUS ->
        lcURL.append("jdbc:postgresql://").append(this.getConnectionHost()).append("/").append(this.getConnectionDatabase());
    }

    return (lcURL.toString());
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public String getConnectionDatabase()
  {
    return (this.getProperty(Constants.CONNECTION_DATABASE, Constants.DERBY_PATH).trim());
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public String getConnectionHost()
  {
    return (this.getProperty(Constants.CONNECTION_HOST, this.getDefaultHost()).trim());
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public String getConnectionUser()
  {
    return (this.getProperty(Constants.CONNECTION_USER, this.getDefaultDerbyUser()).trim());
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public String getConnectionPassword()
  {
    return (this.getProperty(Constants.CONNECTION_PASSWORD, this.getDefaultDerbyPassword()).trim());
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public int getConnectionRDBMS_Key()
  {
    return (this.getProperty(Constants.CONNECTION_RDBMS_KEY, Constants.RDBMS_DRIVERS.get(0).getKey()));
  }

  // -----------------------------------------------------------------------------------------------------------------------
  // Due to the fact that I keep increasing the minimum time, I'm just insuring that the latest minimum value
  // is returned.
  public int getDailyIntervalKey()
  {
    final int lnMinimumKeyValue = Constants.DAILY_INTERVAL.get(0).getKey();
    final int lnCurrentKeyValue = this.getProperty(Constants.DAILY_INTERVAL_KEY, lnMinimumKeyValue);

    final int lnKey = Integer.max(lnCurrentKeyValue, lnMinimumKeyValue);

    return (lnKey);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public int getUpdateIntervalKey()
  {
    return (this.getProperty(Constants.UPDATE_INTERVAL_KEY, Constants.UPDATE_INTERVAL_DEFAULT));
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public String getAlphaVantageAPIKey()
  {
    return (this.getProperty(Constants.ALPHAVANTAGE_API_KEY, "").trim());
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public String getConnectionRDBMS_Description()
  {
    final int lnIndex = this.convertKeyToIndex(this.getRDBMS_Types(), this.getConnectionRDBMS_Key());

    return (Constants.RDBMS_DRIVERS.get(lnIndex).toString());
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public String getBackupRestoreFolder()
  {
    return (this.getProperty(Constants.BACKUP_RESTORE_FOLDER, Misc.includeTrailingBackslash(System.getProperty("user.home"))));
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public boolean getManualFinancialData()
  {
    return (this.getProperty(Constants.DAILY_MANUAL_FINANCIAL_DATA, true));
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public boolean getAutosetValuationDate()
  {
    return (this.getProperty(Constants.DAILY_AUTOSET_VALUATION_DATE, true));
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public boolean getFlywayAlwaysCheck()
  {
    // When developing, sometimes I delete the entire databsae which will then need
    // to be regenerated. This will not happen, if I set a default value of false as
    // loAppProp.getFlywaySuccessfulJEquityVersion().compareTo(Main.getApplicationFullName()) != 0) will be equal
    // after the first time . . . unless I remember to also delete <user>.Properties.
    return (this.getProperty(Constants.FLYWAY_ALWAYS_CHECK, Main.isDevelopmentEnvironment()));
  }

  // -----------------------------------------------------------------------------------------------------------------------
  // This represents the version of JEquity where Flyway was successful.
  public String getFlywaySuccessfulJEquityVersion()
  {
    return (this.getProperty(Constants.FLYWAY_SUCCESSFUL_JEQUITY, "0.0.0.0.0"));
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public String getExportFileChooserFilename()
  {
    return (this.getProperty(Constants.EXPORT_FILECHOOSER_FILENAME, "export.csv"));
  }

  // -----------------------------------------------------------------------------------------------------------------------
  // Yes, private on purpose. Should only be accessed by getMarkerDescription.
  private String getManualMarkerDescription()
  {
    return (this.getProperty(Constants.MANUAL_WEB_MARKER_DESCRIPTION, Constants.WEB_MARKER_DEFAULT_VALUE_DESCRIPTION));
  }

  // -----------------------------------------------------------------------------------------------------------------------
  // Yes, private on purpose. Should only be accessed by getMarkerLastTrade.
  private String getManualMarkerLastTrade()
  {
    return (this.getProperty(Constants.MANUAL_WEB_MARKER_LASTTRADE, Constants.WEB_MARKER_DEFAULT_VALUE_LASTTRADE));
  }


  // -----------------------------------------------------------------------------------------------------------------------
  // Yes, private on purpose. Should only be accessed by getWebPageURL.
  private String getManualWebPageURL()
  {
    return (this.getProperty(Constants.MANUAL_WEB_PAGE_URL, Constants.WEB_PAGE_URL));
  }

  // -----------------------------------------------------------------------------------------------------------------------
  // Yes, private on purpose. Should only be accessed by getAlphaVantageURL.
  private String getManualAlphaVantageURL()
  {
    return (this.getProperty(Constants.MANUAL_ALPHA_VANTAGE_URL, Constants.ALPHA_VANTAGE_URL));
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public int getMarkerSource()
  {
    return (this.getProperty(Constants.WEB_MARKER_SOURCE, Constants.WEB_MARKER_SOURCE_BEOWURKS_DEFAULT));
  }

  // -----------------------------------------------------------------------------------------------------------------------
  // You can't use this.getMarkerSource(): it might not be set yet.
  // Like changing the combo box value in Options, but not yet saved.
  public String getMarkerDescription(final int tnSource)
  {
    switch (tnSource)
    {
      case Constants.WEB_MARKER_SOURCE_BEOWURKS_DEFAULT ->
      {
        return (ConfigJSONSettings.INSTANCE.getMarkerDescription());
      }

      case Constants.WEB_MARKER_SOURCE_APPLICATION ->
      {
        return (Constants.WEB_MARKER_DEFAULT_VALUE_DESCRIPTION);
      }

      case Constants.WEB_MARKER_SOURCE_MANUAL ->
      {
        return (this.getManualMarkerDescription());
      }

      default ->
      {
        System.err.printf("%d is not valid in AppProperties.getMarkerDescripton%n", tnSource);
        return (Constants.WEB_MARKER_DEFAULT_VALUE_DESCRIPTION);
      }
    }

  }

  // -----------------------------------------------------------------------------------------------------------------------
  // You can't use this.getMarkerSource(): it might not be set yet.
  // Like changing the combo box value in Options, but not yet saved.
  public String getMarkerLastTrade(final int tnSource)
  {
    switch (tnSource)
    {
      case Constants.WEB_MARKER_SOURCE_BEOWURKS_DEFAULT ->
      {
        return (ConfigJSONSettings.INSTANCE.getMarkerLastTrade());
      }

      case Constants.WEB_MARKER_SOURCE_APPLICATION ->
      {
        return (Constants.WEB_MARKER_DEFAULT_VALUE_LASTTRADE);
      }

      case Constants.WEB_MARKER_SOURCE_MANUAL ->
      {
        return (this.getManualMarkerLastTrade());
      }

      default ->
      {
        System.err.printf("%d is not valid in AppProperties.getMarkerLastTrade%n", tnSource);
        return (Constants.WEB_MARKER_DEFAULT_VALUE_LASTTRADE);
      }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public String getWebPageURL(final int tnSource)
  {
    switch (tnSource)
    {
      case Constants.WEB_MARKER_SOURCE_BEOWURKS_DEFAULT ->
      {
        return (ConfigJSONSettings.INSTANCE.getWebPageURL());
      }

      case Constants.WEB_MARKER_SOURCE_APPLICATION ->
      {
        return (Constants.WEB_PAGE_URL);
      }

      case Constants.WEB_MARKER_SOURCE_MANUAL ->
      {
        return (this.getManualWebPageURL());
      }

      default ->
      {
        System.err.printf("%d is not valid in AppProperties.getWebPageURL%n", tnSource);
        return (Constants.WEB_PAGE_URL);
      }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public String getAlphaVantageURL(final int tnSource)
  {
    switch (tnSource)
    {
      case Constants.WEB_MARKER_SOURCE_BEOWURKS_DEFAULT ->
      {
        return (ConfigJSONSettings.INSTANCE.getAlphaVantageURL());
      }

      case Constants.WEB_MARKER_SOURCE_APPLICATION ->
      {
        return (Constants.ALPHA_VANTAGE_URL);
      }

      case Constants.WEB_MARKER_SOURCE_MANUAL ->
      {
        return (this.getManualAlphaVantageURL());
      }

      default ->
      {
        System.err.printf("%d is not valid in AppProperties.getAlphaVantageURL%n", tnSource);
        return (Constants.ALPHA_VANTAGE_URL);
      }
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------
  // -----------------------------------------------------------------------------------------------------------------------
  public void setConnectionDatabase(final String tcValue)
  {
    this.setProperty(Constants.CONNECTION_DATABASE, tcValue);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public void setConnectionHost(final String tcValue)
  {
    this.setProperty(Constants.CONNECTION_HOST, tcValue);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public void setConnectionUser(final String tcValue)
  {
    this.setProperty(Constants.CONNECTION_USER, tcValue);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public void setConnectionPassword(final String tcValue)
  {
    this.setProperty(Constants.CONNECTION_PASSWORD, tcValue);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public void setConnectionRDBMS_Key(final int tnValue)
  {
    this.setProperty(Constants.CONNECTION_RDBMS_KEY, tnValue);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public void setDailyIntervalKey(final int tnValue)
  {
    this.setProperty(Constants.DAILY_INTERVAL_KEY, tnValue);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public void setUpdateIntervalKey(final int tnValue)
  {
    this.setProperty(Constants.UPDATE_INTERVAL_KEY, tnValue);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public void setAlphaVantageAPIKey(final String tcValue)
  {
    this.setProperty(Constants.ALPHAVANTAGE_API_KEY, tcValue);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public void setBackupRestoreFolder(final String tcValue)
  {
    this.setProperty(Constants.BACKUP_RESTORE_FOLDER, tcValue);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public void setManualFinancialData(final boolean tlValue)
  {
    this.setProperty(Constants.DAILY_MANUAL_FINANCIAL_DATA, tlValue);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public void setAutosetValuationDate(final boolean tlValue)
  {
    this.setProperty(Constants.DAILY_AUTOSET_VALUATION_DATE, tlValue);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public void setFlywayAlwaysCheck(final boolean tlValue)
  {
    this.setProperty(Constants.FLYWAY_ALWAYS_CHECK, tlValue);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  // This represents the version of JEquity where Flyway was successful.
  public void setFlywaySuccessfulJEquityVersion(final String tcValue)
  {
    this.setProperty(Constants.FLYWAY_SUCCESSFUL_JEQUITY, tcValue);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public void setExportFileChooserFilename(final String tcValue)
  {
    this.setProperty(Constants.EXPORT_FILECHOOSER_FILENAME, tcValue);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public void setMarkerSource(final int tnValue)
  {
    this.setProperty(Constants.WEB_MARKER_SOURCE, tnValue);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  // Yes, private on purpose. Should only be accessed by setMarkerDescription.
  private void setManualMarkerDescription(final String tcValue)
  {
    this.setProperty(Constants.MANUAL_WEB_MARKER_DESCRIPTION, tcValue);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  // Yes, private on purpose. Should only be accessed by setMarkerLastTrade.
  private void setManualMarkerLastTrade(final String tcValue)
  {
    this.setProperty(Constants.MANUAL_WEB_MARKER_LASTTRADE, tcValue);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  // Yes, private on purpose. Should only be accessed by setWebPageURL.
  private void setManualWebPageURL(final String tcValue)
  {
    this.setProperty(Constants.MANUAL_WEB_PAGE_URL, tcValue);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  // Yes, private on purpose. Should only be accessed by setAlphaVantageURL.
  private void setManualAlphaVantageURL(final String tcValue)
  {
    this.setProperty(Constants.MANUAL_ALPHA_VANTAGE_URL, tcValue);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public void setMarkerDescription(final int tnSource, final String tcValue)
  {
    if (tnSource == Constants.WEB_MARKER_SOURCE_MANUAL)
    {
      this.setManualMarkerDescription(tcValue);
    }
    else if ((tnSource != Constants.WEB_MARKER_SOURCE_BEOWURKS_DEFAULT) && (tnSource != Constants.WEB_MARKER_SOURCE_APPLICATION))
    {
      System.err.printf("%d is not valid in AppProperties.setMarkerDescripton%n", tnSource);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public void setMarkerLastTrade(final int tnSource, final String tcValue)
  {
    if (tnSource == Constants.WEB_MARKER_SOURCE_MANUAL)
    {
      this.setManualMarkerLastTrade(tcValue);
    }
    else if ((tnSource != Constants.WEB_MARKER_SOURCE_BEOWURKS_DEFAULT) && (tnSource != Constants.WEB_MARKER_SOURCE_APPLICATION))
    {
      System.err.printf("%d is not valid in AppProperties.setMarkerLastTrade%n", tnSource);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public void setWebPageURL(final int tnSource, final String tcValue)
  {
    if (tnSource == Constants.WEB_MARKER_SOURCE_MANUAL)
    {
      this.setManualWebPageURL(tcValue);
    }
    else if ((tnSource != Constants.WEB_MARKER_SOURCE_BEOWURKS_DEFAULT) && (tnSource != Constants.WEB_MARKER_SOURCE_APPLICATION))
    {
      System.err.printf("%d is not valid in AppProperties.setWebPageURL%n", tnSource);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public void setAlphaVantageURL(final int tnSource, final String tcValue)
  {
    if (tnSource == Constants.WEB_MARKER_SOURCE_MANUAL)
    {
      this.setManualAlphaVantageURL(tcValue);
    }
    else if ((tnSource != Constants.WEB_MARKER_SOURCE_BEOWURKS_DEFAULT) && (tnSource != Constants.WEB_MARKER_SOURCE_APPLICATION))
    {
      System.err.printf("%d is not valid in AppProperties.setAlphaVantageURL%n", tnSource);
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public int convertIndexToKey(final ObservableList<IntegerKeyItem> taItems, final int tnIndex)
  {
    int lnIndex = tnIndex;

    if (lnIndex < 0)
    {
      lnIndex = 0;
    }

    if (lnIndex >= taItems.size())
    {
      lnIndex = taItems.size() - 1;
    }

    return (taItems.get(lnIndex).getKey());
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public int convertKeyToIndex(final ObservableList<IntegerKeyItem> taItems, final int tnKey)
  {
    final int lnLength = taItems.size();

    int lnValue = 0;
    for (int i = 0; i < lnLength; ++i)
    {
      if (taItems.get(i).getKey() == tnKey)
      {
        lnValue = i;
        break;
      }
    }

    return (lnValue);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  // -----------------------------------------------------------------------------------------------------------------------
  // Never save the password to the property's file.
  public void setMasterKey(final String tcValue)
  {
    this.setKey(tcValue);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  public static String getDefaultMasterKey()
  {
    final StringBuilder loBuilder = new StringBuilder(Constants.USER_FILENAME);

    return ("*" + loBuilder.reverse().append("*"));
  }

  // -----------------------------------------------------------------------------------------------------------------------
}
// -----------------------------------------------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------------------------------------------

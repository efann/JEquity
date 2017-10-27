/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.controller;

import com.beowurks.jequity.dao.combobox.IntegerKeyItem;
import com.beowurks.jequity.dao.hibernate.GroupEntity;
import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.hibernate.backuprestore.ThreadRestore;
import com.beowurks.jequity.dao.hibernate.warehouses.TimerSymbolInfo;
import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import com.beowurks.jequity.view.dialog.AboutDialog;
import com.beowurks.jequity.view.misc.CheckForUpdates;
import com.sun.media.jfxmedia.events.PlayerStateEvent;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.stage.WindowEvent;
import org.apache.commons.io.FileUtils;
import org.controlsfx.control.StatusBar;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

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
  private ComboBox<IntegerKeyItem> cboGroup;

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
  public void refreshAllComponents()
  {
    final DateFormat loDateFormat = new SimpleDateFormat("HH:mm:ss");
    final Calendar loCalender = Calendar.getInstance();

    Misc.setStatusText(String.format("Refreshing all of the data @ %s. . . .", loDateFormat.format(loCalender.getTime())));

    Misc.setCursor(Cursor.HAND);

    if (Platform.isFxApplicationThread())
    {
      final Integer loGroupID = this.refreshGroupComboBox();
      HibernateUtil.INSTANCE.setGroupID(loGroupID);

      Main.getPrimaryStage().getScene().setCursor(Cursor.DEFAULT);
    }
    else
    {
      Platform.runLater(() ->
      {
        final Integer loGroupID = this.refreshGroupComboBox();
        HibernateUtil.INSTANCE.setGroupID(loGroupID);

        Misc.setCursor(Cursor.DEFAULT);
      });
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  private Integer refreshGroupComboBox()
  {
    Integer loInit = Constants.UNINITIALIZED;

    final ComboBox loCombo = this.cboGroup;

    loCombo.getItems().clear();

    final HibernateUtil loHibernate = HibernateUtil.INSTANCE;
    final Session loSession = loHibernate.getSession();

    final NativeQuery loQuery = loSession.createNativeQuery("SELECT * FROM " + loHibernate.getTableGroup())
        .addEntity(GroupEntity.class);

    final List<GroupEntity> loList = loQuery.list();
    for (final GroupEntity loRow : loList)
    {
      final Integer loID = loRow.getGroupID();
      final IntegerKeyItem loKeyItem = new IntegerKeyItem(loID, loRow.getDescription());
      loCombo.getItems().add(loKeyItem);

      if (loInit.compareTo(Constants.UNINITIALIZED) == 0)
      {
        loInit = loID;
        loCombo.setValue(loKeyItem);
      }
    }
    loSession.close();

    return (loInit);
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
      catch (final IOException loErr)
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

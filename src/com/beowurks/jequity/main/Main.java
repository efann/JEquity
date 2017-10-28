/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.main;


import com.beowurks.jequity.controller.MainFormController;
import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.hibernate.warehouses.TimerSymbolInfo;
import com.beowurks.jequity.utility.AppProperties;
import com.beowurks.jequity.utility.Misc;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class Main extends Application
{

  private static Stage PRIMARY_STAGE = null;

  private static boolean APPLICATION_DEVELOPMENT = true;
  private static String APPLICATION_TITLE = "J'Equity";
  private static String APPLICATION_VERSION = "(Development Version)";

  private static HostServices foHostService;

  private static MainFormController foMainController;

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  public void start(final Stage toPrimaryStage) throws Exception
  {
    Main.PRIMARY_STAGE = toPrimaryStage;
    Main.foHostService = this.getHostServices();

    Main.initializeEnvironment(this);

    final FXMLLoader loLoader = new FXMLLoader(this.getClass().getResource("/com/beowurks/jequity/view/MainForm.fxml"));

    // loLoader.load must be run before obtaining the controller, which kind of makes sense.
    final BorderPane loBorderPane = loLoader.load();

    Main.foMainController = loLoader.getController();

    toPrimaryStage.setTitle(Main.getApplicationFullName());

    final Rectangle2D loScreenBounds = Screen.getPrimary().getVisualBounds();

    toPrimaryStage.setScene(new Scene(loBorderPane, loScreenBounds.getWidth() * 0.75, loScreenBounds.getHeight() * 0.50));
    toPrimaryStage.show();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  public void stop()
  {
    AppProperties.INSTANCE.writeProperties();
    Misc.cleanFilesFromTemporaryDirectories();

    System.exit(0);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private static void initializeEnvironment(final Object toObject)
  {
    // If you use Main.class.getClass instead of toObject in a static method,
    // you'll get something like Java Runtime Environment 1.8.0_144
    final String lcTitle = toObject.getClass().getPackage().getImplementationTitle();
    final String lcVersion = toObject.getClass().getPackage().getImplementationVersion();

    Main.APPLICATION_DEVELOPMENT = ((lcTitle == null) || (lcVersion == null));

    if (!Main.APPLICATION_DEVELOPMENT)
    {
      Main.APPLICATION_TITLE = lcTitle;
      Main.APPLICATION_VERSION = lcVersion;
    }

    Misc.makeAllTemporaryDirectories();

    Platform.runLater(() ->
    {
      if (HibernateUtil.INSTANCE.initializeSuccess())
      {
        TimerSymbolInfo.INSTANCE.reSchedule();

        Main.getController().refreshAllComponents();
      }
    });


  }

  // ---------------------------------------------------------------------------------------------------------------------
  public static Stage getPrimaryStage()
  {
    return (Main.PRIMARY_STAGE);
  }

  // -----------------------------------------------------------------------------
  public static HostServices getMainHostServices()
  {
    return (Main.foHostService);
  }

  // -----------------------------------------------------------------------------
  public static String getApplicationFullName()
  {
    return (String.format("%s %s", Main.getApplicationName(), Main.getApplicationVersion()));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public static String getApplicationName()
  {
    return (Main.APPLICATION_TITLE);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public static String getApplicationVersion()
  {
    return (Main.APPLICATION_VERSION);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public static boolean isDevelopmentEnvironment()
  {
    return (Main.APPLICATION_DEVELOPMENT);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public static MainFormController getController()
  {
    return (Main.foMainController);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public static void main(final String[] taArgs)
  {
    Main.launch(taArgs);
  }

  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
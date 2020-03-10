/*
 * JEquity
 * Copyright(c) 2008-2020, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.main;

import com.beowurks.jequity.controller.MainFormController;
import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.dao.hibernate.threads.TimerSymbolInfo;
import com.beowurks.jequity.utility.AppProperties;
import com.beowurks.jequity.utility.Misc;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class Main extends Application
{

  private static Stage PRIMARY_STAGE = null;

  // This needs to be false, just in case the run time on a user's machine accesses
  // this variable before this variable is initialized.
  private static boolean APPLICATION_DEVELOPMENT = false;

  private static String APPLICATION_TITLE = "JEquity";
  private static String APPLICATION_VERSION = "(Development Version)";

  private static HostServices foHostService;

  private static MainFormController foMainController;

  private static FXMLLoader foMainLoader;

  private static BorderPane foMainBorderPane;

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  public void start(final Stage toPrimaryStage)
  {
    try
    {
      // Needs to be near the top: otherwise the Constant variables are initialized before
      // APPLICATION_DEVELOPMENT is set.
      Main.setDevelopment(this);

      Main.PRIMARY_STAGE = toPrimaryStage;
      Main.foHostService = this.getHostServices();

      Main.foMainLoader = new FXMLLoader(this.getClass().getResource("/com/beowurks/jequity/view/fxml/MainForm.fxml"));

      Main.initializeEnvironment();
      toPrimaryStage.setTitle(Main.getApplicationFullName());

      toPrimaryStage.getIcons().add(new Image("/com/beowurks/jequity/view/images/JEquity.png"));
      toPrimaryStage.setScene(new Scene(Main.foMainBorderPane));
      toPrimaryStage.show();
    }
    catch (final IOException loErr)
    {
      // You can also test by doing the following:
      //   cd C:\Program Files\JEquity\app>
      //   java -jar JEquity.jar
      // Then you will see the output of printStackTrace.
      loErr.printStackTrace();
    }
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
  private static void setDevelopment(final Object toObject)
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

  }

  // ---------------------------------------------------------------------------------------------------------------------
  private static void initializeEnvironment() throws IOException
  {
    // loLoader.load must be run before obtaining the controller, which kind of makes sense.
    Main.foMainBorderPane = Main.foMainLoader.load();

    Main.foMainController = Main.foMainLoader.getController();

    final Rectangle2D loPrimaryScreenBounds = Screen.getPrimary().getVisualBounds();

    // Set Stage boundaries to visible bounds of the main screen.
    Main.getPrimaryStage().setWidth(loPrimaryScreenBounds.getWidth() * 0.75);
    Main.getPrimaryStage().setHeight(loPrimaryScreenBounds.getHeight() * 0.75);

    Misc.makeAllTemporaryDirectories();

    Platform.runLater(() ->
    {
      if (HibernateUtil.INSTANCE.initializeSuccess())
      {
        TimerSymbolInfo.INSTANCE.reSchedule();

        // Always call with the parameter is true when initializing. Otherwise certain
        // variables will not be properly initialized.
        Main.getController().refreshAllComponents(true);
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

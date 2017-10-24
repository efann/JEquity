/*
 * J'EquityFX
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequityfx.main;

import com.beowurks.jequityfx.controller.MainFormController;
import javafx.application.Application;
import javafx.application.HostServices;
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
  private static String APPLICATION_TITLE = "J'Equity Fx Dev";
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

    final BorderPane loLoader = FXMLLoader.load(this.getClass().getResource("/com/beowurks/jequityfx/view/MainForm.fxml"));
//    Main.foMainController = loLoader.<MainFormController>getController();

    toPrimaryStage.setTitle(Main.getApplicationFullName());

    final Rectangle2D loScreenBounds = Screen.getPrimary().getVisualBounds();

    toPrimaryStage.setScene(new Scene(loLoader, loScreenBounds.getWidth() * 0.75, loScreenBounds.getHeight() * 0.50));
    toPrimaryStage.show();
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

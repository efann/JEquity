/*
 * JEquity
 * Copyright(c) 2008-2023, Beowurks
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
        // Must go here after Hibernate success
        // which ensures AppProperties.INSTANCE.isSuccessfullyRead()
        Main.enableComponentsByOptions();

        TimerSymbolInfo.INSTANCE.reSchedule();

        // Always call with the parameter is true when initializing. Otherwise certain
        // variables will not be properly initialized.
        Main.getController().refreshAllComponents(true);

        try
        {
          Main.libraryCheck();
        }
        catch (final Exception loErr)
        {
          Misc.errorMessage(loErr.getMessage());
        }
      }
    });

  }

  // ---------------------------------------------------------------------------------------------------------------------
  // I was getting javafx.fxml.LoadException:
  //   file:/C:/Program%20Files/JEquity/JEquity.jar!/com/beowurks/jequity/view/fxml/MainForm.fxml
  // at runtime.
  // Then I realized that AppProperties.INSTANCE not initialized yet due to password protection.
  // And I can't use in TabFinancialController.initialize and TabGroupController.initialize as
  // Main.getController() will be null when called indirectly from those initialize functions.
  private static void enableComponentsByOptions()
  {
    if (!Platform.isFxApplicationThread())
    {
      System.err.println("\nYou must call Main.enableComponentsByOptions from within the FX Application Thread. Notify the developer to fix.\n");
      System.exit(-1);
    }

    final MainFormController loController = Main.getController();

    loController.getTabFinancialController().resetComponentsOnModify(false);
    loController.getTabGroupController().resetComponentsOnModify(false);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // If one of the library jar files is signed, then at production run-time, you will get
  //   Error: Could not find or load main class com.beowurks.jequity.main.Main
  //   Caused by: java.lang.ClassNotFoundException: com.beowurks.jequity.main.
  // If you look in the META-INF folder of the JEquity.jar, you should only see one set of
  // *.SF & *.RSA files: BEOWURKS.SF & BEOWURKS.RSA.
  private static void libraryCheck() throws Exception
  {
    // The Bouncy Castle library is already signed.
    // These classes currently exist and, hopefully in future versions, one of them will still exist.
    // This library is not needed as JEquity does not output a signed PDF.
    final String[] taBouncyCastle = {"org.bouncycastle.jcajce.provider.config.ProviderConfiguration",
      "org.bouncycastle.util.Arrays",
      "org.bouncycastle.util.Strings",
      "org.bouncycastle.crypto.BlockCipher"
    };

    for (final String laClass : taBouncyCastle)
    {
      if (Misc.isClassAvailable(laClass))
      {
        throw new Exception(Main.getLibraryErrorMessage("Bouncy Castle"));
      }
    }

    // The Eclipse JDT Batch Compiler library is already signed.
    // These classes currently exist and, hopefully in future versions, one of them will still exist.
    // This library is not needed as JasperReports is not running any just-in-time compilation using this library: it uses the default JDK.
    final String[] taEclipseJDT = {"org.eclipse.jdt.internal.compiler.AbstractAnnotationProcessorManager",
      "org.eclipse.jdt.internal.compiler.tool.Archive",
      "org.eclipse.jdt.internal.compiler.batch.FileFinder",
      "org.eclipse.jdt.internal.compiler.parser.JavadocParser"
    };

    for (final String laClass : taEclipseJDT)
    {
      if (Misc.isClassAvailable(laClass))
      {
        throw new Exception(Main.getLibraryErrorMessage("Eclipse JDT Batch Compiler"));
      }
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  private static String getLibraryErrorMessage(final String tcLibraryName)
  {
    return (String.format("The %s library (signed) has been added to this project.\n\nNotify the developer to remove this jar file from the library (Probably in the JasperReports library).\nThe remove option may be found in Projects | Libraries | Classes: - sign.\nThis will only remove from project, not the disk.", tcLibraryName));
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

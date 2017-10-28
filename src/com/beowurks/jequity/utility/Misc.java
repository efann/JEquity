/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */
package com.beowurks.jequity.utility;

import com.beowurks.jequity.main.Main;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebView;
import org.apache.commons.io.FileUtils;
import org.controlsfx.control.StatusBar;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public final class Misc
{
  // Used by the isWindows routine
  static public final String OPERATING_SYSTEM = System.getProperty("os.name");

  // Used by the alerts
  static public final String ALERT_STYLE_SHEET = Thread.currentThread().getContextClassLoader().getResource("com/beowurks/jequity/view/Main.css").toExternalForm();

  private static final StringBuilder fcExceptionError = new StringBuilder(256);

  private static final boolean flUpdateTopComponentEditorsAlreadyInitialized = false;

  private static boolean flShutdownStarted = false;

  // Used in Replace
  static private final StringBuilder fcReplace = new StringBuilder(256);


  // ---------------------------------------------------------------------------------------------------------------------
  private Misc()
  {
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // From http://stackoverflow.com/questions/13605248/java-converting-image-to-bufferedimage
  public static BufferedImage getBufferedImage(final Image toImage)
  {
    if (toImage instanceof BufferedImage)
    {
      return ((BufferedImage) toImage);
    }

    // Create a buffered image with transparency
    final BufferedImage loBufferedImage = new BufferedImage(toImage.getWidth(null), toImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);

    // Draw the image on to the buffered image
    final Graphics2D loGraphics = loBufferedImage.createGraphics();
    loGraphics.drawImage(toImage, 0, 0, null);
    loGraphics.dispose();

    return (loBufferedImage);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public static boolean makeAllTemporaryDirectories()
  {
    return (Misc.makeDirectory(Constants.TEMPORARY_STOCK_PATH))
        && (Misc.makeDirectory(Constants.TEMPORARY_PATH));

  }

  // ---------------------------------------------------------------------------------------------------------------------
  // Cause deleteonexit doesn't always work. . . .
  // http://www.devx.com/Java/Article/22018/0/page/2 describes the problem.
  public static void cleanFilesFromTemporaryDirectories()
  {
    try
    {
      FileUtils.deleteDirectory(new File(Constants.TEMPORARY_PATH));
      FileUtils.deleteDirectory(new File(Constants.TEMPORARY_STOCK_PATH));
    }
    catch (final IOException loErr)
    {
      loErr.printStackTrace();
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public static void deleteFilesInFolder(final String tcFolder)
  {
    final File loTemp = new File(tcFolder);
    final File[] laFiles = loTemp.listFiles();

    if (laFiles == null)
    {
      return;
    }

    for (final File loFile : laFiles)
    {
      if (loFile.isFile())
      {
        try
        {
          loFile.delete();
        }
        catch (final SecurityException ignored)
        {
        }
      }
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // This routine is used to read text files in a Jar file.
  public static String getURIFileContents(final String tcURIPath)
  {
    final StringBuilder loFileContents = new StringBuilder();

    try
    {
      final InputStream loInput = Thread.currentThread().getContextClassLoader().getResourceAsStream(tcURIPath);

      final BufferedReader loReader = new BufferedReader(new InputStreamReader(loInput));

      String lcLine;
      while ((lcLine = loReader.readLine()) != null)
      {
        loFileContents.append(lcLine);
      }
      loReader.close();
    }
    catch (final Exception loErr)
    {
      Misc.showStackTraceInMessage(loErr, tcURIPath);
    }

    return (loFileContents.toString());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public static void setStatusText(final String tcMessage)
  {
    final StatusBar loStatus = Main.getController().getStatusBar();

    if (Platform.isFxApplicationThread())
    {
      loStatus.setText(tcMessage);
    }
    else
    {
      Platform.runLater(() -> {
        loStatus.setText(tcMessage);
      });
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public static void setStatusText(final String tcMessage, final double tnProgress)
  {
    final StatusBar loStatus = Main.getController().getStatusBar();

    if (Platform.isFxApplicationThread())
    {
      loStatus.setText(tcMessage);
      loStatus.setProgress(tnProgress);
    }
    else
    {
      Platform.runLater(() -> {
        loStatus.setText(tcMessage);
        loStatus.setProgress(tnProgress);
      });
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public static void setStatusText(final double tnProgress)
  {
    final StatusBar loStatus = Main.getController().getStatusBar();

    if (Platform.isFxApplicationThread())
    {
      loStatus.setProgress(tnProgress);
    }
    else
    {
      Platform.runLater(() -> {
        loStatus.setProgress(tnProgress);
      });
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public static void currentThreadSleep(final int tnMillisecondsDelay)
  {
    // Do not hang the UI Thread: the application would become
    // unresponsive.
    if (SwingUtilities.isEventDispatchThread())
    {
      return;
    }

    try
    {
      Thread.sleep(tnMillisecondsDelay);
    }
    catch (final InterruptedException loErr)
    {
      loErr.printStackTrace();
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  // From https://stackoverflow.com/questions/19804751/get-all-text-fields-values-and-id-in-javafx
  public static void initializeTextForDataEntry(final Pane toPane, final String tcValue)
  {
    for (final Node loNode : toPane.getChildren())
    {
      if (loNode instanceof TextField)
      {
        ((TextField) loNode).setText(tcValue);
      }
      else if (loNode instanceof CheckBox)
      {
        ((CheckBox) loNode).setSelected(false);
      }
      else if (loNode instanceof Pane)
      {
        Misc.initializeTextForDataEntry((Pane) loNode, tcValue);
      }

    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // From https://stackoverflow.com/questions/19804751/get-all-text-fields-values-and-id-in-javafx
  public static void setEnabledForDataEntry(final Pane toPane, final boolean tlEnable)
  {
    for (final Node loNode : toPane.getChildren())
    {
      if (loNode instanceof TextField)
      {
        ((TextField) loNode).setEditable(tlEnable);
      }
      else if (loNode instanceof TextArea)
      {
        ((TextArea) loNode).setEditable(tlEnable);
      }
      else if (loNode instanceof CheckBox)
      {
        loNode.setDisable(!tlEnable);
      }
      else if (loNode instanceof Pane)
      {
        Misc.setEnabledForDataEntry((Pane) loNode, tlEnable);
      }
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  public static boolean makeDirectory(final String tcDirectory)
  {
    try
    {
      FileUtils.forceMkdir(new File(tcDirectory));
    }
    catch (final IOException loErr)
    {
      Misc.errorMessage(String.format("Unable to create the directory of %s.\n%s", tcDirectory, loErr.getMessage()));
      return (false);
    }

    return (true);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public static boolean yesNo(final String tcMessage)
  {
    if (!Platform.isFxApplicationThread())
    {
      Misc.errorMessage("Misc.yesNo should only be run in the JavaFX thread as it needs to return an answer.");
      return (false);
    }

    final ButtonType loYes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
    final ButtonType loNo = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

    final Optional<ButtonType> loResult = Misc.baseAlert(tcMessage, "Question", AlertType.CONFIRMATION, loYes, loNo);

    return (loResult.isPresent() && (loResult.get() == loYes));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // From http://code.makery.ch/blog/javafx-dialogs-official/
  public static void showStackTraceInMessage(final Exception toException, final String tcTitle)
  {
    final StringWriter loStringWriter = new StringWriter();
    final PrintWriter loPrintWriter = new PrintWriter(loStringWriter);
    toException.printStackTrace(loPrintWriter);

    final TextArea loTextArea = new TextArea(loStringWriter.toString());
    loTextArea.setEditable(false);
    loTextArea.setWrapText(true);
    loTextArea.setMaxWidth(Double.MAX_VALUE);
    loTextArea.setMaxHeight(Double.MAX_VALUE);
    loTextArea.selectHome();

    GridPane.setVgrow(loTextArea, Priority.ALWAYS);
    GridPane.setHgrow(loTextArea, Priority.ALWAYS);

    final GridPane loGridPane = new GridPane();
    loGridPane.setMaxWidth(Double.MAX_VALUE);
    loGridPane.add(loTextArea, 0, 1);

    final AlertType lnAlertType = AlertType.ERROR;

    if (Platform.isFxApplicationThread())
    {
      Misc.baseAlert(loGridPane, tcTitle, lnAlertType);
    }
    else
    {
      Platform.runLater(() -> {
        Misc.baseAlert(loGridPane, tcTitle, lnAlertType);
      });
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  public static void displayWebContent(final String tcTitle, final String tcValue)
  {
    final WebView loWebView = new WebView();
    Misc.setupWebView(loWebView, false);

    final String lcInspect = tcValue.toLowerCase();
    if ((lcInspect.startsWith("http://")) || (lcInspect.startsWith("https://")))
    {
      loWebView.getEngine().load(tcValue);
    }
    else
    {
      loWebView.getEngine().loadContent(tcValue);
    }

    final AlertType lnAlertType = AlertType.INFORMATION;

    if (Platform.isFxApplicationThread())
    {
      Misc.baseAlert(loWebView, tcTitle, lnAlertType);
    }
    else
    {
      Platform.runLater(() -> {
        Misc.baseAlert(loWebView, tcTitle, lnAlertType);
      });
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  public static void infoMessage(final String tcMessage)
  {
    final String lcTitle = "Information";
    final AlertType lnAlertType = AlertType.INFORMATION;

    if (Platform.isFxApplicationThread())
    {
      Misc.baseAlert(tcMessage, lcTitle, lnAlertType);
    }
    else
    {
      Platform.runLater(() -> {
        Misc.baseAlert(tcMessage, lcTitle, lnAlertType);
      });
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public static void errorMessage(final String tcMessage)
  {
    final String lcTitle = "Error Message";
    final AlertType lnAlertType = AlertType.ERROR;

    if (Platform.isFxApplicationThread())
    {
      Misc.baseAlert(tcMessage, lcTitle, lnAlertType);
    }
    else
    {
      Platform.runLater(() -> {
        Misc.baseAlert(tcMessage, lcTitle, lnAlertType);
      });
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private static Optional<ButtonType> baseAlert(final Object toContent, final String tcTitle, final AlertType tnAlertType, final ButtonType... toButtonType)
  {
    final Alert loAlert = new Alert(tnAlertType);
    loAlert.setHeaderText(null);

    final DialogPane loDialogPane = loAlert.getDialogPane();

    loDialogPane.getStylesheets().add(Misc.ALERT_STYLE_SHEET);
    if ((toButtonType != null) && (toButtonType.length > 0))
    {
      loDialogPane.getButtonTypes().clear();
      for (final ButtonType loType : toButtonType)
      {
        loDialogPane.getButtonTypes().addAll(loType);
      }
    }

    if (toContent instanceof String)
    {
      loDialogPane.setContentText((String) toContent);
    }
    else if (toContent instanceof Node)
    {
      loDialogPane.setContent((Node) toContent);
    }

    loAlert.setTitle(tcTitle);
    loAlert.initOwner(Main.getPrimaryStage());

    final Optional<ButtonType> loResult = loAlert.showAndWait();

    return (loResult);
  }

  // ---------------------------------------------------------------------------------------------------------------------

  public static String includeTrailingBackslash(final String tcFileName)
  {
    final String lcSeparator = File.separator;

    return (tcFileName + (tcFileName.endsWith(lcSeparator) ? "" : lcSeparator));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // From
  // http://developer.apple.com/library/mac/#documentation/Java/Conceptual/Java14Development/00-Intro/JavaDevelopment.html
  static public boolean isMacintosh()
  {
    return (Misc.OPERATING_SYSTEM.toLowerCase().startsWith("mac"));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  static public boolean isWindows()
  {
    return (Misc.OPERATING_SYSTEM.toLowerCase().contains("windows"));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  static public void setCursor(final Cursor toCursor)
  {
    if (Platform.isFxApplicationThread())
    {
      Main.getPrimaryStage().getScene().setCursor(toCursor);
    }
    else
    {
      Platform.runLater(() -> {
        Main.getPrimaryStage().getScene().setCursor(toCursor);
      });
    }

  }
  // ---------------------------------------------------------------------------------------------------------------------

  static public byte[] binaryFileToBytes(final String tcFileName)
  {
    ByteArrayOutputStream loByteArrayOutputStream = null;

    try
    {
      final FileInputStream loFileInputStream = new FileInputStream(tcFileName);
      loByteArrayOutputStream = new ByteArrayOutputStream();

      final byte[] laBuffer = new byte[4096];
      int lnBytesRead;

      do
      {
        lnBytesRead = loFileInputStream.read(laBuffer);

        if (lnBytesRead != -1)
        {
          loByteArrayOutputStream.write(laBuffer, 0, lnBytesRead);
        }
      }
      while (lnBytesRead != -1);

      loFileInputStream.close();
    }
    catch (final IOException loErr)
    {
      loErr.printStackTrace();
    }

    return (loByteArrayOutputStream != null ? loByteArrayOutputStream.toByteArray() : null);
  }
  // ---------------------------------------------------------------------------------------------------------------------

  public static byte[] getKeyBytes(final String tcKey, final int tnFinalLength)
  {
    final byte[] laResults = new byte[tnFinalLength];
    for (int i = 0; i < tnFinalLength; ++i)
    {
      laResults[i] = ' ';
    }

    try
    {
      final byte[] laKey = tcKey.getBytes("UTF-8");
      final int lnLength = laKey.length;
      for (int i = 0; i < lnLength; ++i)
      {
        // Should keep wrapping around and XORing the values.
        final int lnMod = i % tnFinalLength;
        laResults[lnMod] = (i < tnFinalLength) ? laKey[i] : (byte) (laResults[lnMod] ^ laKey[i]);
      }

    }
    catch (final UnsupportedEncodingException ignored)
    {
    }

    return (laResults);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  static public void bytesToBinaryFile(final byte[] taBytes, final String tcFileName)
  {
    try
    {
      final DataOutputStream loDataOutputStream = new DataOutputStream(new FileOutputStream(tcFileName));
      loDataOutputStream.write(taBytes);
      loDataOutputStream.close();
    }
    catch (final IOException loErr)
    {
      loErr.printStackTrace();
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------

  public static void startShutdown()
  {
    if (Misc.flShutdownStarted)
    {
      return;
    }

    Misc.flShutdownStarted = true;

    Platform.exit();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // This only works with text files . . . as the name implies.
  static public void stringToFileText(final String tcExpression, final String tcFileName)
  {
    PrintWriter loPrintWriter = null;
    try
    {
      final FileWriter loFileWriter = new FileWriter(tcFileName);
      loPrintWriter = new PrintWriter(loFileWriter);

      loPrintWriter.print(tcExpression);
    }
    catch (final IOException loErr)
    {
      loErr.printStackTrace();
    }
    finally
    {
      if (loPrintWriter != null)
      {
        loPrintWriter.close();
      }
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  static public StringBuilder replaceAll(final String tcString, final String tcSearch, final String tcReplace)
  {
    if (tcSearch.isEmpty())
    {
      throw new IndexOutOfBoundsException("tcSearch is empty in Misc.replaceAll.");
    }

    // Avoid the infinite that occurs with replacing, say, 'a' with 'aa'.
    final boolean llReduction = (tcSearch.length() > tcReplace.length());

    Misc.clearStringBuilder(Misc.fcReplace);

    Misc.fcReplace.append(tcString);

    int lnIndex;
    int lnIndexFrom = 0;
    while ((lnIndex = Misc.fcReplace.indexOf(tcSearch, lnIndexFrom)) != -1)
    {
      Misc.fcReplace.replace(lnIndex, lnIndex + tcSearch.length(), tcReplace);

      lnIndexFrom = (llReduction) ? lnIndex : lnIndex + tcReplace.length();
    }

    return (Misc.fcReplace);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  static public void replaceAll(final StringBuilder toString, final String tcSearch, final String tcReplace)
  {
    if (tcSearch.isEmpty())
    {
      throw new IndexOutOfBoundsException("tcSearch is empty in Misc.replaceAll.");
    }

    // Avoid the infinite that occurs with replacing, say, 'a' with 'aa'.
    final boolean llReduction = (tcSearch.length() > tcReplace.length());

    int lnIndex;
    int lnIndexFrom = 0;

    while ((lnIndex = toString.indexOf(tcSearch, lnIndexFrom)) != -1)
    {
      toString.replace(lnIndex, lnIndex + tcSearch.length(), tcReplace);

      lnIndexFrom = (llReduction) ? lnIndex : lnIndex + tcReplace.length();
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  static public StringBuilder replaceAllIgnoreCase(final String tcString, final String tcSearch, final String tcReplace)
  {
    if (tcSearch.isEmpty())
    {
      throw new IndexOutOfBoundsException("tcSearch is empty in Misc.replaceAllIgnoreCase.");
    }

    // Avoid the infinite that occurs with replacing, say, 'a' with 'aa'.
    final boolean llReduction = (tcSearch.length() > tcReplace.length());

    Misc.clearStringBuilder(Misc.fcReplace);

    Misc.fcReplace.append(tcString);

    int lnIndex;
    int lnIndexFrom = 0;
    while ((lnIndex = Misc.fcReplace.toString().toLowerCase().indexOf(tcSearch.toLowerCase(), lnIndexFrom)) != -1)
    {
      Misc.fcReplace.replace(lnIndex, lnIndex + tcSearch.length(), tcReplace);

      lnIndexFrom = (llReduction) ? lnIndex : lnIndex + tcReplace.length();
    }

    return (Misc.fcReplace);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // Yeah, I know: it should be 0, lnLength-1. But according to the help,
  // Parameters:
  // start - The beginning index, inclusive.
  // end - The ending index, exclusive.
  // str - String that will replace previous contents.
  // end is exclusive. So really, end is the same thing as length of the string.
  static public void clearStringBuilder(final StringBuilder tcString)
  {
    final int lnLength = tcString.length();
    if (lnLength > 0)
    {
      tcString.delete(0, lnLength);
    }
  }


  // ---------------------------------------------------------------------------------------------------------------------
  // Unfortunately, WebView is a final class, so I can't create WebBrowser extends WebView. So. . . .
  // Anyway, from
  // https://stackoverflow.com/questions/15555510/javafx-stop-opening-url-in-webview-open-in-browser-instead
  static public void setupWebView(final WebView toWebView, final boolean tlContextMenuEnabled)
  {
    toWebView.setContextMenuEnabled(tlContextMenuEnabled);

    // https://stackoverflow.com/questions/12540044/execute-a-task-after-the-webview-is-fully-loaded
    toWebView.getEngine().getLoadWorker().stateProperty().addListener(
        (ObservableValue<? extends Worker.State> observable,
         Worker.State oldValue,
         Worker.State newValue) -> {
          if (newValue != Worker.State.SUCCEEDED)
          {
            return;
          }

          final NodeList loNodeList = toWebView.getEngine().getDocument().getElementsByTagName("a");
          for (int i = 0; i < loNodeList.getLength(); i++)
          {
            final org.w3c.dom.Node loNode = loNodeList.item(i);
            final org.w3c.dom.events.EventTarget loEventTarget = (org.w3c.dom.events.EventTarget) loNode;
            loEventTarget.addEventListener("click", new EventListener()
            {
              @Override
              public void handleEvent(final Event toEvent)
              {
                final org.w3c.dom.events.EventTarget loCurrentTarget = toEvent.getCurrentTarget();
                final org.w3c.dom.html.HTMLAnchorElement loAnchorElement = (org.w3c.dom.html.HTMLAnchorElement) loCurrentTarget;
                final String lcHref = loAnchorElement.getHref();

                toEvent.preventDefault();
                Main.getMainHostServices().showDocument(lcHref);
              }
            }, false);
          }
        });
  }


  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
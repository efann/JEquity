/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */
package com.beowurks.jequity.utility;

import com.beowurks.jequity.controller.MainFormController;
import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.view.textfield.DatePickerPlus;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.NodeList;

import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public final class Misc
{
  static private NumberFormat foCurrencyFormat = null;
  static private NumberFormat foIntegerFormat = null;
  static private NumberFormat foDoubleFormat = null;

  static private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, MMM d, yyyy");

  // Used by the isWindows routine
  static public final String OPERATING_SYSTEM = System.getProperty("os.name", "Unknown OS");

  // Used by the alerts
  static public final String ALERT_STYLE_SHEET = Thread.currentThread().getContextClassLoader().getResource("com/beowurks/jequity/view/css/Main.css").toExternalForm();

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
    if (Main.getController() == null)
    {
      return;
    }

    final Label loStatusMessage = Main.getController().getStatusMessage();

    if (Platform.isFxApplicationThread())
    {
      loStatusMessage.setText(tcMessage);
    }
    else
    {
      Platform.runLater(() -> loStatusMessage.setText(tcMessage));
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public static void setStatusText(final String tcMessage, final double tnProgress)
  {
    final MainFormController loController = Main.getController();

    final Label loStatusMessage = loController.getStatusMessage();
    final ProgressBar loProgressBar = loController.getProgressBar();
    final Label loProgressLabel = loController.getProgressLabel();

    final String lcPercentage = Misc.getPercentage(tnProgress);

    if (Platform.isFxApplicationThread())
    {
      loStatusMessage.setText(tcMessage);
      loProgressBar.setProgress(tnProgress);
      loProgressLabel.setText(lcPercentage);
    }
    else
    {
      Platform.runLater(() ->
      {
        loStatusMessage.setText(tcMessage);
        loProgressBar.setProgress(tnProgress);
        loProgressLabel.setText(lcPercentage);
      });
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public static void setStatusText(final double tnProgress)
  {
    final MainFormController loController = Main.getController();

    final ProgressBar loProgressBar = loController.getProgressBar();
    final Label loProgressLabel = loController.getProgressLabel();

    final String lcPercentage = Misc.getPercentage(tnProgress);

    if (Platform.isFxApplicationThread())
    {
      loProgressBar.setProgress(tnProgress);
      loProgressLabel.setText(lcPercentage);
    }
    else
    {
      Platform.runLater(() ->
      {
        loProgressBar.setProgress(tnProgress);
        loProgressLabel.setText(lcPercentage);
      });

    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private static String getPercentage(final double tnProgress)
  {
    if (tnProgress == ProgressBar.INDETERMINATE_PROGRESS)
    {
      return ("Working. . . .");
    }

    final double lnProgress = 100.0 * tnProgress;
    return ((lnProgress <= 0.5) ? "" : String.format("%.0f %%", lnProgress));
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
      Platform.runLater(() -> Misc.baseAlert(loGridPane, tcTitle, lnAlertType));
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
      Platform.runLater(() -> Misc.baseAlert(loWebView, tcTitle, lnAlertType));
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
      Platform.runLater(() -> Misc.baseAlert(tcMessage, lcTitle, lnAlertType));
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
      Platform.runLater(() -> Misc.baseAlert(tcMessage, lcTitle, lnAlertType));
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
      // From https://stackoverflow.com/questions/35693180/javafx-wrap-content-text-in-alert-dialg
      final Text loMessage = new Text((String) toContent);
      // Seems to be a good compromise for the size and close to the original size for alerts.
      loMessage.setWrappingWidth(400.0);

      loDialogPane.setContent(loMessage);
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
      Platform.runLater(() -> Main.getPrimaryStage().getScene().setCursor(toCursor));
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

    final byte[] laKey = tcKey.getBytes(StandardCharsets.UTF_8);
    final int lnLength = laKey.length;
    for (int i = 0; i < lnLength; ++i)
    {
      // Should keep wrapping around and XORing the values.
      final int lnMod = i % tnFinalLength;
      laResults[lnMod] = (i < tnFinalLength) ? laKey[i] : (byte) (laResults[lnMod] ^ laKey[i]);
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
         Worker.State newValue) ->
        {
          if (newValue != Worker.State.SUCCEEDED)
          {
            return;
          }

          final NodeList loNodeList = toWebView.getEngine().getDocument().getElementsByTagName("a");
          for (int i = 0; i < loNodeList.getLength(); i++)
          {
            final org.w3c.dom.Node loNode = loNodeList.item(i);
            final org.w3c.dom.events.EventTarget loEventTarget = (org.w3c.dom.events.EventTarget) loNode;
            loEventTarget.addEventListener("click", toEvent ->
            {
              final org.w3c.dom.events.EventTarget loCurrentTarget = toEvent.getCurrentTarget();
              final org.w3c.dom.html.HTMLAnchorElement loAnchorElement = (org.w3c.dom.html.HTMLAnchorElement) loCurrentTarget;
              final String lcHref = loAnchorElement.getHref();

              toEvent.preventDefault();
              Main.getMainHostServices().showDocument(lcHref);
            }, false);
          }
        });
  }


  // ---------------------------------------------------------------------------------------------------------------------
  static public NumberFormat getCurrencyFormat()
  {
    if (Misc.foCurrencyFormat == null)
    {
      Misc.foCurrencyFormat = NumberFormat.getCurrencyInstance();
      Misc.foCurrencyFormat.setMinimumFractionDigits(4);
      Misc.foCurrencyFormat.setMaximumFractionDigits(4);
    }

    return (Misc.foCurrencyFormat);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  static public NumberFormat getDoubleFormat()
  {
    if (Misc.foDoubleFormat == null)
    {
      Misc.foDoubleFormat = NumberFormat.getNumberInstance();
      Misc.foDoubleFormat.setMinimumFractionDigits(4);
      Misc.foDoubleFormat.setMaximumFractionDigits(4);
    }

    return (Misc.foDoubleFormat);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  static public NumberFormat getIntegerFormat()
  {
    if (Misc.foIntegerFormat == null)
    {
      Misc.foIntegerFormat = NumberFormat.getNumberInstance();
    }

    return (Misc.foIntegerFormat);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  static public SimpleDateFormat getDateFormat()
  {
    return (Misc.DATE_FORMAT);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  static public double getDoubleFromTextField(final TextField toField)
  {
    double lnValue = 0.0;
    try
    {
      lnValue = Double.parseDouble(toField.getText().trim());
    }
    catch (final NumberFormatException ignored)
    {
    }

    return (lnValue);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  static public void setEditableForDatePicker(final DatePickerPlus toPicker, final boolean tlEditable)
  {
    final String lcStyle = tlEditable ? "" : Constants.DISABLED_CONTROL_BACKGROUND;

    toPicker.getEditor().setEditable(tlEditable);
    toPicker.getEditor().setStyle(lcStyle);

    // The following hides / shows the button for the calendar.
    if (tlEditable)
    {
      if (toPicker.getStyleClass().contains(Constants.DATEPICKER_NON_EDITABLE))
      {
        toPicker.getStyleClass().removeAll(Constants.DATEPICKER_NON_EDITABLE);
      }
    }
    else if (!toPicker.getStyleClass().contains(Constants.DATEPICKER_NON_EDITABLE))
    {
      toPicker.getStyleClass().add(Constants.DATEPICKER_NON_EDITABLE);
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  public static boolean checkForFontAvailability(final String tcFontFamily)
  {
    final List<String> laFontNames = Font.getFamilies();

    final int lnFonts = laFontNames.size();

    for (int i = 0; i < lnFonts; ++i)
    {
      // Font family names appear to be case-sensitive as they are returned in proper title case.
      if (laFontNames.get(i).equals(tcFontFamily))
      {
        return (true);
      }
    }

    return (false);
  }

  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

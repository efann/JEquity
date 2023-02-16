/*
 * JEquity
 * Copyright(c) 2008-2023, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.misc;

import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import javafx.application.Platform;
import javafx.scene.Cursor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URI;
import java.net.URISyntaxException;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class CheckForUpdates implements Runnable
{
  // 2 is a setting for the web controller. 1 is used by Trash Wizard
  private final static int JEQUITY = 2;

  private String fcAppFolder = "";
  private String fcAppVersion = "";

  // ---------------------------------------------------------------------------------------------------------------------
  public CheckForUpdates()
  {
    Misc.setCursor(Cursor.WAIT);

    final Thread loThread = new Thread(this);

    loThread.setPriority(Thread.NORM_PRIORITY);
    loThread.start();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  public void run()
  {
    final boolean llCurrentVersion = this.getCurrentVersion();

    Platform.runLater(() ->
    {
      if (llCurrentVersion)
      {
        Misc.displayWebContent(String.format("Check for %s Updates", Main.getApplicationName()), CheckForUpdates.this.buildHTMLCode());
      }
      else
      {
        final String lcMessage = "There was a problem in obtaining the update information. Please try again later.";
        Misc.setStatusText(lcMessage);
        Misc.errorMessage(lcMessage);
      }

      Misc.setCursor(Cursor.DEFAULT);
    });

  }

  // ---------------------------------------------------------------------------------------------------------------------

  private boolean getCurrentVersion()
  {
    final String lcURL = String.format("https://www.beowurks.com/ajax/version/%d", CheckForUpdates.JEQUITY);

    Document loDoc = null;
    for (int lnTries = 0; (lnTries < Constants.JSOUP_TIMEOUT_TRIES) && (loDoc == null); ++lnTries)
    {
      try
      {
        // Highly recommended to set the userAgent.
        loDoc = Jsoup.connect(lcURL)
          .followRedirects(true)
          .userAgent(Constants.getUserAgent())
          .data("name", "jsoup")
          .maxBodySize(0)
          .timeout(Constants.WEB_TIME_OUT)
          .get();
      }
      catch (final Exception loErr)
      {
        loDoc = null;
      }
    }

    if (loDoc != null)
    {
      try
      {
        this.fcAppFolder = loDoc.select("#app_folder").html();
      }
      catch (final Exception loErr)
      {
        this.fcAppFolder = "";
      }
      try
      {
        this.fcAppVersion = loDoc.select("#app_version").html();
      }
      catch (final Exception loErr)
      {
        this.fcAppVersion = "";
      }
    }

    return ((loDoc != null) && (!this.fcAppFolder.isEmpty()) && (!this.fcAppVersion.isEmpty()));
  }
  // -----------------------------------------------------------------------------

  private String buildHTMLCode()
  {
    final String lcCurrentVersion = Main.getApplicationVersion();

    URI loIcon;
    try
    {
      loIcon = this.getClass().getResource("/com/beowurks/jequity/view/images/connect_established32.png").toURI();

    }
    catch (final URISyntaxException loErr)
    {
      // By the way, if loIcon is null, then the WebView displays an empty image, which is fine for now.
      loIcon = null;
    }

    final StringBuilder loHtml = new StringBuilder();

    loHtml.append("<html>");

    loHtml.append("<head>");
    loHtml.append("<style>");
    loHtml.append("body {background-color: #C0D9D9; border: none; padding: 20px; font-family: Arial; font-size: 1.0em;}");
    loHtml.append("a,a:link,a:visited { color: #006400; text-decoration: none;}");
    loHtml.append("a:active,a:hover { text-decoration: underline;}");
    loHtml.append(".checkimage { float: left; width: 15%; }");
    loHtml.append(".checktext { float: left; width: 85%; }");
    loHtml.append(".checkheader { background-color: darkgray; padding: 20px; overflow: hidden;}");
    loHtml.append("</style>");
    loHtml.append("</head>");

    loHtml.append("<body>");

    loHtml.append("<div class='checkheader'>");
    loHtml.append(String.format("<div class='checkimage'><img src='%s'></div>", loIcon));
    loHtml.append("<div class='checktext'>&nbsp;&nbsp;<b>Check for Updates</b><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Below are the results.</div>");
    loHtml.append("</div>");

    loHtml.append("<div>");
    loHtml.append("<table border=\"1\" cellpadding=\"5\" cellspacing=\"5\" width=\"100%\">");
    loHtml.append("<tr>");
    loHtml.append("<th>Component</th>");
    loHtml.append("<th>Current</th>");
    loHtml.append("<th>Available</th>");
    loHtml.append("</tr>");

    loHtml.append("<tr>");
    loHtml.append("<td>")
      .append(Main.getApplicationName())
      .append("</td>");
    loHtml.append("<td>")
      .append(lcCurrentVersion)
      .append("</td>");
    loHtml.append("<td>")
      .append(this.fcAppVersion)
      .append("</td>");
    loHtml.append("</tr>");
    loHtml.append("</table>");
    loHtml.append("</div>");

    loHtml.append("<p>&nbsp;</p>");

    loHtml.append("<p class='header'>");
    if (this.fcAppFolder.isEmpty() || this.fcAppVersion.isEmpty())
    {
      loHtml.append("We're unable to determine the available version. Please try again later.");
    }
    else if (lcCurrentVersion.compareTo(this.fcAppVersion) == 0)
    {
      loHtml.append("<b>You have the latest version installed</b>: ");
      loHtml.append(Main.getApplicationFullName())
        .append("!");
    }
    else
    {
      loHtml.append("You should download &amp; install the latest version located at<br><a href='")
        .append(this.fcAppFolder)
        .append("'>")
        .append(this.fcAppFolder)
        .append("</a>");
    }

    loHtml.append("</p>");
    loHtml.append("</body>");
    loHtml.append("</html>");

    return (loHtml.toString());
  }
  // ---------------------------------------------------------------------------------------------------------------------


}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

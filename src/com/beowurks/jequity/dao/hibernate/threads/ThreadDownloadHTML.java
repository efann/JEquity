/*
 * JEquity
 * Copyright(c) 2008-2022, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */
package com.beowurks.jequity.dao.hibernate.threads;

import com.beowurks.jequity.utility.AppProperties;
import com.beowurks.jequity.utility.Constants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class ThreadDownloadHTML extends ThreadBase
{

  protected String[] faCurrentTextList;

  // ---------------------------------------------------------------------------------------------------------------------
  public ThreadDownloadHTML()
  {
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected Document getDocument(final String tcDailyURL)
  {
    Document loDoc = null;

    for (int lnTries = 0; (lnTries < Constants.JSOUP_TIMEOUT_TRIES) && (loDoc == null); ++lnTries)
    {
      try
      {
        // Highly recommended to set the userAgent.
        loDoc = Jsoup.connect(tcDailyURL)
          .followRedirects(false)
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

    return (loDoc);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected String getDescriptionFromHtml(final Document toDoc)
  {
    final AppProperties loApp = AppProperties.INSTANCE;
    final int lnSource = loApp.getMarkerSource();

    String lcDescription = this.getHTML(toDoc, loApp.getMarkerDescription(lnSource));
    if (lcDescription.isEmpty())
    {
      lcDescription = Constants.UNKNOWN_STOCK_SYMBOL;
    }
    else
    {
      // Get rid of anything between parentheses including the parentheses
      lcDescription = lcDescription.replaceAll("\\(.*\\)", "").trim();
      lcDescription = lcDescription.replaceAll("&amp;", "&").trim();
    }

    return (lcDescription);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void refreshCurrentTextList(final Document toDoc, final String tcSymbol)
  {
    String lcText = toDoc.text();

    // Trying to trim as much extraneous text as possible.
    final String lcStartPhrase = String.format("(%s)", tcSymbol.trim());
    final int lnPos = lcText.indexOf(lcStartPhrase);
    if (lnPos != -1)
    {
      lcText = lcText.substring(lnPos + lcStartPhrase.length());
    }

    lcText = this.replaceSpacing(lcText, Constants.YAHOO_DAILY_PREVCLOSE);
    lcText = this.replaceSpacing(lcText, Constants.YAHOO_DAILY_OPEN);
    lcText = this.replaceSpacing(lcText, Constants.YAHOO_DAILY_BID);
    lcText = this.replaceSpacing(lcText, Constants.YAHOO_DAILY_ASK);
    lcText = this.replaceSpacing(lcText, Constants.YAHOO_DAILY_DAYRANGE);
    lcText = this.replaceSpacing(lcText, Constants.YAHOO_DAILY_YEARRANGE);
    lcText = this.replaceSpacing(lcText, Constants.YAHOO_DAILY_VOLUME);
    lcText = this.replaceSpacing(lcText, Constants.YAHOO_DAILY_AVGVOLUME);
    lcText = this.replaceSpacing(lcText, Constants.YAHOO_DAILY_MARKETCAP);
    lcText = this.replaceSpacing(lcText, Constants.YAHOO_DAILY_PE);
    lcText = this.replaceSpacing(lcText, Constants.YAHOO_DAILY_EPS);
    lcText = this.replaceSpacing(lcText, Constants.YAHOO_DAILY_DIVIDENDYIELD);
    lcText = this.replaceSpacing(lcText, Constants.YAHOO_DAILY_TARGETEST);

    this.faCurrentTextList = lcText.split(" ");
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected String replaceSpacing(final String tcText, final String tcReplace)
  {
    // Parentheses are special characters for regex.
    final String lcReplace = tcReplace.replace("(", "\\(").replace(")", "\\)");
    final String lcText = tcText.replaceAll(lcReplace, this.getStringWithMarker(tcReplace));

    return (lcText);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected String getStringWithMarker(final String tcReplace)
  {
    return (tcReplace.replaceAll(" ", Constants.YAHOO_DAILY_MARKER));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected int parseInt(final String tcMarker, final int tnLines)
  {
    int lnInteger;

    final String lcHTML = this.cleanForNumber(this.getHTML(tcMarker, tnLines));

    try
    {
      lnInteger = Integer.parseInt(lcHTML);
    }
    catch (final NumberFormatException loErr)
    {
      lnInteger = 0;
    }

    return (lnInteger);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected double parseDouble(final String tcMarker, final int tnLines)
  {
    double lnDouble;

    final String lcHTML = this.cleanForNumber(this.getHTML(tcMarker, tnLines));

    try
    {
      lnDouble = Double.parseDouble(lcHTML);
    }
    catch (final NumberFormatException loErr)
    {
      lnDouble = 0;
    }

    return (lnDouble);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected double parseDouble(final Document toDoc, final String tcSelect)
  {
    double lnDouble;

    final String lcHTML = this.cleanForNumber(this.getHTML(toDoc, tcSelect));

    try
    {
      lnDouble = Double.parseDouble(lcHTML);
    }
    catch (final NumberFormatException loErr)
    {
      lnDouble = 0;
    }

    return (lnDouble);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected String cleanForNumber(final String tcNumber)
  {

    String lcNumber = tcNumber.replaceAll(",", "");
    final int lnPos = lcNumber.indexOf("x");
    if (lnPos >= 0)
    {
      lcNumber = lcNumber.substring(0, lnPos).trim();
    }

    return (lcNumber);
  }

  // ---------------------------------------------------------------------------
  protected String getHTML(final Document toDoc, final String tcSelect)
  {
    String lcHTML;

    try
    {
      lcHTML = toDoc.select(tcSelect).first().html();
    }
    catch (final Exception loErr)
    {
      lcHTML = "";
    }

    // Get rid of all tags and any quotes.
    lcHTML = lcHTML.replaceAll("<[^>]*>", "").replaceAll("\"", "");

    lcHTML = lcHTML.replaceAll("undefined", "Unk");

    return (lcHTML);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected String getHTML(final String tcMarker, final int tnLines)
  {
    final StringBuilder loHTML = new StringBuilder();
    final String lcMarker = this.getStringWithMarker(tcMarker);

    final int lnLength = this.faCurrentTextList.length;
    for (int i = 0; i < lnLength; ++i)
    {
      if (this.faCurrentTextList[i].contains(lcMarker))
      {
        for (int x = 1; (x <= tnLines) && ((i + x) < lnLength); ++x)
        {
          loHTML.append(this.faCurrentTextList[i + x]).append(" ");
        }
        break;
      }
    }

    final String lcHTML = loHTML.toString().replaceAll("undefined", "Unk");

    return (lcHTML.trim());
  }

  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

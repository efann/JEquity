/*
 * JEquity
 * Copyright(c) 2008-2018, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.dao;

import com.beowurks.jequity.utility.Misc;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.StringReader;
import java.util.Date;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class XMLTextReader
{
  public static final XMLTextReader INSTANCE = new XMLTextReader();

  private Document foDoc = null;
  private Node foRootNode = null;

  // ---------------------------------------------------------------------------------------------------------------------
  private XMLTextReader()
  {
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // By the way, this initializes a document with a String, NOT a file name.
  public boolean initializeXMLDocument(final String tcXMLString)
  {
    boolean llOkay = true;

    try
    {
      final DocumentBuilder loDB = DocumentBuilderFactory.newInstance().newDocumentBuilder();

      final InputSource loInputSource = new InputSource(new StringReader(tcXMLString));

      this.foDoc = loDB.parse(loInputSource);
      this.foRootNode = this.foDoc.getFirstChild();
    }
    catch (final Exception loErr)
    {
      llOkay = false;
      Misc.errorMessage("There was an error in parsing the XML document.\n\n" + loErr.getMessage());
    }

    return (llOkay);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public boolean initializeXMLDocument(final File toFile)
  {
    boolean llOkay = true;

    try
    {
      final DocumentBuilder loDB = DocumentBuilderFactory.newInstance().newDocumentBuilder();

      this.foDoc = loDB.parse(toFile);
      this.foRootNode = this.foDoc.getFirstChild();
    }
    catch (final Exception loErr)
    {
      this.foDoc = null;
      this.foRootNode = null;

      llOkay = false;
      Misc.errorMessage("There was an error in parsing the XML document.\n\n" + loErr.getMessage());
    }

    return (llOkay);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Node getRootNode()
  {
    return (this.foRootNode);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Document getDocument()
  {
    return (this.foDoc);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Element findFirstElement(final String tcElement)
  {
    final NodeList loList = this.foDoc.getElementsByTagName(tcElement);
    if (loList.getLength() == 0)
    {
      return (null);
    }

    return ((Element) loList.item(0));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getString(final String tcElement, final String tcDefault)
  {
    final Node loNode = this.findFirstElement(tcElement);
    if (loNode == null)
    {
      return (tcDefault);
    }

    final Node loFirstChild = loNode.getFirstChild();

    return ((loFirstChild == null) ? tcDefault : loFirstChild.getNodeValue());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public boolean getBoolean(final String tcElement, final boolean tlDefault)
  {
    final String lcValue = this.getString(tcElement, "");

    boolean llValue;
    try
    {
      llValue = Boolean.parseBoolean(lcValue);
    }
    catch (final Exception loErr)
    {
      llValue = tlDefault;
    }

    return (llValue);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public int getInteger(final String tcElement, final int tnDefault)
  {
    final String lcValue = this.getString(tcElement, "");

    int lnValue;
    try
    {
      lnValue = Integer.parseInt(lcValue);
    }
    catch (final Exception loErr)
    {
      lnValue = tnDefault;
    }

    return (lnValue);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public long getLong(final String tcElement, final long tnDefault)
  {
    final String lcValue = this.getString(tcElement, "");

    long lnValue;
    try
    {
      lnValue = Long.parseLong(lcValue);
    }
    catch (final Exception loErr)
    {
      lnValue = tnDefault;
    }

    return (lnValue);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public double getDouble(final String tcElement, final double tnDefault)
  {
    final String lcValue = this.getString(tcElement, "");

    double lnValue;
    try
    {
      lnValue = Double.parseDouble(lcValue);
    }
    catch (final Exception loErr)
    {
      lnValue = tnDefault;
    }

    return (lnValue);

  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Date getDate(final String tcElement, final long tnDefault)
  {
    final String lcValue = this.getString(tcElement, "");

    long lnValue;
    try
    {
      lnValue = Long.parseLong(lcValue);
    }
    catch (final Exception loErr)
    {
      lnValue = tnDefault;
    }

    return (new Date(lnValue));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getAttributeString(final Element toElement, final String tcAttribute, final String tcDefault)
  {
    final String lcValue = toElement.getAttribute(tcAttribute);
    if (lcValue.isEmpty())
    {
      return (tcDefault);
    }

    return (lcValue);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public int getAttributeInteger(final Element toElement, final String tcAttribute, final int tnDefault)
  {
    final String lcValue = this.getAttributeString(toElement, tcAttribute, "");

    int lnValue;
    try
    {
      lnValue = Integer.parseInt(lcValue);
    }
    catch (final Exception loErr)
    {
      lnValue = tnDefault;
    }

    return (lnValue);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public long getAttributeLong(final Element toElement, final String tcAttribute, final long tnDefault)
  {
    final String lcValue = this.getAttributeString(toElement, tcAttribute, "");

    long lnValue;
    try
    {
      lnValue = Long.parseLong(lcValue);
    }
    catch (final Exception loErr)
    {
      lnValue = tnDefault;
    }

    return (lnValue);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public double getAttributeDouble(final Element toElement, final String tcAttribute, final double tnDefault)
  {
    final String lcValue = this.getAttributeString(toElement, tcAttribute, "");

    double lnValue;
    try
    {
      lnValue = Double.parseDouble(lcValue);
    }
    catch (final Exception loErr)
    {
      lnValue = tnDefault;
    }

    return (lnValue);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Date getAttributeDate(final Element toElement, final String tcAttribute, final long tnDefault)
  {
    final String lcValue = this.getAttributeString(toElement, tcAttribute, "");

    long lnValue;
    try
    {
      lnValue = Long.parseLong(lcValue);
    }
    catch (final Exception loErr)
    {
      lnValue = tnDefault;
    }

    return (new Date(lnValue));
  }
  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

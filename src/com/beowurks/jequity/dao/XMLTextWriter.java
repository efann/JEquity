/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.dao;

import com.beowurks.jequity.utility.Misc;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.Date;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class XMLTextWriter
{
  public static final XMLTextWriter INSTANCE = new XMLTextWriter();

  private Document foDoc = null;
  private Element foRoot = null;

  // ---------------------------------------------------------------------------------------------------------------------
  private XMLTextWriter()
  {
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Document initializeXMLDocument()
  {
    try
    {
      final DocumentBuilder loDB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      this.foDoc = loDB.newDocument();
    }
    catch (final Exception loErr)
    {
      this.foDoc = null;
      Misc.errorMessage("There was an error in initializing the XML document.\n\n" + loErr.getMessage());
    }

    return (this.foDoc);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Node appendNodeToRoot(final String tcElement, final boolean tlValue, final Object[][] taAttributes)
  {
    return (this.appendNodeToRoot(tcElement, Boolean.toString(tlValue), taAttributes));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Node appendNodeToRoot(final String tcElement, final Date tdValue, final Object[][] taAttributes)
  {
    return (this.appendNodeToRoot(tcElement, tdValue.getTime(), taAttributes));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Node appendNodeToRoot(final String tcElement, final double tnValue, final Object[][] taAttributes)
  {
    return (this.appendNodeToRoot(tcElement, Double.toString(tnValue), taAttributes));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Node appendNodeToRoot(final String tcElement, final int tnValue, final Object[][] taAttributes)
  {
    return (this.appendNodeToRoot(tcElement, Integer.toString(tnValue), taAttributes));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Node appendNodeToRoot(final String tcElement, final long tnValue, final Object[][] taAttributes)
  {
    return (this.appendNodeToRoot(tcElement, Long.toString(tnValue), taAttributes));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Node appendNodeToRoot(final String tcElement, final String tcValue, final Object[][] taAttributes)
  {
    Node loNode = null;

    try
    {
      final Element loLevel = this.foDoc.createElement(tcElement);
      if (tcValue != null)
      {
        final Text loTextData = this.foDoc.createTextNode(tcValue);
        loLevel.appendChild(loTextData);
      }

      if (taAttributes != null)
      {
        for (final Object[] laAttribute : taAttributes)
        {
          if (laAttribute.length != 2)
          {
            throw new Exception("Attribute array should have 2 columns only when appending to a node.");
          }

          loLevel.setAttribute(laAttribute[0].toString(), laAttribute[1].toString());
        }
      }

      loNode = this.foRoot.appendChild(loLevel);
    }
    catch (final Exception loErr)
    {
      loNode = null;
      Misc.errorMessage(
        "There was an error in appending node to the root.\nREMEMBER: no spaces, etc in Tag names.\n\n"
          + loErr.getMessage());
    }

    return (loNode);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Node appendToNode(final Node toNode, final String tcElement, final double tnValue,
                           final Object[][] taAttributes)
  {
    return (this.appendToNode(toNode, tcElement, Double.toString(tnValue), taAttributes));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Node appendToNode(final Node toNode, final String tcElement, final int tnValue, final Object[][] taAttributes)
  {
    return (this.appendToNode(toNode, tcElement, Integer.toString(tnValue), taAttributes));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Node appendToNode(final Node toNode, final String tcElement, final long tnValue, final Object[][] taAttributes)
  {
    return (this.appendToNode(toNode, tcElement, Long.toString(tnValue), taAttributes));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Node appendToNode(final Node toNode, final String tcElement, final String tcValue,
                           final Object[][] taAttributes)
  {
    Node loNode = null;

    try
    {
      final Element loLevel = this.foDoc.createElement(tcElement);
      if (tcValue != null)
      {
        final Text loTextData = this.foDoc.createTextNode(tcValue);
        loLevel.appendChild(loTextData);
      }

      if (taAttributes != null)
      {
        for (final Object[] laAttribute : taAttributes)
        {
          if (laAttribute.length != 2)
          {
            throw new Exception("Attribute array should have 2 columns only when appending to a node.");
          }

          loLevel.setAttribute(laAttribute[0].toString(), laAttribute[1].toString());
        }
      }

      loNode = toNode.appendChild(loLevel);
    }
    catch (final Exception loErr)
    {
      loNode = null;
      Misc.errorMessage(
        "There was an error in appending node to another node.\nREMEMBER: no spaces, etc in Tag names.\n\n"
          + loErr.getMessage());
    }

    return (loNode);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Node createRootNode(final String tcRootName, final Object[][] taAttributes)
  {
    try
    {
      this.foRoot = this.foDoc.createElement(tcRootName);

      if (taAttributes != null)
      {
        for (final Object[] laAttribute : taAttributes)
        {
          if (laAttribute.length != 2)
          {
            throw new Exception("Attribute array should have 2 columns only when appending to a node.");
          }

          this.foRoot.setAttribute(laAttribute[0].toString(), laAttribute[1].toString());
        }
      }

      this.foDoc.appendChild(this.foRoot);
    }
    catch (final Exception loErr)
    {
      this.foRoot = null;
      Misc.errorMessage(
        "There was an error in creating the Root Node.\nREMEMBER: no spaces, etc in Tag names.\n\n"
          + loErr.getMessage());
    }

    return (this.foRoot);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String generateXMLString(final int tnIndent)
  {
    final StringWriter loStringWriter = new StringWriter();

    try
    {
      final TransformerFactory loFactory = TransformerFactory.newInstance();
      // JRE 1.5 did not indent the XML output, whereas 1.4 did. I got
      // setAttribute
      // fix from the following:
      // http://forum.java.sun.com/thread.jspa?threadID=562510&start=15&tstart=15
      // Sometimes this creates problems
      // http://forums.sun.com/thread.jspa?forumID=34&threadID=562510
      // If, for example, the xalan.jar is included with your project (e.g.,
      // JasperReports)
      // then you will get the "Not supported: indent-number" error.
      try
      {
        loFactory.setAttribute("indent-number", Integer.valueOf(tnIndent));
      }
      catch (final IllegalArgumentException ignored)
      {
      }

      final Transformer loTransformer = loFactory.newTransformer();
      loTransformer.setOutputProperty(OutputKeys.METHOD, "xml");
      loTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      loTransformer.setOutputProperty(OutputKeys.INDENT, "yes");

      final DOMSource loDOMSource = new DOMSource(this.foDoc);
      final StreamResult loStreamResult = new StreamResult(loStringWriter);
      // this line actually does the XML generation
      loTransformer.transform(loDOMSource, loStreamResult);
    }
    catch (final TransformerConfigurationException loErr)
    {
      Misc.errorMessage("There was a Transformer Configuration Exception in generating an XML string.\n\n"
        + loErr.getMessage());
    }
    catch (final TransformerFactoryConfigurationError loErr)
    {
      Misc.errorMessage("There was a Transformer Factory Configuration Error in generating an XML string.\n\n"
        + loErr.getMessage());
    }
    catch (final TransformerException loErr)
    {
      Misc.errorMessage("There was a Transformer Exception in generating an XML string.\n\n" + loErr.getMessage());
    }

    return (loStringWriter.toString());
  }


  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

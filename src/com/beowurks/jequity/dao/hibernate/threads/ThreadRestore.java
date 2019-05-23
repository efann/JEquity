/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */
package com.beowurks.jequity.dao.hibernate.threads;

import com.beowurks.jequity.dao.XMLTextReader;
import com.beowurks.jequity.dao.hibernate.FinancialEntity;
import com.beowurks.jequity.dao.hibernate.GroupEntity;
import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
public class ThreadRestore extends ThreadBase implements Runnable
{

  public static final ThreadRestore INSTANCE = new ThreadRestore();

  private final HashMap<String, GroupEntity> foGroupMap = new HashMap<>();
  private File foXMLFile = null;

  // -----------------------------------------------------------------------------
  private ThreadRestore()
  {
    super();
  }

  // -----------------------------------------------------------------------------
  @Override
  public void run()
  {
    this.fcErrorMessage = this.restoreFromXML();

    // No matter the outcome, refresh all of the data.
    Main.getController().refreshAllComponents(true);

    if (this.fcErrorMessage.isEmpty())
    {
      this.flSuccess = true;
      if (this.flDisplayDialogMessage)
      {
        Misc.infoMessage(String.format("%s has been imported into %s", this.foXMLFile.getPath(), Main.getApplicationName()));
      }
    }
    else
    {
      this.flSuccess = false;
      if (this.flDisplayDialogMessage)
      {
        Misc.errorMessage(this.fcErrorMessage);
      }
    }

  }

  // -----------------------------------------------------------------------------
  public boolean start(final File toXMLFile, final boolean tlDisplayDialogMessage)
  {
    if ((this.foThread != null) && (this.foThread.isAlive()))
    {
      if (tlDisplayDialogMessage)
      {
        Misc.errorMessage("Restoring is currently in progress. . . .");
      }
      return (false);
    }

    // Set these variables after Thread check just in case the Thread was already running:
    // you don't want to alter variables in the middle of a run.
    this.flDisplayDialogMessage = tlDisplayDialogMessage;
    this.foXMLFile = toXMLFile;
    this.foGroupMap.clear();

    this.foThread = new Thread(this);
    this.foThread.setPriority(Thread.NORM_PRIORITY);
    this.foThread.start();

    return (true);
  }

  // -----------------------------------------------------------------------------
  private String restoreFromXML()
  {
    final XMLTextReader loReader = XMLTextReader.INSTANCE;
    loReader.initializeXMLDocument(this.foXMLFile, true);

    final Document loDocument = loReader.getDocument();

    if (loDocument == null)
    {
      return (String.format("%s does not appear to be a valid XML file.", this.foXMLFile.getAbsoluteFile()));
    }

    Misc.setStatusText("Creating groups. . . .");
    if (!this.generateGroupEntityHashMap(loDocument))
    {
      return (String.format("With %s, we are unable to append unique Group records.\n\nTry renaming your existing Groups and then run Restore again.", this.foXMLFile.getAbsoluteFile()));
    }

    final HibernateUtil loHibernate = HibernateUtil.INSTANCE;
    final Session loSession = loHibernate.getSession();

    final StringBuilder loMessage = new StringBuilder();

    // Hibernate works well; however, it is not efficient for batch insert operations.
    // Even with StatelessSession and using inserts with SQL rather than saving
    // an entity, Hibernate would plod along at about 0.75 seconds per insert.
    // So From http://viralpatel.net/blogs/batch-insert-in-java-jdbc/ . . . .
    loSession.doWork(toConnection ->
    {
      toConnection.setAutoCommit(true);

      final NodeList loRecordsList = loDocument.getElementsByTagName(Constants.XML_RECORDS_LABEL);
      final int lnRows = loRecordsList.getLength();

      // Must be initialized each time.
      Misc.setStatusText("Restoring. . . .", 0.0);

      PreparedStatement loPreparedStatement = null;
      try
      {
        final String lcInsertSQL = String.format("INSERT INTO %s (groupid, description, symbol, account, type, category, shares, price, valuationdate, retirement, comments) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", loHibernate.getTableFinancial());
        loPreparedStatement = toConnection.prepareStatement(lcInsertSQL);
        for (int lnRow = 0; lnRow < lnRows; ++lnRow)
        {
          final FinancialEntity loEntity = new FinancialEntity();

          final Element loElement = (Element) loRecordsList.item(lnRow);
          final NodeList loColList = loElement.getChildNodes();
          final int lnCols = loColList.getLength();

          String lcGroupDescription = "";

          for (int lnCol = 0; lnCol < lnCols; ++lnCol)
          {
            final Node loField = loColList.item(lnCol);

            if (loField.getNodeType() != Node.ELEMENT_NODE)
            {
              continue;
            }

            // Empty values get skipped.
            if (!loField.hasChildNodes())
            {
              continue;
            }

            final String lcField = loField.getNodeName();
            final String lcValue = loField.getFirstChild().getNodeValue();

            if (lcField.compareTo(Constants.XML_GROUP_DESCRIPTION) == 0)
            {
              lcGroupDescription = lcValue;
              loEntity.setGroupID(ThreadRestore.this.foGroupMap.get(lcValue).getGroupID());
            }
            else if (lcField.compareTo(Constants.XML_ACCOUNT) == 0)
            {
              loEntity.setAccount(lcValue);
            }
            else if (lcField.compareTo(Constants.XML_CATEGORY) == 0)
            {
              loEntity.setCategory(lcValue);
            }
            else if (lcField.compareTo(Constants.XML_COMMENTS) == 0)
            {
              loEntity.setComments(lcValue);
            }
            else if (lcField.compareTo(Constants.XML_DESCRIPTION) == 0)
            {
              loEntity.setDescription(lcValue);
            }
            else if (lcField.compareTo(Constants.XML_PRICE) == 0)
            {
              loEntity.setPrice(Double.parseDouble(lcValue));
            }
            else if (lcField.compareTo(Constants.XML_RETIREMENT) == 0)
            {
              loEntity.setRetirement(Boolean.parseBoolean(lcValue));
            }
            else if (lcField.compareTo(Constants.XML_SHARES) == 0)
            {
              loEntity.setShares(Double.parseDouble(lcValue));
            }
            else if (lcField.compareTo(Constants.XML_SYMBOL) == 0)
            {
              loEntity.setSymbol(lcValue);
            }
            else if (lcField.compareTo(Constants.XML_TYPE) == 0)
            {
              loEntity.setType(lcValue);
            }
            else if (lcField.compareTo(Constants.XML_VALUATIONDATE) == 0)
            {
              loEntity.setValuationDate(java.sql.Date.valueOf(lcValue));
            }
          }

          Misc.setStatusText(String.format("Restoring %s: %s. . . .", lcGroupDescription, loEntity.getDescription()), lnRow / lnRows);

          loPreparedStatement.setInt(1, loEntity.getGroupID());
          loPreparedStatement.setString(2, loEntity.getDescription());
          loPreparedStatement.setString(3, loEntity.getSymbol());
          loPreparedStatement.setString(4, loEntity.getAccount());
          loPreparedStatement.setString(5, loEntity.getType());
          loPreparedStatement.setString(6, loEntity.getCategory());
          loPreparedStatement.setDouble(7, loEntity.getShares());
          loPreparedStatement.setDouble(8, loEntity.getPrice());
          loPreparedStatement.setDate(9, loEntity.getValuationDate());
          loPreparedStatement.setBoolean(10, loEntity.getRetirement());
          loPreparedStatement.setString(11, loEntity.getComments());

          loPreparedStatement.addBatch();

          if ((lnRow % 30) == 0)
          {
            loPreparedStatement.executeBatch();
          }
        }

        loPreparedStatement.executeBatch();
        loPreparedStatement.close();
      }
      catch (final SQLException | DOMException | NumberFormatException loErr)
      {
        loMessage.append(loErr.getMessage());
      }
      finally
      {
        if (loPreparedStatement != null)
        {
          loPreparedStatement.close();
        }
      }

      Misc.setStatusText("Restoration complete.", 0.0);
    });

    loSession.close();

    return (loMessage.toString());
  }

  // -----------------------------------------------------------------------------
  private boolean generateGroupEntityHashMap(final Document toDocument)
  {
    final NodeList loRecordsList = toDocument.getElementsByTagName(Constants.XML_RECORDS_LABEL);
    final int lnRows = loRecordsList.getLength();

    for (int lnRow = 0; lnRow < lnRows; ++lnRow)
    {
      final Element loElement = (Element) loRecordsList.item(lnRow);
      final NodeList loColList = loElement.getChildNodes();
      final int lnCols = loColList.getLength();

      boolean llFound = false;
      for (int lnCol = 0; (lnCol < lnCols) && !llFound; ++lnCol)
      {
        final Node loField = loColList.item(lnCol);

        if (loField.getNodeType() != Node.ELEMENT_NODE)
        {
          continue;
        }

        // If no values in the node.
        if (!loField.hasChildNodes())
        {
          continue;
        }

        final String lcField = loField.getNodeName();
        final String lcValue = loField.getFirstChild().getNodeValue();

        if (lcField.compareTo(Constants.XML_GROUP_DESCRIPTION) == 0)
        {
          if (!this.foGroupMap.containsKey(lcValue))
          {
            if (!this.createGroupEntity(lcValue))
            {
              return (false);
            }
          }
          llFound = true;
        }

      }
    }

    return (true);
  }

  // -----------------------------------------------------------------------------
  private boolean createGroupEntity(final String tcValue)
  {
    final HibernateUtil loHibernate = HibernateUtil.INSTANCE;
    final Session loSession = loHibernate.getSession();

    final String lcSQL = String.format("SELECT * FROM %s g WHERE g.description = :description", loHibernate.getTableGroup());

    NativeQuery loQuery;
    String lcDescription = tcValue;

    loQuery = loSession.createNativeQuery(lcSQL).setParameter("description", lcDescription);

    // Description value already exists.
    if (!loQuery.list().isEmpty())
    {
      final String lcTimeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.S").format(new java.util.Date());
      lcDescription = String.format("%s (%s)", tcValue.trim(), lcTimeStamp);

      loQuery = loSession.createNativeQuery(lcSQL).setParameter("description", lcDescription);

      // Description value still exists so give up.
      if (!loQuery.list().isEmpty())
      {
        lcDescription = "";
      }
    }

    loSession.close();

    if (!lcDescription.isEmpty())
    {
      final GroupEntity loEntity = new GroupEntity();

      loEntity.setDescription(lcDescription);
      if (loHibernate.insertRow(loEntity))
      {
        this.foGroupMap.put(tcValue, loEntity);
      }
    }

    return (!lcDescription.isEmpty());
  }

  // -----------------------------------------------------------------------------
}
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------

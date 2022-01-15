/*
 * JEquity
 * Copyright(c) 2008-2022, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */
package com.beowurks.jequity.dao.hibernate;

import com.beowurks.jequity.dao.XMLTextWriter;
import com.beowurks.jequity.dao.migration.FlywayMigration;
import com.beowurks.jequity.utility.AppProperties;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import org.apache.commons.io.FileUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.NativeQuery;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;

/**
 * Hibernate Utility class with a convenient method to get Session Factory object.
 *
 * @author efann
 */
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
public final class HibernateUtil
{

  // Here's a discussion of when INSTANCE will be initialized:
  // http://stackoverflow.com/questions/13724230/singleton-and-public-static-variable-java
  // Accordingly, it should be initialized when first accessed.
  public static final HibernateUtil INSTANCE = new HibernateUtil();

  private static final int TABLES_FINANCIAL = 0;
  private static final int TABLES_GROUP = 1;
  private static final int TABLES_SYMBOL = 2;

  private final HashMap<Integer, String> foTableList = new HashMap<>();

  private SessionFactory foSessionFactory = null;
  private boolean flCaseSensitive;
  private char fcQuoteOpen;
  private char fcQuoteClose;

  private WhichDatabase foWhichDatabase = null;

  private Integer fnGroupID = Constants.UNINITIALIZED;
  private Integer fnFinancialID = Constants.UNINITIALIZED;

  private boolean flInitializationSuccess = false;

  // -----------------------------------------------------------------------------
  private HibernateUtil()
  {
    if (AppProperties.INSTANCE.isSuccessfullyRead())
    {
      if (FlywayMigration.INSTANCE.migrate())
      {
        if (this.initializeHibernate())
        {
          this.setupVariables();
          this.flInitializationSuccess = true;
        }
      }

    }
  }

  // -----------------------------------------------------------------------------
  // Though private, I would like to ensure that this routine will not interleave,
  // that this.foSessionFactory can be null only once.
  synchronized private boolean initializeHibernate()
  {
    Misc.setStatusText("Initializing Hibernate. . . .");

    boolean llOkay = true;
    final AppProperties loApp = AppProperties.INSTANCE;
    if (this.foSessionFactory == null)
    {
      final String lcDescription = loApp.getConnectionRDBMS_Description();
      Misc.setStatusText(String.format("Initializing %s. . . .", lcDescription));

      try
      {
        String lcCfgFile = "";
        final int lnKey = loApp.getConnectionRDBMS_Key();

        switch (lnKey)
        {
          case Constants.DRIVER_KEY_DERBY:
            lcCfgFile = "apachederby";
            break;

          case Constants.DRIVER_KEY_MYSQL5_PLUS:
            lcCfgFile = "mysql5";
            break;

          case Constants.DRIVER_KEY_POSTGRESQL9_PLUS:
            lcCfgFile = "postgresql91";
            break;
        }

        // Should never happen but check anyway.
        if (lcCfgFile.isEmpty())
        {
          Misc.errorMessage("Unable to determine the configuration file for Hibernate.");
          return (false);
        }

        final Configuration loConfiguration = new Configuration();
        loConfiguration.configure(String.format("/com/beowurks/jequity/dao/hibernate/%s.cfg.xml", lcCfgFile));

        Misc.setStatusText(String.format("Reading config file for %s. . . .", lcDescription));

        final String lcConnectionURL = loApp.getConnectionURL();
        // Should never happen but check anyway.
        if (lcConnectionURL.isEmpty())
        {
          Misc.errorMessage("The connection string is empty.");
          return (false);
        }

        loConfiguration.setProperty("hibernate.connection.url", lcConnectionURL);
        loConfiguration.setProperty("hibernate.connection.username", loApp.getConnectionUser());
        loConfiguration.setProperty("hibernate.connection.password", loApp.getConnectionPassword());

        Misc.setStatusText(String.format("Opening session for %s. . . .", lcDescription));

        // Well this was simpler than the old way of using StandardServiceRegistryBuilder, etc.
        this.foSessionFactory = loConfiguration.buildSessionFactory();
      }
      catch (final HibernateException loErr)
      {
        llOkay = false;
        Misc.showStackTraceInMessage(loErr, "Connection Error");
      }
    }

    return (llOkay);
  }

  // -----------------------------------------------------------------------------
  public boolean initializeSuccess()
  {
    return (this.flInitializationSuccess);
  }

  // -----------------------------------------------------------------------------
  private void setupVariables()
  {
    this.foTableList.put(HibernateUtil.TABLES_FINANCIAL, "Financial");
    this.foTableList.put(HibernateUtil.TABLES_GROUP, "Group");
    this.foTableList.put(HibernateUtil.TABLES_SYMBOL, "Symbol");

    final Session loSession = this.getSession();
    loSession.doWork(toConnection ->
    {
      HibernateUtil.this.flCaseSensitive = toConnection.getMetaData().supportsMixedCaseQuotedIdentifiers();

      HibernateUtil.this.foWhichDatabase = new WhichDatabase(toConnection);
    });

    loSession.close();

    this.fcQuoteOpen = this.getDialect().openQuote();
    this.fcQuoteClose = this.getDialect().closeQuote();
  }

  // -----------------------------------------------------------------------------
  public String getTableFinancial()
  {
    return (this.buildTableName(HibernateUtil.TABLES_FINANCIAL));
  }

  // -----------------------------------------------------------------------------
  public String getTableGroup()
  {
    return (this.buildTableName(HibernateUtil.TABLES_GROUP));
  }

  // -----------------------------------------------------------------------------
  public String getTableSymbol()
  {
    return (this.buildTableName(HibernateUtil.TABLES_SYMBOL));
  }

  // -----------------------------------------------------------------------------
  // The default schema is set in the configuration file.
  // This way, Hibernate will default to Constants.FLYWAY_JEQUITY_SCHEMA
  // except in the case of MySQL where the schema is the database.
  // And in the entity declarations, I don't have to declare a schema.
  private String buildTableName(final Integer toTableKey)
  {
    final String lcTable;

    if (this.isMySQL())
    {
      lcTable = String.format("%s%s%s",
        this.fcQuoteOpen, this.foTableList.get(toTableKey), this.fcQuoteClose);
    }
    else
    {
      lcTable = String.format("%s%s%s.%s%s%s",
        this.fcQuoteOpen, Constants.FLYWAY_JEQUITY_SCHEMA, this.fcQuoteClose,
        this.fcQuoteOpen, this.foTableList.get(toTableKey), this.fcQuoteClose);
    }

    return (lcTable);
  }

  // -----------------------------------------------------------------------------
  public Session getSession() throws HibernateException
  {
    return (this.foSessionFactory.openSession());
  }

  // -----------------------------------------------------------------------------
  public StatelessSession getStatelessSession() throws HibernateException
  {
    return (this.foSessionFactory.openStatelessSession());
  }

  // -----------------------------------------------------------------------------
  public boolean insertRow(final Object toEntity)
  {
    final Session loSession = this.getSession();

    boolean llOkay = false;

    Transaction loTransaction = null;
    try
    {
      loTransaction = loSession.beginTransaction();
      loSession.save(toEntity);
      loTransaction.commit();

      llOkay = true;
    }
    catch (final Exception loErr)
    {
      Misc.showStackTraceInMessage(loErr, "Insert Row Error");

      try
      {
        if (loTransaction != null)
        {
          loTransaction.rollback();
        }
      }
      catch (final RuntimeException loRuntimeErr)
      {
        Misc.showStackTraceInMessage(loRuntimeErr, "Rollback Error");
      }
    }

    loSession.close();

    return (llOkay);
  }

  // -----------------------------------------------------------------------------
  public boolean updateRow(final Object toEntity)
  {
    final Session loSession = this.getSession();

    boolean llOkay = false;

    Transaction loTransaction = null;
    try
    {
      loTransaction = loSession.beginTransaction();
      loSession.update(toEntity);
      loTransaction.commit();

      llOkay = true;
    }
    catch (final Exception loErr)
    {
      Misc.showStackTraceInMessage(loErr, "Update Row Error");

      try
      {
        if (loTransaction != null)
        {
          loTransaction.rollback();
        }
      }
      catch (final RuntimeException loRuntimeErr)
      {
        Misc.showStackTraceInMessage(loRuntimeErr, "Rollback Error");
      }
    }

    loSession.close();

    return (llOkay);
  }

  // -----------------------------------------------------------------------------
  public boolean removeRow(final Object toEntity)
  {
    final Session loSession = this.getSession();

    boolean llOkay = false;

    Transaction loTransaction = null;
    try
    {
      loTransaction = loSession.beginTransaction();

      if (toEntity instanceof GroupEntity)
      {
        final GroupEntity loGroupEntity = (GroupEntity) toEntity;

        // First remove the related financial records.

        // From http://phpzila.blogspot.com/2012/01/mysql-using-table-alias-in-delete.html
        // Otherwise, you get 'You have an error in your SQL syntax'
        // Also, it's not ANSI: https://stackoverflow.com/questions/11005209/why-cant-i-use-an-alias-in-a-delete-statement
        // Basically, do not use aliases in a DELETE statement.
        final String lcDelete = String.format("DELETE FROM %s WHERE groupid = :groupid", this.getTableFinancial());

        final NativeQuery loDelete = loSession.createNativeQuery(lcDelete)
          .setParameter("groupid", loGroupEntity.getGroupID());

        loDelete.executeUpdate();

        // Now remove the group record.
        loSession.delete(loGroupEntity);
      }
      else
      {
        loSession.delete(toEntity);
      }

      loTransaction.commit();

      llOkay = true;
    }
    catch (final Exception loErr)
    {
      try
      {
        if (loTransaction != null)
        {
          loTransaction.rollback();
        }
      }
      catch (final RuntimeException loRuntimeErr)
      {
        Misc.showStackTraceInMessage(loRuntimeErr, "Rollback Error");
      }

      Misc.showStackTraceInMessage(loErr, "Delete Row Error");
    }

    loSession.close();

    return (llOkay);
  }

  // ---------------------------------------------------------------------------
  private Dialect getDialect()
  {
    final Dialect loDialect = ((SessionFactoryImplementor) this.foSessionFactory).getJdbcServices().getDialect();

    return (loDialect);
  }

  // -----------------------------------------------------------------------------
  public void setGroupID(final Integer tnGroupID)
  {
    this.fnGroupID = tnGroupID;
  }

  // -----------------------------------------------------------------------------
  public Integer getGroupID()
  {
    return (this.fnGroupID);
  }

  // -----------------------------------------------------------------------------
  public void setFinancialID(final Integer tnFinancialID)
  {
    this.fnFinancialID = tnFinancialID;
  }

  // -----------------------------------------------------------------------------
  public Integer getFinancialID()
  {
    return (this.fnFinancialID);
  }

  // -----------------------------------------------------------------------------
  public boolean isMySQL()
  {
    return (this.foWhichDatabase.isMySQL());
  }

  // -----------------------------------------------------------------------------
  public boolean isApacheDerby()
  {
    return (this.foWhichDatabase.isApacheDerby());
  }

  // -----------------------------------------------------------------------------
  public boolean isPostgreSQL()
  {
    return (this.foWhichDatabase.isPostgreSQL());
  }

  // -----------------------------------------------------------------------------
  public void backupToXML(final File toXMLFile)
  {
    File loFile = toXMLFile;
    if (!loFile.getPath().endsWith(".xml"))
    {
      loFile = new File(loFile.getPath() + ".xml");
    }

    final Session loSession = this.getSession();

    final String lcSQL = String.format("SELECT {g.*}, {f.*} FROM %s g, %s f WHERE g.GROUPID = f.GROUPID ORDER BY g.GROUPID, f.DESCRIPTION ",
      this.getTableGroup(), this.getTableFinancial());

    final NativeQuery loQuery = loSession.createNativeQuery(lcSQL)
      .addEntity("g", GroupEntity.class)
      .addEntity("f", FinancialEntity.class);

    try
    {
      FileUtils.writeStringToFile(loFile, this.generateXMLString(loQuery.list()), Charset.defaultCharset());
    }
    catch (final IOException loErr)
    {
      Misc.showStackTraceInMessage(loErr, String.format("Error Saving %s", loFile.getPath()));
    }

    loSession.close();
  }

  // -----------------------------------------------------------------------------
  private String generateXMLString(final List<Object> toList)
  {
    final XMLTextWriter loTextWriter = XMLTextWriter.INSTANCE;
    loTextWriter.initializeXMLDocument();
    loTextWriter.createRootNode(Constants.XML_ROOT_LABEL, null);

    for (final Object loRow : toList)
    {
      final Object[] laEntities = (Object[]) loRow;
      final GroupEntity loGroupEntity = (GroupEntity) laEntities[0];
      final FinancialEntity loFinancialEntity = (FinancialEntity) laEntities[1];

      final Node loRecord = loTextWriter.appendNodeToRoot(Constants.XML_RECORDS_LABEL, (String) null, null);

      loTextWriter.appendToNode(loRecord, Constants.XML_GROUP_DESCRIPTION, loGroupEntity.getDescription(), null);
      loTextWriter.appendToNode(loRecord, Constants.XML_DESCRIPTION, loFinancialEntity.getDescription(), null);
      loTextWriter.appendToNode(loRecord, Constants.XML_ACCOUNT, loFinancialEntity.getAccount(), null);
      loTextWriter.appendToNode(loRecord, Constants.XML_CATEGORY, loFinancialEntity.getCategory(), null);
      loTextWriter.appendToNode(loRecord, Constants.XML_COMMENTS, loFinancialEntity.getComments(), null);
      loTextWriter.appendToNode(loRecord, Constants.XML_PRICE, loFinancialEntity.getPrice(), null);
      loTextWriter.appendToNode(loRecord, Constants.XML_SHARES, loFinancialEntity.getShares(), null);
      loTextWriter.appendToNode(loRecord, Constants.XML_SYMBOL, loFinancialEntity.getSymbol(), null);
      loTextWriter.appendToNode(loRecord, Constants.XML_TYPE, loFinancialEntity.getType(), null);
      loTextWriter.appendToNode(loRecord, Constants.XML_RETIREMENT, loFinancialEntity.getRetirement() ? Constants.XML_TRUE : Constants.XML_FALSE, null);
      loTextWriter.appendToNode(loRecord, Constants.XML_OWNERSHIP, loFinancialEntity.getOwnership(), null);
      loTextWriter.appendToNode(loRecord, Constants.XML_TAXSTATUS, loFinancialEntity.getTaxStatus(), null);
      loTextWriter.appendToNode(loRecord, Constants.XML_VALUATIONDATE, loFinancialEntity.getValuationDate().toString(), null);
    }

    return (loTextWriter.generateXMLString(2));
  }

  // -----------------------------------------------------------------------------
}
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------

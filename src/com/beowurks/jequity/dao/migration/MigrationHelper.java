/*
 * JEquity
 * Copyright(c) 2008-2022, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */
package com.beowurks.jequity.dao.migration;

import com.beowurks.jequity.dao.hibernate.WhichDatabase;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import org.flywaydb.core.api.migration.BaseJavaMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
abstract public class MigrationHelper extends BaseJavaMigration
{

  protected Connection foConnection = null;

  private static final String PRIMARY_AUTOINCREMENT_TAG = "<<primary_auto_increment>>";
  private static final String SCHEMA_TAG = "<<schema>>";
  private static final String LONG_VARCHAR_TAG = "<<long_varchar>>";

  private WhichDatabase foWhichDatabase = null;

  // -----------------------------------------------------------------------------
  protected void executeStatements(final String tcResource) throws SQLException
  {
    // From http://stackoverflow.com/questions/1921975/java-convert-package-name-to-path
    final String lcPath = this.getClass().getPackage().getName().replace(".", "/") + "/";

    final String[] laSQL = this.getSQLStatements(Misc.getURIFileContents(lcPath + tcResource));

    for (final String lcSQL : laSQL)
    {
      this.executeStatement(lcSQL);
    }
  }

  // -----------------------------------------------------------------------------
  protected void executeStatement(final String tcSQL) throws SQLException
  {
    // Uses automatic resource management as recommended by IntelliJ.
    // Because the PreparedStatement instance is declared in a try-with-resource statement,
    // it will be closed regardless of whether the try statement completes normally or abruptly.
    // From https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
    try (final PreparedStatement loStatement = this.foConnection.prepareStatement(tcSQL))
    {
      loStatement.execute();
    }
  }

  // -----------------------------------------------------------------------------
  private String[] getSQLStatements(final String tcSQL)
  {
    String[] laStatements = null;

    try
    {
      final String lcSQL = this.convertSQL(tcSQL);
      laStatements = lcSQL.split(";");
    }
    catch (final Exception ignored)
    {
    }

    return (laStatements);
  }

  // -----------------------------------------------------------------------------
  private String convertSQL(final String tcSQL)
  {
    final boolean llMySQL = this.isMySQL();

    String lcSQL = tcSQL;

    if (llMySQL)
    {
      // MySQL has the ` for quotes.
      lcSQL = Misc.replaceAll(lcSQL, "\"", "`").toString();
    }

    if (lcSQL.contains(MigrationHelper.SCHEMA_TAG))
    {
      // With MySQL, the schema is the database.
      lcSQL = Misc.replaceAll(lcSQL, MigrationHelper.SCHEMA_TAG, this.isMySQL() ? "" : "\"" + Constants.FLYWAY_JEQUITY_SCHEMA + "\".").toString();
    }

    if (lcSQL.contains(MigrationHelper.LONG_VARCHAR_TAG))
    {
      String lcReplace = "";
      if (this.isApacheDerby())
      {
        lcReplace = "LONG VARCHAR NOT NULL";
      }
      else if (llMySQL)
      {
        lcReplace = "LONG VARCHAR NOT NULL";
      }
      else if (this.isPostgreSQL())
      {
        lcReplace = "VARCHAR NOT NULL";
      }

      lcSQL = lcSQL.replace(MigrationHelper.LONG_VARCHAR_TAG, lcReplace);
    }

    if (lcSQL.contains(MigrationHelper.PRIMARY_AUTOINCREMENT_TAG))
    {
      String lcReplace = "";
      if (this.isApacheDerby())
      {
        lcReplace = "INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)";
      }
      else if (llMySQL)
      {
        lcReplace = "INT NOT NULL AUTO_INCREMENT";
      }
      else if (this.isPostgreSQL())
      {
        lcReplace = "serial NOT NULL";
      }

      lcSQL = lcSQL.replace(MigrationHelper.PRIMARY_AUTOINCREMENT_TAG, lcReplace);
    }

    return (lcSQL);
  }

  // -----------------------------------------------------------------------------
  protected boolean isMySQL()
  {
    if (this.foWhichDatabase == null)
    {
      this.foWhichDatabase = new WhichDatabase(this.foConnection);
    }

    return (this.foWhichDatabase.isMySQL());
  }

  // -----------------------------------------------------------------------------
  protected boolean isApacheDerby()
  {
    if (this.foWhichDatabase == null)
    {
      this.foWhichDatabase = new WhichDatabase(this.foConnection);
    }

    return (this.foWhichDatabase.isApacheDerby());
  }

  // -----------------------------------------------------------------------------
  protected boolean isPostgreSQL()
  {
    if (this.foWhichDatabase == null)
    {
      this.foWhichDatabase = new WhichDatabase(this.foConnection);
    }

    return (this.foWhichDatabase.isPostgreSQL());
  }
  // -----------------------------------------------------------------------------
}
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------

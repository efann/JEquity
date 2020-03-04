/*
 * JEquity
 * Copyright(c) 2008-2020, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */
package com.beowurks.jequity.dao.migration;

import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import org.flywaydb.core.api.migration.Context;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;

// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
public class V3_4__Move_Financial_Data extends MigrationHelper
{

  final static String SCHEMA_OLD = "JEquity";

  final static String GROUP_TABLE = String.format("\"%s\".\"%s\"", Constants.FLYWAY_JEQUITY_SCHEMA, "Group");
  final static String FINANCIAL_TABLE = String.format("\"%s\".\"%s\"", Constants.FLYWAY_JEQUITY_SCHEMA, "Financial");

  // -----------------------------------------------------------------------------
  @Override
  public void migrate(final Context toContext) throws Exception
  {
    Misc.setStatusText("Migration v3.4. . . .");

    this.foConnection = toContext.getConnection();

    // Only the original version which used Apache Derby
    // contains separate financial tables in the JEquity schema.
    if (!this.isApacheDerby())
    {
      return;
    }

    final String[] laTypeNames =
      {
        "TABLE"
      };

    final DatabaseMetaData loMetaData = this.foConnection.getMetaData();

    final ResultSet loResults = loMetaData.getTables(null, V3_4__Move_Financial_Data.SCHEMA_OLD, null, laTypeNames);
    while (loResults.next())
    {
      final String lcDescription = loResults.getString("TABLE_NAME");
      final String lcTableOld = String.format("\"%s\".\"%s\"", loResults.getString("TABLE_SCHEM"), lcDescription);

      final PreparedStatement loStatement = this.foConnection.prepareStatement("INSERT INTO " + V3_4__Move_Financial_Data.GROUP_TABLE + " (description) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
      loStatement.setString(1, lcDescription);
      loStatement.executeUpdate();

      final ResultSet loIDResultSet = loStatement.getGeneratedKeys();
      if (loIDResultSet.next())
      {
        final int lnID = loIDResultSet.getInt(1);

        boolean llIsRetirementChar = false;
        final ResultSet loResultSetRetirementColumn = this.foConnection.createStatement().executeQuery(String.format("SELECT retirement FROM %s", lcTableOld));
        if (loResultSetRetirementColumn != null)
        {
          final ResultSetMetaData loResultSetMetaData = loResultSetRetirementColumn.getMetaData();
          llIsRetirementChar = (loResultSetMetaData.getColumnType(1) == Types.CHAR);
        }

        // Now move entire table into the Financial table.
        final StringBuilder loSQL = new StringBuilder();
        loSQL.append(String.format("INSERT INTO %s ", V3_4__Move_Financial_Data.FINANCIAL_TABLE))
          .append("(groupid, description, symbol, account, type, category, shares, price, valuationdate, retirement, comments) ");

        if (llIsRetirementChar)
        {
          loSQL.append(String.format("SELECT %d,description, symbol, accountno, type, category, shares, price, valuationdate, CASE WHEN retirement='1' THEN TRUE ELSE FALSE END AS retirement, comments ", lnID));
        }
        else
        {
          loSQL.append(String.format("SELECT %d,description, symbol, accountno, type, category, shares, price, valuationdate, retirement, comments ", lnID));
        }

        loSQL.append(String.format("FROM %s", lcTableOld));

        this.executeStatement(loSQL.toString());
      }
    }

  }
  // -----------------------------------------------------------------------------

}
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------

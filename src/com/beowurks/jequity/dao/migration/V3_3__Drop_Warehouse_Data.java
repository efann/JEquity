/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */
package com.beowurks.jequity.dao.migration;

import com.beowurks.jequity.utility.Misc;
import org.flywaydb.core.api.migration.Context;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
public class V3_3__Drop_Warehouse_Data extends MigrationHelper
{

  // -----------------------------------------------------------------------------
  @Override
  public void migrate(final Context toContext) throws Exception
  {
    Misc.setStatusText("Migration v3.3. . . .");

    this.foConnection = toContext.getConnection();

    // Only the original version which used Apache Derby
    // contains the JEquityStocks schema.
    if (!this.isApacheDerby())
    {
      return;
    }

    final String[] laTypeNames =
      {
        "TABLE"
      };

    ResultSet loResults = null;
    final String lcSchema = "JEquityStocks";
    final DatabaseMetaData loMetaData = this.foConnection.getMetaData();

    loResults = loMetaData.getTables(null, lcSchema, null, laTypeNames);
    while (loResults.next())
    {
      final String lcTable = String.format("\"%s\".\"%s\"", loResults.getString("TABLE_SCHEM"), loResults.getString("TABLE_NAME"));
      this.executeStatement("DROP TABLE " + lcTable);
    }

    loResults = loMetaData.getSchemas();
    while (loResults.next())
    {
      if (loResults.getString("TABLE_SCHEM").equals(lcSchema))
      {
        this.executeStatement(String.format("DROP SCHEMA \"%s\" RESTRICT", lcSchema));
      }
    }
  }
  // -----------------------------------------------------------------------------

}
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------

/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */
package com.beowurks.jequity.dao.migration;

import com.beowurks.jequity.utility.Misc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
public class V3_3__Drop_Warehouse_Data extends MigrationHelper
{

  // -----------------------------------------------------------------------------
  @Override
  public void migrate(final Connection toConnection) throws Exception
  {
    Misc.setStatusText("Migration v3.3. . . .");

    this.foConnection = toConnection;

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

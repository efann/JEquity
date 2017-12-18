/*
 * JEquity
 * Copyright(c) 2008-2017, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */
package com.beowurks.jequity.dao.migration;

import com.beowurks.jequity.utility.Misc;

import java.sql.Connection;

// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
public class V3_0__Setup_Primary_Tables extends MigrationHelper
{

  // -----------------------------------------------------------------------------
  @Override
  public void migrate(final Connection toConnection) throws Exception
  {
    Misc.setStatusText("Migration v3.0. . . .");

    this.foConnection = toConnection;

    this.executeStatements("TablesPrimarySetup.sql");

  }
  // -----------------------------------------------------------------------------
}
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------


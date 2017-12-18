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
public class V3_1__Setup_Secondary_Tables extends MigrationHelper
{

  // -----------------------------------------------------------------------------
  @Override
  public void migrate(final Connection toConnection) throws Exception
  {
    Misc.setStatusText("Migration v3.1. . . .");

    this.foConnection = toConnection;

    this.executeStatements("TablesSecondarySetup.sql");

  }
  // -----------------------------------------------------------------------------

}
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------

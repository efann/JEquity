/*
 * J'EquityFX
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */
package com.beowurks.jequityfx.dao.migration;

import java.sql.Connection;

// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
public class V3_5__Drop_Add_Daily_Fields extends MigrationHelper
{

  // -----------------------------------------------------------------------------
  @Override
  public void migrate(final Connection toConnection) throws Exception
  {
    this.foConnection = toConnection;

    this.executeStatements("DropAddDailyFields.sql");

  }
  // -----------------------------------------------------------------------------

}
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------

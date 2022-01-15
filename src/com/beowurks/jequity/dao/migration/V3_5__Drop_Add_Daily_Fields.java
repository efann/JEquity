/*
 * JEquity
 * Copyright(c) 2008-2022, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */
package com.beowurks.jequity.dao.migration;

import org.flywaydb.core.api.migration.Context;

// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
public class V3_5__Drop_Add_Daily_Fields extends MigrationHelper
{

  // -----------------------------------------------------------------------------
  @Override
  public void migrate(final Context toContext) throws Exception
  {
    this.foConnection = toContext.getConnection();

    this.executeStatements("sql/DropAddDailyFields.template");

  }
  // -----------------------------------------------------------------------------

}
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------

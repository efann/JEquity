/*
 * JEquity
 * Copyright(c) 2008-2023, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */
package com.beowurks.jequity.dao.migration;

import com.beowurks.jequity.utility.Misc;
import org.flywaydb.core.api.migration.Context;

// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
public class V3_2__Setup_Constraints extends MigrationHelper
{

  // -----------------------------------------------------------------------------
  @Override
  public void migrate(final Context toContext) throws Exception
  {
    Misc.setStatusText("Migration v3.2. . . .");

    this.foConnection = toContext.getConnection();

    this.executeStatements("sql/ConstraintsSetup.template");

  }
  // -----------------------------------------------------------------------------

}
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------

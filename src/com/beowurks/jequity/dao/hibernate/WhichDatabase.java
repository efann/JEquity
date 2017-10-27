/*
 * J'EquityFX
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */
package com.beowurks.jequity.dao.hibernate;

import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;

import java.sql.Connection;
import java.sql.SQLException;

// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
public class WhichDatabase
{

  private boolean flMySQL = false;
  private boolean flPostgres = false;
  private boolean flDerby = false;

  // -----------------------------------------------------------------------------
  public WhichDatabase(final Connection toConnection)
  {
    try
    {
      final String lcDatabase = toConnection.getMetaData().getDatabaseProductName();

      if (lcDatabase.contains(Constants.FLYWAY_POSTGRES))
      {
        this.flPostgres = true;
      }
      else if (lcDatabase.contains(Constants.FLYWAY_DERBY))
      {
        this.flDerby = true;
      }
      else if (lcDatabase.contains(Constants.FLYWAY_MYSQL))
      {
        this.flMySQL = true;
      }
    }
    catch (final SQLException loErr)
    {
      Misc.errorMessage(String.format("There was an error in determining the database server:\n\n%s", loErr.getMessage()));
    }

  }

  // -----------------------------------------------------------------------------
  public boolean isMySQL()
  {
    return (this.flMySQL);
  }

  // -----------------------------------------------------------------------------
  public boolean isApacheDerby()
  {
    return (this.flDerby);
  }

  // -----------------------------------------------------------------------------
  public boolean isPostgreSQL()
  {
    return (this.flPostgres);
  }
  // -----------------------------------------------------------------------------

}
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------

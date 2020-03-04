/*
 * JEquity
 * Copyright(c) 2008-2020, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
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

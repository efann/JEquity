/*
 * JEquity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */
package com.beowurks.jequity.dao.migration;

import com.beowurks.jequity.dao.hibernate.WhichDatabase;
import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.AppProperties;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import org.flywaydb.core.Flyway;

// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
public final class FlywayMigration
{

  public static final FlywayMigration INSTANCE = new FlywayMigration();

  private final Flyway foFlyway = new Flyway();

  private WhichDatabase foWhichDatabase = null;

  // -----------------------------------------------------------------------------
  private FlywayMigration()
  {
  }

  // -----------------------------------------------------------------------------
  public boolean migrate()
  {
    // From http://stackoverflow.com/questions/1921975/java-convert-package-name-to-path
    final String lcPath = this.getClass().getPackage().getName().replace(".", "/") + "/";

    boolean llOkay = true;
    final AppProperties loAppProp = AppProperties.INSTANCE;

    if (loAppProp.getFlywayAlwaysCheck() || (loAppProp.getFlywaySuccessfulJEquityVersion().compareTo(Main.getApplicationFullName()) != 0))
    {
      Misc.setStatusText("Checking for data migrations. . . .");

      try
      {
        final String lcConnectionURL = loAppProp.getConnectionURL();
        if (lcConnectionURL.isEmpty())
        {
          return (false);
        }

        this.foFlyway.setLocations(lcPath);
        this.foFlyway.setDataSource(lcConnectionURL, loAppProp.getConnectionUser(), loAppProp.getConnectionPassword());

        this.foWhichDatabase = new WhichDatabase(this.foFlyway.getDataSource().getConnection());

        // In Flyway.java, line 975, the migration routine wants to reset the schema to the default
        // schema of the database. However, if the default schema has not been created, then
        // an exception is thrown at the end of the migration. And with Apache Derby, the default
        // schema is based upon the user name.
        // By doing 2 schemas, you will notice JEquityRCP and JEQUITY in the database. And you
        // should have JEquityRCP.schema_version, not JEQUITY.schema_version.
        if (this.isApacheDerby())
        {
          // From https://github.com/flyway/flyway/issues/1331
          this.foFlyway.setValidateOnMigrate(false);

          this.foFlyway.setSchemas(Constants.FLYWAY_JEQUITY_SCHEMA, Constants.FLYWAY_DERBY_DEFAULT_SCHEMA);
        }
        else if (!this.isMySQL())
        {
          this.foFlyway.setSchemas(Constants.FLYWAY_JEQUITY_SCHEMA);
        }

        // Will call baseline if needed.
        this.foFlyway.setBaselineOnMigrate(true);

        this.foFlyway.migrate();

        if (this.isApacheDerby())
        {
          this.foFlyway.validate();
        }

        loAppProp.setFlywaySuccessfulJEquityVersion(Main.getApplicationFullName());
      }
      catch (final Exception loErr)
      {
        llOkay = false;
        // You have to use Util.errorMessage rather than Misc.errorMessage as some of the pieces parts of JavaFX
        // are not yet working.
        Misc.errorMessage(String.format("There was a problem in opening the database:\n\n%s\n\nIf Apache Derby, make sure the directory does not already exist. Otherwise ensure that your login parameters are correct.", loErr.getMessage()));
      }

      Misc.setStatusText("Migrations verified. . . .");
    }

    return (llOkay);
  }

  // -----------------------------------------------------------------------------
  protected boolean isMySQL() throws Exception
  {
    return (this.foWhichDatabase.isMySQL());
  }

  // -----------------------------------------------------------------------------
  protected boolean isApacheDerby() throws Exception
  {
    return (this.foWhichDatabase.isApacheDerby());
  }

  // -----------------------------------------------------------------------------
  protected boolean isPostgreSQL() throws Exception
  {
    return (this.foWhichDatabase.isPostgreSQL());
  }
  // -----------------------------------------------------------------------------
}
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------

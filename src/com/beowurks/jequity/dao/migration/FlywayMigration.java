/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */
package com.beowurks.jequity.dao.migration;

import com.beowurks.jequity.dao.hibernate.WhichDatabase;
import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.AppProperties;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;


// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
public final class FlywayMigration
{

  public static final FlywayMigration INSTANCE = new FlywayMigration();

  private Flyway foFlyway;

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

        // From https://github.com/flyway/flyway/issues/1928
        final ClassicConfiguration loConfiguration = new ClassicConfiguration();

        loConfiguration.setDataSource(lcConnectionURL, loAppProp.getConnectionUser(), loAppProp.getConnectionPassword());
        loConfiguration.setLocationsAsStrings(lcPath);

        this.foWhichDatabase = new WhichDatabase(loConfiguration.getDataSource().getConnection());

        // In Flyway.java, line 975, the migration routine wants to reset the schema to the default
        // schema of the database. However, if the default schema has not been created, then
        // an exception is thrown at the end of the migration. And with Apache Derby, the default
        // schema is based upon the user name.
        // By doing 2 schemas, you will notice JEquityRCP and JEQUITY in the database. And you
        // should have JEquityRCP.schema_version, not JEQUITY.schema_version.
        if (this.isApacheDerby())
        {
          // From https://github.com/flyway/flyway/issues/1331
          loConfiguration.setValidateOnMigrate(false);

          loConfiguration.setSchemas(Constants.FLYWAY_JEQUITY_SCHEMA, Constants.FLYWAY_DERBY_DEFAULT_SCHEMA);
        }
        else if (!this.isMySQL())
        {
          loConfiguration.setSchemas(Constants.FLYWAY_JEQUITY_SCHEMA);
        }

        // Will call baseline if needed.
        loConfiguration.setBaselineOnMigrate(true);

        this.foFlyway = new Flyway(loConfiguration);
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

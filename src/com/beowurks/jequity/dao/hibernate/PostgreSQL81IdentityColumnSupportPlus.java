/*
 * JEquity
 * Copyright(c) 2008-2025, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */
package com.beowurks.jequity.dao.hibernate;

import org.hibernate.dialect.identity.PostgreSQL81IdentityColumnSupport;

// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
public class PostgreSQL81IdentityColumnSupportPlus extends PostgreSQL81IdentityColumnSupport
{

  // -----------------------------------------------------------------------------
  // From https://hibernate.atlassian.net/plugins/servlet/mobile#issue/HHH-8574
  // The default behaviour is quite bizarre.
  // By the way, you can also query the sequence id:
  //   select sequence_schema, sequence_name from information_schema.sequences;
  // Currently, the parameters look like the following:
  //   tcTable = "JEquity"."Financial"
  //   tcColumn = FINANCIALID
  @Override
  public String getIdentitySelectString(final String tcTable, final String tcColumn, final int tnType)
  {
    // If case-sensitive. . . .
    if (tcTable.endsWith("\""))
    {
      // Create something like "JEquity"."Financial_financialid_seq"
      final String lcTable = tcTable.substring(0, tcTable.length() - 1);
      return ("select currval('" + lcTable + "_" + tcColumn.toLowerCase() + "_seq\"')");
    }

    return (super.getIdentitySelectString(tcTable, tcColumn, tnType));
  }
  // -----------------------------------------------------------------------------

}
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------

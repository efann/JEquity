/*
 * JEquity
 * Copyright(c) 2008-2022, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */
package com.beowurks.jequity.dao.hibernate;

import org.hibernate.dialect.PostgreSQL91Dialect;
import org.hibernate.dialect.identity.IdentityColumnSupport;

// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
public class PostgreSQL91DialectPlus extends PostgreSQL91Dialect
{
  // -----------------------------------------------------------------------------

  @Override
  public IdentityColumnSupport getIdentityColumnSupport()
  {
    return new PostgreSQL81IdentityColumnSupportPlus();
  }
  // -----------------------------------------------------------------------------

}
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------


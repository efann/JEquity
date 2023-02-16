/*
 * JEquity
 * Copyright(c) 2008-2023, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.dao.hibernate.threads;

import java.time.LocalDate;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// Objects derived from this class need to be immutable. Otherwise, if linked to a DataSeries element and changed
// with another value for another DataSeries element, bugs occur.
public class DataExtraValue
{
  private final LocalDate foDate;
  private final int fnCountWeekDays;

  // ---------------------------------------------------------------------------------------------------------------------
  public DataExtraValue(final LocalDate toDate, final int tnCountWeekDays)
  {
    this.foDate = toDate;
    this.fnCountWeekDays = tnCountWeekDays;
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public LocalDate getDate()
  {
    return (this.foDate);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public int getWeekDays()
  {
    return (this.fnCountWeekDays);
  }
  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

/*
 * JEquity
 * Copyright(c) 2008-2023, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.controller.tab;

import java.time.LocalDate;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class HistoricalDateInfo
{
  // Start date is the same for both Data and Trends. However, it is only used by Data
  // as Trends uses the start of the actual data.
  public LocalDate foLocalStartDate;

  public int fnDisplaySequenceData;
  public LocalDate foLocalEndDateData;

  public int fnDisplaySequenceTrends;
  public LocalDate foLocalEndDateTrends;
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

/*
 * J'EquityFX
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */
package com.beowurks.jequityfx.dao.combobox;

// ---------------------------------------------------------------------------
// ---------------------------------------------------------------------------
// ---------------------------------------------------------------------------
public class StringKeyItem
{

  private final String fcKey;
  private final String fcDescription;

  // ---------------------------------------------------------------------------
  public StringKeyItem(final String tcKey, final String tcDescription)
  {
    this.fcKey = tcKey.trim();
    this.fcDescription = tcDescription.trim();
  }

  // ---------------------------------------------------------------------------
  public String getKey()
  {
    return (this.fcKey);
  }

  // ---------------------------------------------------------------------------
  public String getDescription()
  {
    return (this.fcDescription);
  }

  // ---------------------------------------------------------------------------
  // By the way, toString is used by the JComboBox renderer.
  @Override
  public String toString()
  {
    return (this.fcDescription);
  }
  // ---------------------------------------------------------------------------
}

/*
 * J'EquityFX
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */
package com.beowurks.jequityfx.view.combobox;

import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

// ---------------------------------------------------------------------------
// ---------------------------------------------------------------------------
// ---------------------------------------------------------------------------
public class StringKeyComboBoxFX<T> extends ComboBox<T>
{
  // ---------------------------------------------------------------------------
  public StringKeyComboBoxFX()
  {
    this.initComboBox();
  }

  // ---------------------------------------------------------------------------
  public StringKeyComboBoxFX(final ObservableList<T> toItems)
  {
    super(toItems);

    this.initComboBox();
  }

  // ---------------------------------------------------------------------------
  private void initComboBox()
  {
    this.setCursor(javafx.scene.Cursor.HAND);
  }

  // ---------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------
// ---------------------------------------------------------------------------
// ---------------------------------------------------------------------------

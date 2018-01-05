/*
 * JEquity
 * Copyright(c) 2008-2018, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */
package com.beowurks.jequity.view.combobox;

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

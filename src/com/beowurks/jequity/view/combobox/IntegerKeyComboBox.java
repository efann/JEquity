/*
 * J'EquityFX
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */
package com.beowurks.jequity.view.combobox;

import com.beowurks.jequity.dao.combobox.IntegerKeyItem;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import java.awt.Cursor;
import java.util.Vector;

// ---------------------------------------------------------------------------
// ---------------------------------------------------------------------------
// ---------------------------------------------------------------------------
public class IntegerKeyComboBox extends JComboBox
{

  // ---------------------------------------------------------------------------
  public IntegerKeyComboBox()
  {
    this.initComboBox();
  }

  // ---------------------------------------------------------------------------
  public IntegerKeyComboBox(final ComboBoxModel<IntegerKeyItem> taModel)
  {
    super(taModel);

    this.initComboBox();
  }

  // ---------------------------------------------------------------------------
  IntegerKeyComboBox(final IntegerKeyItem[] taItems)
  {
    super(taItems);

    this.initComboBox();
  }

  // ---------------------------------------------------------------------------
  IntegerKeyComboBox(final Vector<IntegerKeyItem> toItems)
  {
    super(toItems);

    this.initComboBox();
  }

  // ---------------------------------------------------------------------------
  private void initComboBox()
  {
    this.setCursor(new Cursor(Cursor.HAND_CURSOR));
  }

  // ---------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------
// ---------------------------------------------------------------------------
// ---------------------------------------------------------------------------

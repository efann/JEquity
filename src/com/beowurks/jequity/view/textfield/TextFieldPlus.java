/*
 * JEquity
 * Copyright(c) 2008-2023, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.textfield;

import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.view.interfaces.IReadOnly;
import org.controlsfx.control.textfield.CustomTextField;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class TextFieldPlus extends CustomTextField implements IReadOnly
{

  // ---------------------------------------------------------------------------------------------------------------------
  public TextFieldPlus()
  {
    super();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public TextFieldPlus(final String tcText)
  {
    super();

    this.setText(tcText);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setReadOnly(final boolean tlReadOnly)
  {
    this.setEditable(!tlReadOnly);

    this.setStyle(tlReadOnly ? Constants.DISABLED_CONTROL_BACKGROUND : "");
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

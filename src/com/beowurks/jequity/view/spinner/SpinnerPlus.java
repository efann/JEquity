/*
 * JEquity
 * Copyright(c) 2008-2022, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.spinner;

import com.beowurks.jequity.view.interfaces.IReadOnly;
import javafx.scene.control.Spinner;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class SpinnerPlus<T> extends Spinner<T> implements IReadOnly
{
  // ---------------------------------------------------------------------------------------------------------------------
  public void setReadOnly(final boolean tlReadOnly)
  {
    //this.setEditable(tlReadOnly);
    this.setDisable(tlReadOnly);

    if (this.getEditor().isEditable())
    {
      this.getEditor().setEditable(false);
    }

    if (tlReadOnly)
    {
      this.getStyleClass().add("hide-spinner-increment-arrow");
      this.getStyleClass().add("hide-spinner-decrement-arrow");
    }
    else
    {
      this.getStyleClass().remove("hide-spinner-increment-arrow");
      this.getStyleClass().remove("hide-spinner-decrement-arrow");
    }

  }
  // ---------------------------------------------------------------------------------------------------------------------


}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

/*
 * JEquity
 * Copyright(c) 2008-2018, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.dialog;

import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.Misc;
import com.sun.javafx.scene.control.skin.resources.ControlResources;
import javafx.beans.NamedArg;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// From javafx\scene\control\TextInputDialog.java
public class PasswordBaseDialog extends Dialog<String>
{

  protected final String fcDefaultValue;

  // ---------------------------------------------------------------------------------------------------------------------
  public PasswordBaseDialog()
  {
    this("");
  }
  // ---------------------------------------------------------------------------------------------------------------------

  public PasswordBaseDialog(@NamedArg("defaultValue") final String tcDefaultValue)
  {
    final DialogPane loDialogPane = this.getDialogPane();
    loDialogPane.setHeaderText(null);
    this.initOwner(Main.getPrimaryStage());

    loDialogPane.getStylesheets().add(Misc.ALERT_STYLE_SHEET);

    this.fcDefaultValue = tcDefaultValue;

    this.setTitle(ControlResources.getString("Dialog.confirm.title"));

    loDialogPane.getStyleClass().add("text-input-dialog");
    loDialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
  }

  // ---------------------------------------------------------------------------------------------------------------------

  public final String getDefaultValue()
  {
    return (this.fcDefaultValue);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // From javafx\scene\control\DialogPane.java
  protected static Label createContentLabel(final String tcText)
  {
    final Label loLabel = new Label(tcText);

    loLabel.setMaxWidth(Double.MAX_VALUE);
    loLabel.setMaxHeight(Double.MAX_VALUE);
    loLabel.getStyleClass().add("content");
    loLabel.setWrapText(true);
    loLabel.setPrefWidth(360);

    return (loLabel);
  }

  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

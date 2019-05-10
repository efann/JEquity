/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.dialog;

import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.Misc;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

import java.io.IOException;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

public class AboutDialog extends Dialog
{

  // ---------------------------------------------------------------------------------------------------------------------
  public AboutDialog()
  {
    final DialogPane loDialogPane = this.getDialogPane();
    loDialogPane.getStylesheets().add(Misc.ALERT_STYLE_SHEET);

    this.setTitle(String.format("About %s", Main.getApplicationFullName()));

    loDialogPane.setHeaderText(null);
    loDialogPane.getButtonTypes().addAll(ButtonType.OK);

    this.initOwner(Main.getPrimaryStage());

    // From https://stackoverflow.com/questions/40031632/custom-javafx-dialog
    final FXMLLoader loLoader = new FXMLLoader(this.getClass().getResource("/com/beowurks/jequity/view/fxml/AboutDialog.fxml"));
    try
    {
      loDialogPane.setContent(loLoader.load());
    }
    catch (final IOException loErr)
    {
      loErr.printStackTrace();
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

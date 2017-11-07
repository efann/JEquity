/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.view.dialog;

import com.beowurks.jequity.controller.OptionsController;
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

public class OptionsDialog extends Dialog
{

  private final OptionsController foController;

  // ---------------------------------------------------------------------------------------------------------------------
  public OptionsDialog()
  {
    final DialogPane loDialogPane = this.getDialogPane();
    loDialogPane.getStylesheets().add(Misc.ALERT_STYLE_SHEET);

    this.setTitle(String.format("Options for %s", Main.getApplicationFullName()));

    loDialogPane.setHeaderText(null);
    loDialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    this.initOwner(Main.getPrimaryStage());

    // From https://stackoverflow.com/questions/40031632/custom-javafx-dialog
    final FXMLLoader loLoader = new FXMLLoader(this.getClass().getResource("../fxml/OptionsDialog.fxml"));
    try
    {
      loDialogPane.setContent(loLoader.load());
    }
    catch (final IOException loErr)
    {
      loErr.printStackTrace();
    }

    this.foController = loLoader.getController();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public OptionsController getController()
  {
    return (this.foController);
  }
  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

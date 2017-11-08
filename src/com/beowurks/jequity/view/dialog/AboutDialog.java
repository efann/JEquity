/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.view.dialog;

import com.beowurks.jequity.controller.AboutBoxController;
import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.Misc;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.DialogPane;

import java.io.IOException;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

public class AboutDialog extends Dialog
{

  private final AboutBoxController foController;

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
    final FXMLLoader loLoader = new FXMLLoader(this.getClass().getResource("../fxml/AboutDialog.fxml"));
    try
    {
      loDialogPane.setContent(loLoader.load());
    }
    catch (final IOException loErr)
    {
      loErr.printStackTrace();
    }

    this.foController = loLoader.getController();

    // For some reason, the skin is not being set for the TableView in a dialog.
    this.setOnShown(new EventHandler<DialogEvent>()
    {
      public void handle(DialogEvent toEvent)
      {
        AboutDialog.this.foController.setupTable();
      }
    });
  }

  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

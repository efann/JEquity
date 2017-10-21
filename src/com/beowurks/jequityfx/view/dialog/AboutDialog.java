/*
 * J'EquityFX
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequityfx.view.dialog;

import com.beowurks.jequityfx.main.Main;
import com.beowurks.jequityfx.utility.Misc;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.stage.Screen;

import javax.swing.text.TableView;
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
    final FXMLLoader loader = new FXMLLoader(this.getClass().getResource("AboutDialog.fxml"));
    try
    {
      loDialogPane.setContent(loader.load());
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

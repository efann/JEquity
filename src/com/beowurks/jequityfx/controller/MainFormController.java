/*
 * J'EquityFX
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequityfx.controller;

import com.beowurks.jequityfx.main.Main;
import com.beowurks.jequityfx.utility.Misc;
import com.beowurks.jequityfx.view.CheckForUpdates;
import com.beowurks.jequityfx.view.dialog.AboutDialog;
import javafx.fxml.FXML;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class MainFormController
{

  // ---------------------------------------------------------------------------------------------------------------------
  @FXML
  private void exitApplication()
  {
    Misc.startShutdown();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @FXML
  private void showCredits()
  {
    final String lcTitle = String.format("Credits for %s", Main.getApplicationFullName());
    Misc.displayWebContent(lcTitle, "http://www.beowurks.com/ajax/node/32");
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @FXML
  private void showUpdates()
  {
    new CheckForUpdates();
  }
  // ---------------------------------------------------------------------------------------------------------------------
  @FXML
  private void showAbout()
  {
    final AboutDialog loDialog = new AboutDialog();
    loDialog.showAndWait();
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

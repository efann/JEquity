/*
 * JEquity
 * Copyright(c) 2008-2021, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.dialog;

import com.beowurks.jequity.view.textfield.PasswordFieldPlus;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class PasswordConfirmDialog extends PasswordBaseDialog
{
  private final GridPane foGrid;

  private final Label foLabel1;
  private final PasswordFieldPlus foTextField1;

  private final Label foLabel2;
  private final PasswordFieldPlus foTextField2;

  // ---------------------------------------------------------------------------------------------------------------------
  public PasswordConfirmDialog()
  {
    this("");
  }
  // ---------------------------------------------------------------------------------------------------------------------

  public PasswordConfirmDialog(@NamedArg("defaultValue") final String tcDefaultValue)
  {
    super(tcDefaultValue);

    final DialogPane loDialogPane = this.getDialogPane();

    // -- PasswordFieldPlus
    this.foTextField1 = new PasswordFieldPlus();
    this.foTextField1.setText(tcDefaultValue);
    this.foTextField1.setMaxWidth(Double.MAX_VALUE);
    GridPane.setHgrow(this.foTextField1, Priority.ALWAYS);
    GridPane.setFillWidth(this.foTextField1, true);

    this.foTextField2 = new PasswordFieldPlus();
    this.foTextField2.setText(tcDefaultValue);
    this.foTextField2.setMaxWidth(Double.MAX_VALUE);
    GridPane.setHgrow(this.foTextField2, Priority.ALWAYS);
    GridPane.setFillWidth(this.foTextField2, true);

    // -- Label
    this.foLabel1 = PasswordConfirmDialog.createContentLabel("Password");
    this.foLabel1.setPrefWidth(Region.USE_COMPUTED_SIZE);

    this.foLabel2 = PasswordConfirmDialog.createContentLabel("Confirm Password");
    this.foLabel2.setPrefWidth(Region.USE_COMPUTED_SIZE);

    // -- Grid
    this.foGrid = new GridPane();
    this.foGrid.setHgap(10);
    this.foGrid.setMaxWidth(Double.MAX_VALUE);
    this.foGrid.setAlignment(Pos.CENTER_LEFT);

    loDialogPane.contentTextProperty().addListener(o -> this.updateGrid());

    this.updateGrid();

    this.setResultConverter((dialogButton) ->
    {
      final ButtonData loButtonData = (dialogButton == null) ? null : dialogButton.getButtonData();
      return ((loButtonData == ButtonData.OK_DONE) ? this.getPasswordResults() : null);
    });
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private String getPasswordResults()
  {
    final String lcResults = (this.foTextField1.getText().compareTo(this.foTextField2.getText()) == 0) ? this.foTextField1.getText() : null;

    return (lcResults);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void updateGrid()
  {
    this.foGrid.getChildren().clear();

    this.foGrid.add(this.foLabel1, 0, 0);
    this.foGrid.add(this.foTextField1, 0, 1);

    this.foGrid.add(this.foLabel2, 0, 2);
    this.foGrid.add(this.foTextField2, 0, 3);

    this.getDialogPane().setContent(this.foGrid);

    Platform.runLater(this.foTextField1::requestFocus);
  }
  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

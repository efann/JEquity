/*
 * JEquity
 * Copyright(c) 2008-2017, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.view.dialog;

import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class PasswordDialog extends PasswordBaseDialog
{
  private final GridPane foGrid;
  private final Label foLabel;
  private final PasswordField foTextField;

  // ---------------------------------------------------------------------------------------------------------------------
  public PasswordDialog()
  {
    this("");
  }
  // ---------------------------------------------------------------------------------------------------------------------

  public PasswordDialog(@NamedArg("defaultValue") final String tcDefaultValue)
  {
    super(tcDefaultValue);

    final DialogPane loDialogPane = this.getDialogPane();

    // -- TextField
    this.foTextField = new PasswordField();
    this.foTextField.setText(tcDefaultValue);
    this.foTextField.setMaxWidth(Double.MAX_VALUE);
    GridPane.setHgrow(this.foTextField, Priority.ALWAYS);
    GridPane.setFillWidth(this.foTextField, true);

    // -- Label
    this.foLabel = PasswordDialog.createContentLabel("Password");
    this.foLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);

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
      return ((loButtonData == ButtonData.OK_DONE) ? this.foTextField.getText() : null);
    });
  }

  // ---------------------------------------------------------------------------------------------------------------------

  public final TextField getEditor()
  {
    return (this.foTextField);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void updateGrid()
  {
    this.foGrid.getChildren().clear();

    this.foGrid.add(this.foLabel, 0, 0);
    this.foGrid.add(this.foTextField, 0, 1);

    this.getDialogPane().setContent(this.foGrid);

    Platform.runLater(this.foTextField::requestFocus);
  }
  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

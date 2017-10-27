/*
 * J'EquityFX
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.view.dialog;

import com.beowurks.jequity.utility.Misc;
import com.sun.javafx.scene.control.skin.resources.ControlResources;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
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
// From javafx\scene\control\TextInputDialog.java
public class PasswordDialog extends Dialog<String>
{

  private final GridPane foGrid;
  private final Label foLabel;
  private final PasswordField foTextField;
  private final String fcDefaultValue;

  // ---------------------------------------------------------------------------------------------------------------------
  public PasswordDialog()
  {
    this("");
  }
  // ---------------------------------------------------------------------------------------------------------------------

  public PasswordDialog(@NamedArg("defaultValue") final String tcDefaultValue)
  {
    final DialogPane loDialogPane = this.getDialogPane();

    loDialogPane.getStylesheets().add(Misc.ALERT_STYLE_SHEET);
    // -- textfield
    this.foTextField = new PasswordField();
    this.foTextField.setText(tcDefaultValue);
    this.foTextField.setMaxWidth(Double.MAX_VALUE);
    GridPane.setHgrow(this.foTextField, Priority.ALWAYS);
    GridPane.setFillWidth(this.foTextField, true);

    // -- foLabel
    this.foLabel = PasswordDialog.createContentLabel(loDialogPane.getContentText());
    this.foLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
    this.foLabel.textProperty().bind(loDialogPane.contentTextProperty());

    this.fcDefaultValue = tcDefaultValue;

    this.foGrid = new GridPane();
    this.foGrid.setHgap(10);
    this.foGrid.setMaxWidth(Double.MAX_VALUE);
    this.foGrid.setAlignment(Pos.CENTER_LEFT);

    loDialogPane.contentTextProperty().addListener(o -> this.updateGrid());

    this.setTitle(ControlResources.getString("Dialog.confirm.title"));

    loDialogPane.setHeaderText(ControlResources.getString("Dialog.confirm.header"));
    loDialogPane.getStyleClass().add("text-input-dialog");
    loDialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    this.updateGrid();

    this.setResultConverter((dialogButton) -> {
      final ButtonData loButtonData = dialogButton == null ? null : dialogButton.getButtonData();
      return ((loButtonData == ButtonData.OK_DONE) ? this.foTextField.getText() : null);
    });
  }

  // ---------------------------------------------------------------------------------------------------------------------

  public final TextField getEditor()
  {
    return (this.foTextField);
  }
  // ---------------------------------------------------------------------------------------------------------------------

  public final String getDefaultValue()
  {
    return (this.fcDefaultValue);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // From javafx\scene\control\DialogPane.java
  private static Label createContentLabel(final String tcText)
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
  private void updateGrid()
  {
    this.foGrid.getChildren().clear();

    this.foGrid.add(this.foLabel, 0, 0);
    this.foGrid.add(this.foTextField, 1, 0);

    this.getDialogPane().setContent(this.foGrid);

    Platform.runLater(() -> this.foTextField.requestFocus());
  }
  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

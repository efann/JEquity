/*
 * JEquity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.controller.table;


import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.Misc;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
abstract public class TableModifyController extends TableBaseController
{
  protected boolean flCreatingRow = false;

  @FXML
  protected Button btnModify;

  @FXML
  protected Button btnSave;

  @FXML
  protected Button btnCancel;

  @FXML
  protected Button btnCreate;

  @FXML
  protected Button btnClone;

  @FXML
  protected Button btnRemove;

  // ---------------------------------------------------------------------------------------------------------------------
  abstract protected void removeRow();

  // ---------------------------------------------------------------------------------------------------------------------
  abstract protected void saveRow();

  // ---------------------------------------------------------------------------------------------------------------------
  abstract protected void updateComponentsContent(final boolean tlUseEmptyFields);

  // ---------------------------------------------------------------------------------------------------------------------
  abstract protected void resetTextFields(final boolean tlModify);

  // ---------------------------------------------------------------------------------------------------------------------
  abstract protected void setupListeners();

  // ---------------------------------------------------------------------------------------------------------------------
  protected void setupTooltips()
  {
    this.btnModify.setTooltip(new Tooltip("Modify the contents of the currently selected record"));
    this.btnSave.setTooltip(new Tooltip("Save any modifications to the contents of the currently selected record"));
    this.btnCancel.setTooltip(new Tooltip("Discard any modifications to the contents of the currently selected record"));

    this.btnCreate.setTooltip(new Tooltip("Create a new record"));
    this.btnClone.setTooltip(new Tooltip("Clone a new record from the currently selected record"));
    this.btnRemove.setTooltip(new Tooltip("Remove the currently selected record"));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void createRow()
  {
    this.flCreatingRow = true;

    this.updateComponentsContent(true);

    this.resetComponentsOnModify(true);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected boolean cloneRow(final Object toCurrentProperty)
  {
    if (toCurrentProperty == null)
    {
      Misc.errorMessage("You need to select a record before cloning it.");
      return (false);
    }

    this.flCreatingRow = true;

    this.updateComponentsContent(false);
    this.resetComponentsOnModify(true);

    return (true);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected boolean modifyRow(final Object toCurrentProperty)
  {
    if (toCurrentProperty == null)
    {
      Misc.errorMessage("You need to select a record before modifying it.");
      return (false);
    }

    this.resetComponentsOnModify(true);

    return (true);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void resetComponentsOnModify(final boolean tlModify)
  {
    this.resetButtons(tlModify);
    this.resetTextFields(tlModify);

    if (Main.getController() != null)
    {
      Main.getController().getToolbarController().getGroupComboBox().setDisable(tlModify);
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void cancelRow()
  {
    this.resetComponentsOnModify(false);

    this.updateComponentsContent(false);

    Misc.setStatusText("Your modifications have been cancelled.");
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void resetButtons(final boolean tlModify)
  {
    this.btnModify.setDisable(tlModify);
    this.btnCreate.setDisable(tlModify);
    this.btnClone.setDisable(tlModify);
    this.btnRemove.setDisable(tlModify);

    this.btnSave.setDisable(!tlModify);
    this.btnCancel.setDisable(!tlModify);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // Unfortunately, I can't create an inherited class from TextField and override setEditable: it's a final method.
  // Oh well. . . .
  protected void setEditable(final Control toField, final boolean tlEditable)
  {
    final String lcStyle = tlEditable ? "" : "-fx-control-inner-background: #EEEEEE";

    if (toField instanceof TextField)
    {
      ((TextField) toField).setEditable(tlEditable);
    }
    else if (toField instanceof TextArea)
    {
      final TextArea loTextArea = (TextArea) toField;
      loTextArea.setEditable(tlEditable);
    }
    else if (toField instanceof DatePicker)
    {
      final DatePicker loPicker = (DatePicker) toField;
      loPicker.setDisable(!tlEditable);
      loPicker.getEditor().setStyle(lcStyle);
    }
    else if (toField instanceof CheckBox)
    {
      toField.setDisable(!tlEditable);
    }
    else
    {
      System.err.println("Unknown class in TableModifyController.setEditable");
    }

    toField.setStyle(lcStyle);
  }
  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

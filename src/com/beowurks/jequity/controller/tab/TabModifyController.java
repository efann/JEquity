/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.controller.tab;


import com.beowurks.jequity.controller.ToolbarController;
import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import javafx.collections.ObservableList;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
abstract public class TabModifyController extends TabBaseController
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

  @FXML
  protected GridPane gridPaneComponents;

  // ---------------------------------------------------------------------------------------------------------------------
  abstract protected void removeRow();

  // ---------------------------------------------------------------------------------------------------------------------
  abstract protected void saveRow();

  // ---------------------------------------------------------------------------------------------------------------------
  abstract protected void updateComponentsContent(final boolean tlUseEmptyFields);

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
  protected void setupQuickModify(final TableView toTableView)
  {
    // From https://stackoverflow.com/questions/26563390/detect-doubleclick-on-row-of-tableview-javafx
    toTableView.setOnMouseClicked(toEvent ->
    {

      if (toEvent.getClickCount() == 2)
      {
        final EventTarget loTarget = toEvent.getTarget();
        if (!(loTarget instanceof TableColumnHeader) && !(loTarget instanceof Rectangle))
        {
          TabModifyController.this.modifyRow();
        }
      }
    });

    this.addModifyListener(this.gridPaneComponents);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // Adds a focused listener to any editable field so that when a readonly field is clicked, the user
  // will immediately be able to modify the components.
  private void addModifyListener(final Pane toParent)
  {
    for (final Node loNode : toParent.getChildren())
    {
      if ((loNode instanceof TextField) || (loNode instanceof TextArea) || (loNode instanceof CheckBox))
      {
        loNode.focusedProperty().addListener((obs, oldVal, newVal) ->
            TabModifyController.this.modifyRow());
      }
      else if (loNode instanceof DatePicker)
      {
        ((DatePicker) loNode).getEditor().focusedProperty().addListener((obs, oldVal, newVal) ->
            TabModifyController.this.modifyRow());
      }
      else if (loNode instanceof HBox)
      {
        // CheckBox can only be disabled, not readonly. So if you surround with a container
        // the container can implement a mouse listener. Cool. . . .
        boolean llAddListener = false;
        final ObservableList<Node> loChildren = ((HBox) loNode).getChildren();
        for (final Node loChildNode : loChildren)
        {
          if (loChildNode instanceof CheckBox)
          {
            llAddListener = true;
            break;
          }
        }
        if (llAddListener)
        {
          loNode.setOnMouseClicked(toEvent ->
              TabModifyController.this.modifyRow());
        }
      }
      // Otherwise, do recursion.
      else if (loNode instanceof Pane)
      {
        this.addModifyListener((Pane) loNode);
      }

    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected boolean isEditing()
  {
    return (this.btnModify.isDisabled() || this.btnCreate.isDisabled() || this.btnClone.isDisabled());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void createRow()
  {
    this.flCreatingRow = true;

    this.updateComponentsContent(true);

    this.resetComponentsOnModify(true);

    Misc.setStatusText("Creating new row of data.");
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

    Misc.setStatusText("Cloning from existing row of data to a new one.");

    return (true);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  abstract protected boolean modifyRow();

  // ---------------------------------------------------------------------------------------------------------------------
  protected boolean modifyRow(final Object toCurrentProperty)
  {
    if (toCurrentProperty == null)
    {
      Misc.errorMessage("You need to select a record before modifying it.");
      return (false);
    }

    this.resetComponentsOnModify(true);

    Misc.setStatusText("Modifying current row of data.");

    return (true);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void resetComponentsOnModify(final boolean tlModify)
  {
    this.resetButtons(tlModify);
    this.resetTextFields(tlModify);

    if (Main.getController() != null)
    {
      final ToolbarController loController = Main.getController().getToolbarController();

      loController.getGroupComboBox().setDisable(tlModify);
      loController.getUpdateButton().setDisable(tlModify);
      loController.getRefreshButton().setDisable(tlModify);
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
  protected void resetTextFields(final boolean tlModify)
  {
    this.setEditableFields(this.gridPaneComponents, tlModify);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void setEditableFields(final Pane toParent, final boolean tlModify)
  {
    for (final Node loNode : toParent.getChildren())
    {
      if (loNode instanceof Control)
      {
        this.setEditable((Control) loNode, tlModify);
      }
      else if (loNode instanceof Pane)
      {
        this.setEditableFields((Pane) loNode, tlModify);
      }
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  // Unfortunately, I can't create an inherited class from TextField and override setEditable: it's a final method.
  // Oh well. . . .
  protected void setEditable(final Control toField, final boolean tlEditable)
  {
    final String lcStyle = tlEditable ? "" : Constants.DISABLED_CONTROL_BACKGROUND;

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

      Misc.setEditableForDatePicker(loPicker, tlEditable);
    }
    else if (toField instanceof CheckBox)
    {
      ((CheckBox) toField).setDisable(!tlEditable);
    }
    else if (!(toField instanceof Label) && !(toField instanceof Button) && !(toField instanceof Hyperlink))
    {
      System.err.println(String.format("Unknown class in TabModifyController.setEditable: %s", toField.getClass()));
    }

    toField.setStyle(lcStyle);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected Control findFocused()
  {
    return (this.findFocusedComponent(this.gridPaneComponents));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private Control findFocusedComponent(final Pane toParent)
  {
    for (final Node loNode : toParent.getChildren())
    {
      if ((loNode instanceof Control) && (loNode.isFocused()))
      {
        return ((Control) loNode);
      }
      else if ((loNode instanceof DatePicker) && (((DatePicker) loNode).getEditor().isFocused()))
      {
        return ((Control) loNode);
      }
      else if (loNode instanceof Pane)
      {
        final Node loNodeRecursion = (this.findFocusedComponent((Pane) loNode));
        if (loNodeRecursion != null)
        {
          return ((Control) loNodeRecursion);
        }
      }
    }

    return (null);
  }

  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

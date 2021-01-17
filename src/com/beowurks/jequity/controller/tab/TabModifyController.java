/*
 * JEquity
 * Copyright(c) 2008-2021, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.controller.tab;


import com.beowurks.jequity.controller.ToolbarController;
import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.AppProperties;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import com.beowurks.jequity.view.checkbox.CheckBoxPlus;
import com.beowurks.jequity.view.combobox.ComboBoxPlus;
import com.beowurks.jequity.view.interfaces.IReadOnly;
import com.beowurks.jequity.view.textarea.TextAreaPlus;
import com.beowurks.jequity.view.textfield.DatePickerPlus;
import com.beowurks.jequity.view.textfield.TextFieldPlus;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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
    // If grid row double-clicked then modify row.
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
      if ((loNode instanceof TextFieldPlus) || (loNode instanceof TextAreaPlus))
      {
        loNode.focusedProperty().addListener((obs, oldVal, newVal) ->
          TabModifyController.this.modifyRow());
      }
      else if ((loNode instanceof CheckBoxPlus) || (loNode instanceof ComboBoxPlus))
      {
        // CheckBoxPlus can only be disabled, not readonly. So if you surround with a container,
        //
        // ComboBoxPlus should only be disabled, not readonly, as there's some weird bug. You can read
        // more about the problem in its setReadOnly function.
        //
        // The container can implement a mouse listener. Cool. . . .
        // However, there could be other components in an HBox.
        final Parent loParent = loNode.getParent();
        if ((loParent instanceof HBox) || (loParent instanceof VBox))
        {
          loParent.setOnMouseClicked(toEvent ->
            TabModifyController.this.modifyRow());
        }
      }
      else if (loNode instanceof DatePickerPlus)
      {
        ((DatePickerPlus) loNode).getEditor().focusedProperty().addListener((obs, oldVal, newVal) ->
          TabModifyController.this.modifyRow());
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
  protected void createRow(final boolean tlCheckGroupRecords)
  {
    if (tlCheckGroupRecords)
    {
      if (Misc.countGroupRecords() == 0)
      {
        Misc.errorMessage(Constants.NO_GROUPS_EXIST_YET);
        return;
      }
    }

    this.flCreatingRow = true;

    this.updateComponentsContent(true);

    this.resetComponentsOnModify(true);

    Misc.setStatusText("Creating new row of data.");
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected boolean cloneRow(final Object toCurrentProperty, final boolean tlCheckGroupRecords)
  {
    if (tlCheckGroupRecords)
    {
      if (Misc.countGroupRecords() == 0)
      {
        Misc.errorMessage(Constants.NO_GROUPS_EXIST_YET);
        return (false);
      }
    }

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
  public void resetComponentsOnModify(final boolean tlModify)
  {
    this.resetButtons(tlModify);
    this.resetTextFields(tlModify);

    if (Main.getController() != null)
    {
      final boolean llManualDataEntry = AppProperties.INSTANCE.getManualFinancialData();

      Main.getController().getTabSymbol().setDisable(llManualDataEntry);

      final ToolbarController loController = Main.getController().getToolbarController();
      loController.getGroupComboBox().setDisable(tlModify);
      loController.getUpdateButton().setDisable(tlModify || llManualDataEntry);
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
      if (loNode instanceof IReadOnly)
      {
        ((IReadOnly) loNode).setReadOnly(!tlModify);
      }
      else if (loNode instanceof Pane)
      {
        this.setEditableFields((Pane) loNode, tlModify);
      }

      else if (!(loNode instanceof Label) && !(loNode instanceof Button) && !(loNode instanceof Hyperlink))
      {
        System.err.println("==========================================================================================");
        System.err.println(String.format("Unknown class in TabModifyController.setEditable: %s", loNode.getClass()));
        System.err.println("==========================================================================================");
      }

    }

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
      else if ((loNode instanceof DatePickerPlus) && (((DatePickerPlus) loNode).getEditor().isFocused()))
      {
        return ((Control) loNode);
      }
      else if (loNode instanceof Pane)
      {
        final Control loNodeRecursion = (this.findFocusedComponent((Pane) loNode));
        if (loNodeRecursion != null)
        {
          return (loNodeRecursion);
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

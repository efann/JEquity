/*
 * JEquity
 * Copyright(c) 2008-2018, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.table;

import com.sun.javafx.scene.control.skin.NestedTableColumnHeader;
import com.sun.javafx.scene.control.skin.TableColumnHeader;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.lang.reflect.Method;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class TableViewPlus extends TableView
{
  private boolean flSkinPropertyListenerAdded = false;

  private final StringBuffer foKeySearch = new StringBuffer("");

  private Label foStatusMessage = null;

  // ---------------------------------------------------------------------------------------------------------------------
  public TableViewPlus()
  {
    super();

    this.setEditable(false);
    this.setupKeySearch();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public TableViewPlus(final ObservableList toItems)
  {
    super(toItems);

    this.setEditable(false);
    this.setupKeySearch();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setStatusMessage(final Label toLabel)
  {
    this.foStatusMessage = toLabel;
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void updateMessage()
  {
    if (this.foStatusMessage != null)
    {
      this.foStatusMessage.setText(this.foKeySearch.length() > 0 ? String.format("Finding: %s", this.foKeySearch.toString()) : "");
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void updateMessage(final String tcMessage)
  {
    if (this.foStatusMessage != null)
    {
      this.foStatusMessage.setText(tcMessage);
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void resizeColumnsToFit()
  {
    if (this.getSkin() != null)
    {
      this.resizeColumnsPlatformCheck();
    }
    else if (!this.flSkinPropertyListenerAdded)
    {
      this.flSkinPropertyListenerAdded = true;

      // From https://stackoverflow.com/questions/38718926/how-to-get-tableheaderrow-from-tableview-nowadays-in-javafx
      // Add listener to detect when the skin has been initialized and therefore this.getSkin() != null.
      this.skinProperty().addListener((a, b, newSkin) -> this.resizeColumnsPlatformCheck());
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  protected void setupKeySearch()
  {
    this.setOnKeyTyped((KeyEvent toEvent) ->
    {
      final ObservableList<TableColumn> loColumnList = this.getSortOrder();
      if (loColumnList.isEmpty())
      {
        return;
      }

      final String lcCharacter = toEvent.getCharacter();
      if ((int) lcCharacter.toCharArray()[0] == 8)
      {
        final int lnLength = this.foKeySearch.length();
        if (lnLength > 0)
        {
          this.foKeySearch.setLength(lnLength - 1);
        }
      }
      else
      {
        this.foKeySearch.append(lcCharacter.toLowerCase());
      }

      final TableColumn loColumn = loColumnList.get(0);
      final int lnRows = this.getItems().size();
      for (int i = 0; i < lnRows; ++i)
      {
        final String lcValue = loColumn.getCellData(i).toString().toLowerCase();
        if (lcValue.indexOf(this.foKeySearch.toString()) == 0)
        {
          this.getSelectionModel().select(i);
          this.scrollTo(i);
          break;
        }
      }

      this.updateMessage();
    });

    this.setOnKeyPressed((KeyEvent toEvent) ->
    {
      final KeyCode loKey = toEvent.getCode();

      if (loKey.isArrowKey() || loKey.isFunctionKey() || loKey.isNavigationKey() || loKey.isMediaKey())
      {
        this.foKeySearch.setLength(0);
        this.foKeySearch.append("");
      }

      this.updateMessage();
    });
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void resizeColumnsPlatformCheck()
  {
    if (Platform.isFxApplicationThread())
    {
      this.resizeAllColumnsUsingReflection();
    }
    else
    {
      Platform.runLater(this::resizeAllColumnsUsingReflection);
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // From https://stackoverflow.com/questions/38090353/javafx-how-automatically-width-of-tableview-column-depending-on-the-content
  // Geesh. . . .
  private void resizeAllColumnsUsingReflection()
  {
    // Otherwise, you can have ghost values in rows that are not overwritten with data.
    this.refresh();
    // Otherwise the column will not resort after refreshing.
    this.sort();

    final TableViewSkin<?> loSkin = (TableViewSkin<?>) this.getSkin();
    // The skin is not applied till after being rendered. Which is happening with the About dialog.
    if (loSkin == null)
    {
      System.err.println("Skin is null");
      return;
    }

    final TableHeaderRow loHeaderRow = loSkin.getTableHeaderRow();
    final NestedTableColumnHeader loRootHeader = loHeaderRow.getRootHeader();
    for (final TableColumnHeader loColumnHeader : loRootHeader.getColumnHeaders())
    {
      try
      {
        final TableColumn<?, ?> loColumn = (TableColumn<?, ?>) loColumnHeader.getTableColumn();
        if (loColumn != null)
        {
          final Method loMethod = loSkin.getClass().getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
          loMethod.setAccessible(true);
          loMethod.invoke(loSkin, loColumn, 30);
        }
      }
      catch (final Throwable loErr)
      {
        loErr.printStackTrace(System.err);
      }
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

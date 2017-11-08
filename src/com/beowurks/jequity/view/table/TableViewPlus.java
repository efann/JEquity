/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.view.table;

import com.sun.javafx.scene.control.skin.NestedTableColumnHeader;
import com.sun.javafx.scene.control.skin.TableColumnHeader;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.lang.reflect.Method;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class TableViewPlus extends TableView
{
  // ---------------------------------------------------------------------------------------------------------------------
  public TableViewPlus()
  {
    super();

    this.setEditable(false);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public TableViewPlus(final ObservableList toItems)
  {
    super(toItems);

    this.setEditable(false);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void resizeColumns()
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
  protected void resizeAllColumnsUsingReflection()
  {
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

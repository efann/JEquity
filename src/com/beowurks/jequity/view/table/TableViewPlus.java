/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.table;

import com.beowurks.jequity.utility.Constants;
import com.sun.javafx.scene.control.Properties;
import com.sun.javafx.scene.control.TableColumnBaseHelper;
import com.sun.javafx.scene.control.skin.Utils;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.TableViewSkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class TableViewPlus extends TableView
{
  private boolean flSkinPropertyListenerAdded = false;

  private final StringBuffer foKeySearch = new StringBuffer();

  private Label foStatusMessage = null;

  private Timer foTimerSearchReset = null;

  private static Method foColumnToFitMethod;

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
  protected void updateKeySearchMessage()
  {
    if (this.foStatusMessage == null)
    {
      return;
    }

    final String lcMessage = this.foKeySearch.length() > 0 ? String.format("Finding: %s", this.foKeySearch.toString()) : "";

    if (Platform.isFxApplicationThread())
    {
      this.foStatusMessage.setText(lcMessage);
    }
    else
    {
      Platform.runLater(() ->
          this.foStatusMessage.setText(lcMessage));
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

      if (this.foTimerSearchReset != null)
      {
        this.foTimerSearchReset.cancel();
      }

      // From
      // http://stackoverflow.com/questions/1041675/java-timer
      // and
      // http://stackoverflow.com/questions/10335784/restart-timer-in-java
      this.foTimerSearchReset = new Timer();

      this.foTimerSearchReset.schedule(
          new TimerTask()
          {
            @Override
            public void run()
            {
              TableViewPlus.this.resetKeySearch();
              TableViewPlus.this.updateKeySearchMessage();
            }
          }, Constants.TIMER_TABLE_SEARCH_RESET);


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

      this.updateKeySearchMessage();
    });

    this.setOnKeyPressed((KeyEvent toEvent) ->
    {
      final KeyCode loKey = toEvent.getCode();

      if (loKey.isArrowKey() || loKey.isFunctionKey() || loKey.isNavigationKey() || loKey.isMediaKey())
      {
        this.resetKeySearch();
      }

      this.updateKeySearchMessage();
    });
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void resetKeySearch()
  {
    this.foKeySearch.setLength(0);
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

    final TableHeaderRow loHeaderRow = (TableHeaderRow) this.lookup("TableHeaderRow");
    for (final Node loChild : loHeaderRow.getChildren())
    {
      if (loChild instanceof NestedTableColumnHeader)
      {
        for (final TableColumnHeader loHeader : ((NestedTableColumnHeader) loChild).getColumnHeaders())
        {
          TableViewPlus.resizeColumnToFitContent(this, loSkin, loHeader);
        }
      }
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // Unfortunately, we can't access TableSkinUtils.resizeColumnToFitContent. So I just copied the code, modify slightly,
  // and use what's below.
  private static <T, S> void resizeColumnToFitContent(final TableView<T> toTableView, final TableViewSkinBase toSkin, final TableColumnHeader toHeader)
  {
    final TableColumn<T, S> loTableColumn = (TableColumn) toHeader.getTableColumn();
    final List<?> loItems = toTableView.getItems();
    if (loItems == null || loItems.isEmpty())
    {
      return;
    }

    final Callback/*<TableColumn<T, ?>, TableCell<T,?>>*/ loCellFactory = loTableColumn.getCellFactory();
    if (loCellFactory == null)
    {
      return;
    }

    final TableCell<T, ?> loCell = (TableCell<T, ?>) loCellFactory.call(loTableColumn);
    if (loCell == null)
    {
      return;
    }

    // set this property to tell the TableCell we want to know its actual
    // preferred width, not the width of the associated TableColumnBase
    loCell.getProperties().put(Properties.DEFER_TO_PARENT_PREF_WIDTH, Boolean.TRUE);

    // determine cell padding
    double lnPadding = 10;
    final Node loNode = loCell.getSkin() == null ? null : loCell.getSkin().getNode();
    if (loNode instanceof Region)
    {
      final Region loRegion = (Region) loNode;
      lnPadding = loRegion.snappedLeftInset() + loRegion.snappedRightInset();
    }

    final int lnRows = loItems.size();
    double lnMaxWidth = 0.0;
    for (int lnRow = 0; lnRow < lnRows; lnRow++)
    {
      loCell.updateTableColumn(loTableColumn);
      loCell.updateTableView(toTableView);
      loCell.updateIndex(lnRow);

      if ((loCell.getText() != null && !loCell.getText().isEmpty()) || loCell.getGraphic() != null)
      {
        toSkin.getChildren().add(loCell);
        loCell.applyCss();
        lnMaxWidth = Math.max(lnMaxWidth, loCell.prefWidth(-1));
        toSkin.getChildren().remove(loCell);
      }
    }

    // dispose of the cell to prevent it retaining listeners (see RT-31015)
    loCell.updateIndex(-1);

    // RT-36855 - take into account the column header text / graphic widths.
    // Magic 10 is to allow for sort arrow to appear without text truncation.
    for (final Node loItem : toHeader.getChildrenUnmodifiable())
    {
      if (loItem instanceof Label)
      {
        final Label loLabel = (Label) loItem;
        final double lnHeaderTextWidth = Utils.computeTextWidth(loLabel.getFont(), loTableColumn.getText(), -1);
        final Node loGraphic = loLabel.getGraphic();
        final double lnHeaderGraphicWidth = loGraphic == null ? 0 : loGraphic.prefWidth(-1) + loLabel.getGraphicTextGap();
        final double lnHeaderWidth = lnHeaderTextWidth + lnHeaderGraphicWidth + 10 + toHeader.snappedLeftInset() + toHeader.snappedRightInset();
        lnMaxWidth = Math.max(lnMaxWidth, lnHeaderWidth);

        break;
      }
    }

    // RT-23486
    lnMaxWidth += lnPadding;
    TableColumnBaseHelper.setWidth(loTableColumn, lnMaxWidth);
  }

  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

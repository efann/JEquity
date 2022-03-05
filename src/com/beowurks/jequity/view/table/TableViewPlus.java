/*
 * JEquity
 * Copyright(c) 2008-2022, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.table;

import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class TableViewPlus<S> extends TableView
{
  private boolean flSkinPropertyListenerAdded = false;

  private final StringBuffer foKeySearch = new StringBuffer();

  private Label foStatusMessage = null;

  private Timer foTimerSearchReset = null;

  // Defined in javafx.controls\com\sun\javafx\scene\control\Properties.java
  // which can't seem to be accessed in JDK 17.
  private static final String DEFER_TO_PARENT_PREF_WIDTH = "deferToParentPrefWidth";

  // From TableColumnBase (javafx17\javafx.controls\javafx\scene\control\TableColumnBase.java),
  // which, of course, we can't access
  private static final double DEFAULT_MIN_WIDTH = 10.0F;
  private static final double DEFAULT_MAX_WIDTH = 5000.0F;


  // ---------------------------------------------------------------------------------------------------------------------
  public TableViewPlus()
  {
    super();

    this.setEditable(false);
    this.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

    this.setupKeySearch();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public TableViewPlus(final ObservableList toItems)
  {
    super(toItems);

    this.setEditable(false);
    this.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

    this.setupKeySearch();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  // Well, that was easy.
  public void sort()
  {
    super.sort();

    if ((this.getSelectionModel().getSelectedIndex()) > -1)
    {
      this.scrollTo(this.getSelectionModel().getSelectedItem());
    }
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

    final String lcMessage = this.foKeySearch.length() > 0 ? String.format("Finding: %s", this.foKeySearch) : "";

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
          TableViewPlus.resizeColumnToFitContent(this, loHeader);
        }
      }
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // Unfortunately, we can't access TableColumnHeader.resizeColumnToFitContent (previously TableSkinUtils.resizeColumnToFitContent).
  // So I just copied the code, modified slightly.
  private static <T, S> void resizeColumnToFitContent(final TableView<T> toTableView, final TableColumnHeader toHeader)
  {
    final TableColumn<T, S> loTableColumn = (TableColumn<T, S>) toHeader.getTableColumn();
    final List<?> loItems = toTableView.getItems();
    if (loItems == null || loItems.isEmpty())
    {
      return;
    }

    final Callback loCellFactory = loTableColumn.getCellFactory();
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
    loCell.getProperties().put(TableViewPlus.DEFER_TO_PARENT_PREF_WIDTH, Boolean.TRUE);

    // determine cell padding
    double lnPadding = 18;
    final Node loNode = loCell.getSkin() == null ? null : loCell.getSkin().getNode();
    if (loNode instanceof final Region loRegion)
    {
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
        lnMaxWidth = Math.max(lnMaxWidth, Misc.getStringWidth(loCell.getText(), loCell.getFont()));
      }
    }

    // dispose of the cell to prevent it retaining listeners (see RT-31015)
    loCell.updateIndex(-1);

    // RT-36855 - take into account the column header text / graphic widths.
    // Magic 10 is to allow for sort arrow to appear without text truncation.
    for (final Node loItem : toHeader.getChildrenUnmodifiable())
    {
      if (loItem instanceof final Label loLabel)
      {
        final double lnHeaderTextWidth = Misc.getStringWidth(loLabel.getText(), loLabel.getFont());

        final Node loGraphic = loLabel.getGraphic();
        final double lnHeaderGraphicWidth = loGraphic == null ? 0 : loGraphic.prefWidth(-1) + loLabel.getGraphicTextGap();
        final double lnHeaderWidth = lnHeaderTextWidth + lnHeaderGraphicWidth + 10 + toHeader.snappedLeftInset() + toHeader.snappedRightInset();
        lnMaxWidth = Math.max(lnMaxWidth, lnHeaderWidth);

        break;
      }
    }

    // RT-23486
    // Hmmmmmm.
    // https://bugs.openjdk.java.net/browse/JDK-8113955
    lnMaxWidth += lnPadding;

    // From https://stackoverflow.com/questions/33348757/javafx-8-tablecolumn-setprefwidth-doing-nothing-if-user-manually-resizes-the-co
    loTableColumn.setPrefWidth(lnMaxWidth);
    loTableColumn.setMinWidth(lnMaxWidth);
    loTableColumn.setMaxWidth(lnMaxWidth);

    loTableColumn.setMinWidth(TableViewPlus.DEFAULT_MIN_WIDTH);
    loTableColumn.setMaxWidth(TableViewPlus.DEFAULT_MAX_WIDTH);
  }

  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

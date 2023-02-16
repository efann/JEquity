/*
 * JEquity
 * Copyright(c) 2008-2023, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.table;

import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.view.tableskin.TableViewSkinPlus;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

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

  // ---------------------------------------------------------------------------------------------------------------------
  public TableViewPlus()
  {
    super();

    this.setupTable();
    this.setupKeySearch();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public TableViewPlus(final ObservableList toItems)
  {
    super(toItems);

    this.setupTable();
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
  protected void setupTable()
  {
    this.setEditable(false);
    this.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

    this.setSkin(new TableViewSkinPlus(this));
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
      this.resizeColumnsToFitContent();
    }
    else
    {
      Platform.runLater(this::resizeColumnsToFitContent);
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void resizeColumnsToFitContent()
  {
    final Skin<?> loSkin = this.getSkin();
    // Also covers if null, duh.
    if ((loSkin instanceof TableViewSkinPlus))
    {
      ((TableViewSkinPlus) loSkin).resizeColumnToFit();
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.chart;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// From https://gist.github.com/jewelsea/4681797
public class HoveredNode extends StackPane
{
  public HoveredNode(final double tnValue)
  {
    this.setPrefSize(15, 15);

    final Label loLabel = this.createDataThresholdLabel(tnValue);

    this.setOnMouseEntered(new EventHandler<MouseEvent>()
    {
      @Override
      public void handle(final MouseEvent teMouseEvent)
      {
        HoveredNode.this.getChildren().setAll(loLabel);
        HoveredNode.this.setCursor(Cursor.NONE);
        HoveredNode.this.toFront();
      }
    });
    this.setOnMouseExited(new EventHandler<MouseEvent>()
    {
      @Override
      public void handle(final MouseEvent teMouseEvent)
      {
        HoveredNode.this.getChildren().clear();
        HoveredNode.this.setCursor(Cursor.CROSSHAIR);
      }
    });
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private Label createDataThresholdLabel(final double tnValue)
  {
    final Label loLabel = new Label("$ " + tnValue);
    loLabel.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
    loLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

    loLabel.setTextFill(Color.FIREBRICK);

    loLabel.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
    return (loLabel);
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

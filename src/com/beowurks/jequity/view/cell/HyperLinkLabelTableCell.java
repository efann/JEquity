/*
 * JEquity
 * Copyright(c) 2008-2022, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.cell;

import com.beowurks.jequity.main.Main;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import org.controlsfx.control.HyperlinkLabel;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// From http://code.makery.ch/blog/javafx-8-tableview-cell-renderer/
public class HyperLinkLabelTableCell extends TableCell<Object, String> implements EventHandler<ActionEvent>
{
  // ---------------------------------------------------------------------------------------------------------------------
  public HyperLinkLabelTableCell()
  {
    super();
  }

  // ---------------------------------------------------------------------------------------------------------------------

  @Override
  protected void updateItem(final String tcItem, final boolean tlEmpty)
  {
    super.updateItem(tcItem, tlEmpty);

    if ((tcItem == null) || tlEmpty)
    {
      // Now mimics the other cells' updateItem.
      this.setText(null);
      return;
    }

    final int lnPos = tcItem.toLowerCase().indexOf("http");

    // So if a hyperlink exists, put '[' before http if and ']' at the end
    final String lcItem = (lnPos == -1) ? tcItem : tcItem.substring(0, lnPos) + "[" + tcItem.substring(lnPos) + "]";

    final HyperlinkLabel loLabel = new HyperlinkLabel(lcItem);
    loLabel.setOnAction(this);

    this.setGraphic(loLabel);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void handle(final ActionEvent toEvent)
  {
    final Object loSource = toEvent.getSource();
    if (loSource instanceof final Hyperlink loHyperLink)
    {
      final String lcTextURL = loHyperLink.getText();

      Main.getMainHostServices().showDocument(lcTextURL);
    }
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

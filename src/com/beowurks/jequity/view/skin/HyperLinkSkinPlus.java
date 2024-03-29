/*
 * JEquity
 * Copyright(c) 2008-2023, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.view.skin;

import javafx.animation.FadeTransition;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.skin.HyperlinkSkin;
import javafx.util.Duration;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// From https://stackoverflow.com/questions/49831755/javafx-animation-on-button-hover
// By the way, it's used in com/beowurks/jequity/view/css/Main.css
public class HyperLinkSkinPlus extends HyperlinkSkin
{

  // ---------------------------------------------------------------------------------------------------------------------
  public HyperLinkSkinPlus(final Hyperlink toControl)
  {
    super(toControl);

    final FadeTransition loFadeIn = new FadeTransition(Duration.millis(250));
    loFadeIn.setNode(toControl);
    loFadeIn.setToValue(1.0);

    final FadeTransition loFadeOut = new FadeTransition(Duration.millis(250));
    loFadeOut.setNode(toControl);
    loFadeOut.setToValue(0.70);

    toControl.setOnMouseEntered(e -> loFadeOut.playFromStart());
    toControl.setOnMouseExited(e -> loFadeIn.playFromStart());
  }
  // ---------------------------------------------------------------------------------------------------------------------

}

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

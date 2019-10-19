/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.dao.hibernate.threads;

import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import com.beowurks.jequity.view.textfield.DatePickerPlus;
import com.beowurks.jequity.view.textfield.NumberTextField;
import com.beowurks.jequity.view.textfield.TextFieldPlus;
import javafx.scene.control.Button;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class SingleSymbolInfo
{
  public static final SingleSymbolInfo INSTANCE = new SingleSymbolInfo();

  private String fcInitialSymbol;
  private TextFieldPlus txtSymbol;

  private TextFieldPlus txtDescription;
  private NumberTextField txtPrice;
  private DatePickerPlus txtDate;

  private Button btnSave;

  // ---------------------------------------------------------------------------------------------------------------------
  private SingleSymbolInfo()
  {
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setInformation(final String tcInitialSymbol, final TextFieldPlus toSymbol, final TextFieldPlus toDescription, final NumberTextField toPrice, final DatePickerPlus toDate, final Button toSave)
  {
    this.fcInitialSymbol = tcInitialSymbol.trim();
    this.txtSymbol = toSymbol;

    this.txtDescription = toDescription;
    this.txtPrice = toPrice;
    this.txtDate = toDate;

    this.btnSave = toSave;
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void resetSymbol(final String tcSymbol)
  {
    this.fcInitialSymbol = tcSymbol;
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public TextFieldPlus getSymbol()
  {
    return (this.txtSymbol);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public TextFieldPlus getDescriptionField()
  {
    return (this.txtDescription);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public NumberTextField getPriceField()
  {
    return (this.txtPrice);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public DatePickerPlus getDateField()
  {
    return (this.txtDate);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Button getSaveButton()
  {
    return (this.btnSave);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public boolean runUpdate()
  {
    // If called when
    //   1) no row has been selected on the Financial table
    //   2) the user clicks first on the Symbol field
    //   3) the user moves to another field.
    // then the txtSymbol will be null.
    if (this.txtSymbol == null)
    {
      return (false);
    }

    if (!this.txtSymbol.getText().equals(this.fcInitialSymbol))
    {
      return (true);
    }

    final String lcDescription = this.txtDescription.getText().trim();
    if (lcDescription.equals(Constants.BLANK_DESCRIPTION_FOR_SYMBOL) || lcDescription.isEmpty())
    {
      return (true);
    }

    final double lnPrice = Misc.getDoubleFromTextField(this.txtPrice);
    return (lnPrice == 0.0);

  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

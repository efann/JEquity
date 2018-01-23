/*
 * JEquity
 * Copyright(c) 2008-2018, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.dao.hibernate.threads;

import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import com.beowurks.jequity.view.textfield.NumberTextField;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class SingleSymbolInfo
{
  public static SingleSymbolInfo INSTANCE = new SingleSymbolInfo();

  private String fcInitialSymbol;
  private TextField txtSymbol;

  private TextField txtDescription;
  private NumberTextField txtPrice;
  private DatePicker txtDate;

  private Button btnSave;

  // ---------------------------------------------------------------------------------------------------------------------
  private SingleSymbolInfo()
  {
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setInformation(final String tcInitialSymbol, final TextField toSymbol, final TextField toDescription, final NumberTextField toPrice, final DatePicker toDate, final Button toSave)
  {
    this.fcInitialSymbol = tcInitialSymbol.trim();
    this.txtSymbol = toSymbol;

    this.txtDescription = toDescription;
    this.txtPrice = toPrice;
    this.txtDate = toDate;

    this.btnSave = toSave;
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public TextField getSymbol()
  {
    return (this.txtSymbol);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public TextField getDescriptionField()
  {
    return (this.txtDescription);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public NumberTextField getPriceField()
  {
    return (this.txtPrice);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public DatePicker getDateField()
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

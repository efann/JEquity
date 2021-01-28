/*
 * JEquity
 * Copyright(c) 2008-2021, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.controller;

import com.beowurks.jequity.dao.combobox.IntegerKeyItem;
import com.beowurks.jequity.dao.hibernate.GroupEntity;
import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import com.beowurks.jequity.view.combobox.ComboBoxIntegerKey;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;

import java.util.List;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class ToolbarController
{
  @FXML
  private ComboBoxIntegerKey cboGroup;

  @FXML
  private Button btnUpdate;

  @FXML
  private Button btnRefresh;

  @FXML
  private Label lblSystemMessage;

  // ---------------------------------------------------------------------------------------------------------------------
  public ComboBoxIntegerKey getGroupComboBox()
  {
    return (this.cboGroup);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Button getRefreshButton()
  {
    return (this.btnRefresh);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Button getUpdateButton()
  {
    return (this.btnUpdate);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Integer refreshGroupComboBox()
  {
    final ComboBoxIntegerKey loCombo = this.cboGroup;

    // Save the onAction event then set to null so nothing happens when rebuilding the list.
    final EventHandler<ActionEvent> loActionHandler = loCombo.getOnAction();
    loCombo.setOnAction(null);

    IntegerKeyItem loKeyItemInit = loCombo.getSelectedItem();

    // An error should not occur here; however, I always want the ComboBox action reset afterwards
    // just in case.
    try
    {
      loCombo.getItems().clear();

      final HibernateUtil loHibernate = HibernateUtil.INSTANCE;
      final Session loSession = loHibernate.getSession();

      final NativeQuery loQuery = loSession.createNativeQuery("SELECT * FROM " + loHibernate.getTableGroup())
        .addEntity(GroupEntity.class);

      final List<GroupEntity> loList = loQuery.list();
      for (final GroupEntity loRow : loList)
      {
        final Integer loID = loRow.getGroupID();
        final IntegerKeyItem loKeyItem = new IntegerKeyItem(loID, loRow.getDescription());
        loCombo.getItems().add(loKeyItem);

        if ((loKeyItemInit != null) && (loKeyItem.getKey() == loKeyItemInit.getKey()))
        {
          // The description could have changed.
          loKeyItemInit = loKeyItem;
          loCombo.setValue(loKeyItem);
        }
      }
      loSession.close();

      if (loCombo.getItems().size() == 0)
      {
        loKeyItemInit = null;
      }
      else if ((loCombo.getValue() == null) && (loCombo.getItems().size() > 0))
      {
        loKeyItemInit = loCombo.getItems().get(0);
        loCombo.setValue(loKeyItemInit);
      }

    }
    catch (final HibernateException loErr)
    {
      Misc.showStackTraceInMessage(loErr, "From ToolbarController.refreshGroupComboBox");
    }

    loCombo.setOnAction(loActionHandler);

    return ((loKeyItemInit != null) ? loKeyItemInit.getKey() : Constants.UNINITIALIZED);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setSystemMessage(final String tcMessage)
  {
    this.lblSystemMessage.setText(tcMessage);
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

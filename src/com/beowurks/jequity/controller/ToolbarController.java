/*
 * JEquity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.controller;

import com.beowurks.jequity.dao.combobox.IntegerKeyItem;
import com.beowurks.jequity.dao.hibernate.GroupEntity;
import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.utility.Constants;
import com.beowurks.jequity.utility.Misc;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
  private ComboBox<IntegerKeyItem> cboGroup;

  @FXML
  private Button btnUpdate;

  @FXML
  private Button btnRefresh;

  // ---------------------------------------------------------------------------------------------------------------------
  public ComboBox<IntegerKeyItem> getGroupComboBox()
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

    Integer loInit = Constants.UNINITIALIZED;

    final ComboBox<IntegerKeyItem> loCombo = this.cboGroup;

    final EventHandler<ActionEvent> loActionHandler = loCombo.getOnAction();
    loCombo.setOnAction(null);

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

        if (loInit.intValue() == Constants.UNINITIALIZED)
        {
          loInit = loID;
          loCombo.setValue(loKeyItem);
        }
      }
      loSession.close();
    }
    catch (final HibernateException loErr)
    {
      Misc.showStackTraceInMessage(loErr, "From ToolbarController.refreshGroupComboBox");
    }

    loCombo.setOnAction(loActionHandler);

    return (loInit);
  }


  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

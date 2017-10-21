/*
 * J'EquityFX
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */
package com.beowurks.jequityfx.view.combobox;


import com.beowurks.jequityfx.controller.combobox.IComponentAction;
import com.beowurks.jequityfx.dao.combobox.IntegerKeyItem;
import com.beowurks.jequityfx.dao.hibernate.GroupEntity;
import com.beowurks.jequityfx.dao.hibernate.HibernateUtil;
import com.beowurks.jequityfx.utility.Constants;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;

import java.util.List;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class GroupComboBox extends IntegerKeyComboBox implements IComponentAction
{

  // ---------------------------------------------------------------------------------------------------------------------
  synchronized public Integer refreshComboBox()
  {
    Integer loInit = Constants.UNINITIALIZED;
    this.removeAllItems();

    final HibernateUtil loHibernate = HibernateUtil.INSTANCE;
    final Session loSession = loHibernate.getSession();

    final NativeQuery loQuery = loSession.createNativeQuery("SELECT * FROM " + loHibernate.getTableGroup())
        .addEntity(GroupEntity.class);

    final List<GroupEntity> loList = loQuery.list();
    for (final GroupEntity loRow : loList)
    {
      final Integer loID = loRow.getGroupID();
      final IntegerKeyItem loKeyItem = new IntegerKeyItem(loID, loRow.getDescription());
      this.addItem(loKeyItem);

      if (loInit.compareTo(Constants.UNINITIALIZED) == 0)
      {
        loInit = loID;
      }
    }
    loSession.close();

    return (loInit);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  @Override
  public void performComponentAction(final int tnAction)
  {
    if (tnAction == Constants.COMPONENT_ACTION_UPDATE)
    {
      this.refreshComboBox();
    }
  }
  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

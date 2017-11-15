/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.controller;

import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.view.jasperreports.JRViewerBase;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;

import javax.swing.SwingUtilities;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class ReportController
{
  @FXML
  private SwingNode rptSwingNode;

  private JasperPrint foJPSummary;
  private JRViewerBase foJRViewerSummary;

  // ---------------------------------------------------------------------------------------------------------------------
  public JasperPrint getJasperPrint()
  {
    return (this.foJPSummary);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void refreshReport()
  {
    SwingUtilities.invokeLater(this::generateSummary);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void generateSummary()
  {
    final Session loSession = HibernateUtil.INSTANCE.getSession();

    loSession.doWork(new Work()
    {
      @Override
      public void execute(final Connection toConnection) throws SQLException
      {
        try
        {
          final ReportController loThis = ReportController.this;

          final JasperReport loJasperReport = (JasperReport) JRLoader.loadObject(this.getClass().getResource("/com/beowurks/jequity/view/jasperreports/Summary.jasper"));

          final HashMap<String, Object> loHashMap = new HashMap<>();

          final HibernateUtil loHibernate = HibernateUtil.INSTANCE;

          loHashMap.put("parFinancialTable", loHibernate.getTableFinancial());
          loHashMap.put("parGroupTable", loHibernate.getTableGroup());
          loHashMap.put("parGroupID", loHibernate.getGroupID());

          loThis.foJPSummary = JasperFillManager.fillReport(loJasperReport, loHashMap, toConnection);

          if (loThis.foJRViewerSummary == null)
          {
            loThis.foJRViewerSummary = new JRViewerBase(loThis.foJPSummary);
            loThis.rptSwingNode.setContent(loThis.foJRViewerSummary);
          }
          else
          {
            loThis.foJRViewerSummary.loadReport(loThis.foJPSummary);
          }

        }
        catch (final JRException loErr)
        {
          loErr.printStackTrace();
        }
      }
    });

    loSession.close();
  }

  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

/*
 * JEquity
 * Copyright(c) 2008-2020, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.controller.tab;

import com.beowurks.jequity.dao.hibernate.HibernateUtil;
import com.beowurks.jequity.utility.Misc;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.swing.JRViewer;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;

import javax.swing.SwingUtilities;
import java.sql.Connection;
import java.util.HashMap;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class TabReportController
{
  @FXML
  private SwingNode rptSwingNode;

  private JasperPrint foJPSummary;

  // ---------------------------------------------------------------------------------------------------------------------
  public void refreshReport(final boolean tlShowPrintDialog)
  {
    SwingUtilities.invokeLater(() -> TabReportController.this.generateSummary(tlShowPrintDialog));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void generateSummary(final boolean tlShowPrintDialog)
  {
    final Session loSession = HibernateUtil.INSTANCE.getSession();

    loSession.doWork(new Work()
    {
      @Override
      public void execute(final Connection toConnection)
      {
        try
        {
          final TabReportController loThis = TabReportController.this;

          final JasperReport loJasperReport = (JasperReport) JRLoader.loadObject(this.getClass().getResource("/com/beowurks/jequity/view/jasperreports/Summary.jasper"));

          final HashMap<String, Object> loHashMap = new HashMap<>();

          final HibernateUtil loHibernate = HibernateUtil.INSTANCE;

          loHashMap.put("parFinancialTable", loHibernate.getTableFinancial());
          loHashMap.put("parGroupTable", loHibernate.getTableGroup());
          loHashMap.put("parGroupID", loHibernate.getGroupID());

          loThis.foJPSummary = JasperFillManager.fillReport(loJasperReport, loHashMap, toConnection);

          // No longer a loJRViewerSummary.loadReport(loThis.foJPSummary) function
          final JRViewer loJRViewerSummary = new JRViewer(loThis.foJPSummary);
          loThis.rptSwingNode.setContent(loJRViewerSummary);

          if (tlShowPrintDialog)
          {
            JasperPrintManager.printReport(loThis.foJPSummary, true);
          }
        }
        catch (final JRException loErr)
        {
          Misc.showStackTraceInMessage(loErr, "Error in Report");
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

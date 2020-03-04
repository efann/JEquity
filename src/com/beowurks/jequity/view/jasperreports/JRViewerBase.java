/*
 * JEquity
 * Copyright(c) 2008-2020, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */
package com.beowurks.jequity.view.jasperreports;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JRViewer;

// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
public class JRViewerBase extends JRViewer
{

  // Gets rid of the following error:
  // serializable class has no definition of serialVersionUID
  private static final long serialVersionUID = 1L;

  // -----------------------------------------------------------------------------
  public JRViewerBase(final JasperPrint toJasperPrint) throws JRException
  {
    super(toJasperPrint);

    this.tlbToolBar.remove(this.btnReload);
    this.tlbToolBar.remove(this.btnPrint);
  }

  // -----------------------------------------------------------------------------
  @Override
  public void loadReport(final JasperPrint toJasperPrint)
  {
    super.loadReport(toJasperPrint);

    this.refreshPage();
  }
  // -----------------------------------------------------------------------------
}
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------

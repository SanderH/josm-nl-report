// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.nl_pdok_report.AbstractReport;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportDataListener;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportLayer;
import org.openstreetmap.josm.plugins.nl_pdok_report.gui.ReportNewDialog;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Map mode to add a new report. Listens for a mouse click and then prompts the user for text and adds a report to the
 * report layer
 */
public class ReportNewEditAction extends JosmAction implements ReportDataListener {
  private static final long serialVersionUID = -6510896275208104350L;

  /**
   * Construct a new map mode.
   * 
   * @param data
   *          Note data container. Must not be null
   * @since 11713
   */
  public ReportNewEditAction() {
    super(
      tr("Edit selected Report"), new ImageProvider("markers", "report-add.svg"), tr("Edit the currently selected Report"), null, false, "reportEdit", true
    );
  }

  @Override
  public void reportsAdded() {
    // Nothing
  }

  @Override
  public void reportsRemoved() {
    // Nothing
  }

  @Override
  protected boolean listenToSelectionChange() {
    return false;
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    ReportNewDialog.editReport();
  }
  
  @Override
  public void selectedReportChanged(AbstractReport oldReport, AbstractReport newReport) {
    if (oldReport == null && newReport != null) {
      setEnabled(true);
    } else if (oldReport != null && newReport == null) {
      setEnabled(false);
    }
    setEnabled(ReportLayer.hasInstance() && ReportLayer.getInstance().getData().getSelectedReport() != null);
  }

  @Override
  protected void updateEnabledState() {
    super.updateEnabledState();
    setEnabled(ReportLayer.hasInstance() && ReportLayer.getInstance().getData().getSelectedReport() != null);
  }
}

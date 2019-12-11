// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.nl_pdok_report.AbstractReport;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportDataListener;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportLayer;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportPlugin;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.ImageProvider.ImageSizes;

/**
 * Zooms to the currently selected image.
 *
 * @author nokutu
 *
 */
public class ReportZoomAction extends JosmAction implements ReportDataListener {
  private static final long serialVersionUID = 1260243082695786290L;

  /**
   * Main constructor.
   */
  public ReportZoomAction() {
    super(
      tr("Zoom to selected report"), new ImageProvider(ReportPlugin.LOGO).setSize(ImageSizes.DEFAULT), tr("Zoom to the currently selected Report"), null, false, "reportZoom", true
    );
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    if (ReportLayer.getInstance().getData().getSelectedReport() == null) {
      throw new IllegalStateException();
    }
    MainApplication.getMap().mapView.zoomTo(ReportLayer.getInstance().getData().getSelectedReport().getMovingLatLon());
    MainApplication.getMap().mapView.zoomIn();
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

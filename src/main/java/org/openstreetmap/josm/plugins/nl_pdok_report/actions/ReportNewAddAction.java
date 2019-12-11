// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.gui.util.KeyPressReleaseListener;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportData;
import org.openstreetmap.josm.plugins.nl_pdok_report.gui.ReportNewDialog;
import org.openstreetmap.josm.plugins.nl_pdok_report.gui.ReportPropertiesDialog;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;

/**
 * Map mode to add a new report. Listens for a mouse click and then prompts the user for text and adds a report to the
 * report layer
 */
public class ReportNewAddAction extends MapMode implements KeyPressReleaseListener {
  private static final long serialVersionUID = -1463408052513101961L;
  private final transient ReportData reportData;

  /**
   * Construct a new map mode.
   * 
   * @param data
   *          Note data container. Must not be null
   * @since 11713
   */
  public ReportNewAddAction(ReportData data) {
    super(
      tr("Add a new PDOK Report"), "report-new.svg", tr("Add report mode"), ImageProvider.getCursor("crosshair", "create-report")
    );
    CheckParameterUtil.ensureParameterNotNull(data, "data");
    reportData = data;
  }

  @Override
  public String getModeHelpText() {
    return tr("Click the location where you wish to create a new report");
  }

  @Override
  public void enterMode() {
    super.enterMode();
    MapFrame map = MainApplication.getMap();
    map.mapView.addMouseListener(this);
    map.keyDetector.addKeyListener(this);
  }

  @Override
  public void exitMode() {
    super.exitMode();
    MapFrame map = MainApplication.getMap();
    map.mapView.removeMouseListener(this);
    map.keyDetector.removeKeyListener(this);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if (!SwingUtilities.isLeftMouseButton(e)) {
      // allow to pan without distraction
      return;
    }
    MapFrame map = MainApplication.getMap();
    map.selectMapMode(map.mapModeSelect);

    ReportPropertiesDialog dialog = new ReportPropertiesDialog(
      MainApplication.getMainFrame(), tr("Create a new report"), tr("Create report")
    );
    dialog.showReportPropertiesDialog(
      tr("Enter a detailed description to create a report"), new ImageProvider("dialogs", "report-new").setSize(24, 24).get()
    );

    if (dialog.getValue() != 1) {
      Logging.debug("User aborted report creation");
      return;
    }
    String input = dialog.getInputText();
    if (input != null && !input.isEmpty()) {
      LatLon latlon = map.mapView.getLatLon(e.getPoint().x, e.getPoint().y);
      reportData.createReport(latlon, input);
      ReportNewDialog.getInstance().setUploadPending(false);
    } else {
      new Notification(tr("You must enter a description to create a new report")).setIcon(JOptionPane.WARNING_MESSAGE)
        .show();
    }
  }

  @Override
  public void doKeyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
      MapFrame map = MainApplication.getMap();
      map.selectMapMode(map.mapModeSelect);
    }
  }

  @Override
  public void doKeyReleased(KeyEvent e) {
    // Do nothing
  }

}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.mode;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.plugins.nl_pdok_report.AbstractReport;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportLayer;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportNewBAG;

/**
 * In this mode the user can join pictures to make sequences or unjoin them.
 *
 * @author nokutu
 *
 */
public class JoinMode extends AbstractMode {

  private ReportNewBAG lastClick;
  private MouseEvent lastPos;

  /**
   * Main constructor.
   */
  public JoinMode() {
    this.cursor = Cursor.CROSSHAIR_CURSOR;
  }

  @Override
  public void mousePressed(MouseEvent e) {
    final AbstractReport highlighted = ReportLayer.getInstance().getData().getHighlightedReport();
    if (highlighted == null) {
      return;
    }
    if (this.lastClick == null && highlighted instanceof ReportNewBAG) {
      this.lastClick = (ReportNewBAG) highlighted;
    }
    ReportLayer.invalidateInstance();
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    this.lastPos = e;
    if (!(MainApplication.getLayerManager().getActiveLayer() instanceof ReportLayer))
      return;
    AbstractReport closestTemp = getClosest(e.getPoint());
    ReportLayer.getInstance().getData().setHighlightedReport(closestTemp);
    ReportLayer.invalidateInstance();
  }

  @Override
  public void paint(Graphics2D g, MapView mv, Bounds box) {
    if (this.lastClick != null) {
      g.setColor(Color.WHITE);
      Point p0 = mv.getPoint(this.lastClick.getMovingLatLon());
      Point p1 = this.lastPos.getPoint();
      g.drawLine(p0.x, p0.y, p1.x, p1.y);
    }
  }

  @Override
  public String toString() {
    return tr("Join mode");
  }
}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.mode;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Objects;

import javax.swing.SwingUtilities;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.nl_pdok_report.AbstractReport;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportBAG;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportData;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportLayer;
import org.openstreetmap.josm.plugins.nl_pdok_report.history.ReportRecord;
import org.openstreetmap.josm.plugins.nl_pdok_report.history.commands.CommandMove;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportProperties;

/**
 * Handles the input event related with the layer. Mainly clicks.
 *
 * @author SanderH
 */
public class SelectMode extends AbstractMode {
  private AbstractReport closest;
  private AbstractReport lastClicked;
  private final ReportRecord record;
  private boolean nothingHighlighted;
  private boolean imageHighlighted;

  /**
   * Main constructor.
   */
  public SelectMode() {
    this.record = ReportRecord.getInstance();
  }

  @Override
  public void mousePressed(MouseEvent e) {
    if (e.getButton() != MouseEvent.BUTTON1) {
      return;
    }
    final AbstractReport closest = getClosest(e.getPoint());
    if (closest == null) {
      ReportLayer.getInstance().getData().setSelectedReport(null);
      return;
    }

    if ((MainApplication.getLayerManager().getActiveLayer() instanceof ReportLayer) || ReportProperties.SELECT_FROM_OTHER_LAYER.get()) {
      if (e.getClickCount() == 2) { // Double click

      } else if (e.getModifiersEx() == (InputEvent.BUTTON1_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)) { // ctrl + click
        ReportLayer.getInstance().getData().addMultiSelectedReport(closest);
      } else { // click
        ReportLayer.getInstance().getData().setSelectedReport(closest);
      }
    } else { // If the ReportLayer is NOT selected
      if (MainApplication.getMap().mapMode == MainApplication.getMap().mapModeSelect) {
        ReportLayer.getInstance().getData().setSelectedReport(closest);
      }
    }
    this.lastClicked = this.closest;
    this.closest = closest;
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    AbstractReport highlightImg = ReportLayer.getInstance().getData().getHighlightedReport();
    if (MainApplication.getLayerManager().getActiveLayer() == ReportLayer.getInstance()
      && SwingUtilities.isLeftMouseButton(e) && highlightImg != null && highlightImg.getLatLon() != null) {
      Point highlightImgPoint = MainApplication.getMap().mapView.getPoint(highlightImg.getTempLatLon());
      if (!e.isShiftDown()) { // move
        LatLon eventLatLon = MainApplication.getMap().mapView.getLatLon(e.getX(), e.getY());
        LatLon imgLatLon = MainApplication.getMap().mapView
          .getLatLon(highlightImgPoint.getX(), highlightImgPoint.getY());
        ReportLayer.getInstance().getData().getMultiSelectedReports().parallelStream().filter(
          img -> !(img instanceof ReportBAG)
        ).forEach(img -> img.move(eventLatLon.getX() - imgLatLon.getX(), eventLatLon.getY() - imgLatLon.getY()));
      }
      ReportLayer.invalidateInstance();
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    final ReportData data = ReportLayer.getInstance().getData();
    if (data.getSelectedReport() == null) {
      return;
    }
    if (!Objects.equals(data.getSelectedReport().getTempLatLon(), data.getSelectedReport().getMovingLatLon())) {
      LatLon from = data.getSelectedReport().getTempLatLon();
      LatLon to = data.getSelectedReport().getMovingLatLon();
      record
        .addCommand(new CommandMove(data.getMultiSelectedReports(), to.getX() - from.getX(), to.getY() - from.getY()));
    }
    data.getMultiSelectedReports().parallelStream().filter(Objects::nonNull).forEach(AbstractReport::stopMoving);
    ReportLayer.invalidateInstance();
  }

  /**
   * Checks if the mouse is over pictures.
   */
  @Override
  public void mouseMoved(MouseEvent e) {
    if (MainApplication.getLayerManager().getActiveLayer() instanceof OsmDataLayer
      && MainApplication.getMap().mapMode != MainApplication.getMap().mapModeSelect) {
      return;
    }

    AbstractReport closestTemp = getClosest(e.getPoint());

    final OsmDataLayer editLayer = MainApplication.getLayerManager().getEditLayer();
    if (editLayer != null) {
      if (closestTemp != null && !this.imageHighlighted) {
        if (MainApplication.getMap().mapMode != null) {
          MainApplication.getMap().mapMode.putValue("active", Boolean.FALSE);
        }
        imageHighlighted = true;
      } else if (closestTemp == null && imageHighlighted && nothingHighlighted) {
        if (MainApplication.getMap().mapMode != null) {
          MainApplication.getMap().mapMode.putValue("active", Boolean.TRUE);
        }
        nothingHighlighted = false;
      } else if (imageHighlighted && !nothingHighlighted && editLayer.data != null) {
        for (OsmPrimitive primivitive : MainApplication.getLayerManager().getEditLayer().data.allPrimitives()) {
          primivitive.setHighlighted(false);
        }
        imageHighlighted = false;
        nothingHighlighted = true;
      }
    }

    if (ReportLayer.getInstance().getData().getHighlightedReport() != closestTemp && closestTemp != null) {
      ReportLayer.getInstance().getData().setHighlightedReport(closestTemp);
      // ReportMainDialog.getInstance().setReport(closestTemp);
      // ReportMainDialog.getInstance().updateReport(false);
    } else if (ReportLayer.getInstance().getData().getHighlightedReport() != closestTemp && closestTemp == null) {
      ReportLayer.getInstance().getData().setHighlightedReport(null);
      // ReportMainDialog.getInstance().setReport(ReportLayer.getInstance().getData().getSelectedReport());
      // ReportMainDialog.getInstance().updateReport();
    }
    ReportLayer.invalidateInstance();
  }

  @Override
  public void paint(Graphics2D g, MapView mv, Bounds box) {
  }

  @Override
  public String toString() {
    return tr("Select mode");
  }
}

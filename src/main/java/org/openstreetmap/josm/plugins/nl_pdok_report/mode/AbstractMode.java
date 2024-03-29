// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.mode;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.util.Calendar;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.NavigatableComponent.ZoomChangeListener;
import org.openstreetmap.josm.plugins.nl_pdok_report.AbstractReport;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportLayer;
import org.openstreetmap.josm.plugins.nl_pdok_report.io.download.ReportDownloader;

/**
 * Superclass for all the mode of the {@link ReportLayer}.
 *
 * @author SanderH
 * @see ReportLayer
 */
public abstract class AbstractMode extends MouseAdapter implements ZoomChangeListener {

  private static final int DOWNLOAD_COOLDOWN = 2000;
  private static SemiautomaticThread semiautomaticThread = new SemiautomaticThread();

  /**
   * Cursor that should become active when this mode is activated.
   */
  public int cursor = Cursor.DEFAULT_CURSOR;

  protected AbstractReport getClosest(Point clickPoint) {
    double snapDistance = 10;
    double minDistance = Double.MAX_VALUE;
    AbstractReport closest = null;
    for (AbstractReport image : ReportLayer.getInstance().getData().getReports()) {
      Point imagePoint = MainApplication.getMap().mapView.getPoint(image.getMovingLatLon());
      imagePoint.setLocation(imagePoint.getX(), imagePoint.getY());
      double dist = clickPoint.distanceSq(imagePoint);
      if (minDistance > dist && clickPoint.distance(imagePoint) < snapDistance && image.isVisible()) {
        minDistance = dist;
        closest = image;
      }
    }
    return closest;
  }

  /**
   * Paint the dataset using the engine set.
   *
   * @param g
   *          {@link Graphics2D} used for painting
   * @param mv
   *          The object that can translate GeoPoints to screen coordinates.
   * @param box
   *          Area where painting is going to be performed
   */
  public abstract void paint(Graphics2D g, MapView mv, Bounds box);

  @Override
  public void zoomChanged() {
    if (ReportDownloader.getMode() == ReportDownloader.DOWNLOAD_MODE.VISIBLE_AREA) {
      if (!semiautomaticThread.isAlive())
        semiautomaticThread.start();
      semiautomaticThread.moved();
    }
  }

  /**
   * Resets the semiautomatic mode thread.
   */
  public static void resetThread() {
    semiautomaticThread.interrupt();
    semiautomaticThread = new SemiautomaticThread();
  }

  private static class SemiautomaticThread extends Thread {

    /** If in semiautomatic mode, the last Epoch time when there was a download */
    private long lastDownload;

    private boolean moved;

    @Override
    public void run() {
      while (true) {
        if (this.moved && Calendar.getInstance().getTimeInMillis() - this.lastDownload >= DOWNLOAD_COOLDOWN) {
          this.lastDownload = Calendar.getInstance().getTimeInMillis();
          ReportDownloader.downloadVisibleArea();
          this.moved = false;
          ReportLayer.invalidateInstance();
        }
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          return;
        }
      }
    }

    public void moved() {
      this.moved = true;
    }
  }
}

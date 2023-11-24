// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.io.download;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportLayer;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportPlugin;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportProperties;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.Logging;

/**
 * Class that concentrates all the ways of downloading of the plugin. All the download petitions will be managed one by
 * one.
 *
 * @author SanderH
 */
public final class ReportDownloader {

  /** Possible download modes. */
  public enum DOWNLOAD_MODE {
    // i18n: download mode for Feedback
    VISIBLE_AREA("visibleArea", I18n.tr("everything in the visible area")),
    // i18n: download mode for Feedback
    OSM_AREA("osmArea", I18n.tr("areas with downloaded OSM-data")),
    // i18n: download mode for Feedback
    MANUAL_ONLY("manualOnly", I18n.tr("only when manually requested"));

    public final static DOWNLOAD_MODE DEFAULT = OSM_AREA;

    private final String prefId;
    private final String label;

    DOWNLOAD_MODE(String prefId, String label) {
      this.prefId = prefId;
      this.label = label;
    }

    /**
     * @return the ID that is used to represent this download mode in the JOSM preferences
     */
    public String getPrefId() {
      return prefId;
    }

    /**
     * @return the (internationalized) label describing this download mode
     */
    public String getLabel() {
      return label;
    }

    public static DOWNLOAD_MODE fromPrefId(String prefId) {
      for (DOWNLOAD_MODE mode : DOWNLOAD_MODE.values()) {
        if (mode.getPrefId().equals(prefId)) {
          return mode;
        }
      }
      return DEFAULT;
    }

    public static DOWNLOAD_MODE fromLabel(String label) {
      for (DOWNLOAD_MODE mode : DOWNLOAD_MODE.values()) {
        if (mode.getLabel().equals(label)) {
          return mode;
        }
      }
      return DEFAULT;
    }
  }

  /** Executor that will run the petitions. */
  private static ThreadPoolExecutor executor = new ThreadPoolExecutor(
    3, 5, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100), new ThreadPoolExecutor.DiscardPolicy()
  );

  /**
   * Indicates whether the last download request has been rejected because it requested an area that was too big. If
   * true, the last download has been rejected, if false, it was executed.
   */
  private static boolean stoppedDownload;

  private ReportDownloader() {
    // Private constructor to avoid instantiation
  }

  /**
   * Gets all the images in a square. It downloads all the images of all the sequences that pass through the given
   * rectangle.
   *
   * @param minLatLon
   *          The minimum latitude and longitude of the rectangle.
   * @param maxLatLon
   *          The maximum latitude and longitude of the rectangle
   */
  public static void getFeedback(LatLon minLatLon, LatLon maxLatLon) {
    if (minLatLon == null || maxLatLon == null) {
      throw new IllegalArgumentException();
    }
    getFeedback(new Bounds(minLatLon, maxLatLon));
  }

  /**
   * Gets the images within the given bounds.
   *
   * @param bounds
   *          A {@link Bounds} object containing the area to be downloaded.
   */
  public static void getFeedback(Bounds bounds) {
    run(new ReportSquareDownloadRunnable(bounds));
  }

  private static void run(Runnable t) {
    executor.execute(t);
  }

  /**
   * Returns the current download mode.
   *
   * @return the currently enabled {@link DOWNLOAD_MODE}
   */
  public static DOWNLOAD_MODE getMode() {
    return DOWNLOAD_MODE.fromPrefId(ReportProperties.DOWNLOAD_MODE.get());
  }

  /**
   * If some part of the current view has not been downloaded, it is downloaded.
   */
  public static void downloadVisibleArea() {
    final MapView mv = ReportPlugin.getMapView();
    if (mv != null) {
      final Bounds view = mv.getRealBounds();
      // 2019-11-27: always download, there may be new/updated data...
      //if (!isViewDownloaded(view)) {
        ReportLayer.getInstance().getData().getBounds().add(view);
        getFeedback(view);
      //}
    }
  }

  private static boolean isViewDownloaded(Bounds view) {
    int n = 15;
    boolean[][] inside = new boolean[n][n];
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        if (isInBounds(
          new LatLon(
            view.getMinLat() + (view.getMaxLat() - view.getMinLat()) * ((double) i / n),
            view.getMinLon() + (view.getMaxLon() - view.getMinLon()) * ((double) j / n)
          )
        )) {
          inside[i][j] = true;
        }
      }
    }
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        if (!inside[i][j])
          return false;
      }
    }
    return true;
  }

  /**
   * Checks if the given {@link LatLon} object lies inside the bounds of the image.
   *
   * @param latlon
   *          The coordinates to check.
   *
   * @return true if it lies inside the bounds; false otherwise;
   */
  private static boolean isInBounds(LatLon latlon) {
    return ReportLayer.getInstance().getData().getBounds().parallelStream().anyMatch(b -> b.contains(latlon));
  }

  /**
   * Downloads all images of the area covered by the OSM data.
   */
  public static void downloadOSMArea() {
    if (MainApplication.getLayerManager().getEditLayer() == null) {
      return;
    }
    MainApplication.getLayerManager().getEditLayer().data.getDataSourceBounds().stream()
      .filter(bounds -> !ReportLayer.getInstance().getData().getBounds().contains(bounds)).forEach(bounds -> {
        ReportLayer.getInstance().getData().getBounds().add(bounds);
        ReportDownloader.getFeedback(bounds.getMin(), bounds.getMax());
      });
  }

  /**
   * Stops all running threads.
   */
  public static void stopAll() {
    executor.shutdownNow();
    try {
      executor.awaitTermination(30, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Logging.error(e);
    }
    executor = new ThreadPoolExecutor(
      3, 5, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100), new ThreadPoolExecutor.DiscardPolicy()
    );
  }
}

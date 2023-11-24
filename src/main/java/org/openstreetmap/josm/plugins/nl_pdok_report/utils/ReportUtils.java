// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.utils;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.nl_pdok_report.AbstractReport;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportLayer;
import org.openstreetmap.josm.tools.I18n;

/**
 * Set of utilities.
 *
 * @author SanderH
 */
public final class ReportUtils {

  private static final double MIN_ZOOM_SQUARE_SIDE = 0.002;

  private ReportUtils() {
    // Private constructor to avoid instantiation
  }

  /**
   * Open the default browser in the given URL.
   *
   * @param url
   *          The (not-null) URL that is going to be opened.
   * @throws IOException
   *           when the URL could not be opened
   */
  public static void browse(URL url) throws IOException {
    if (url == null) {
      throw new IllegalArgumentException();
    }
    Desktop desktop = Desktop.getDesktop();
    if (desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(url.toURI());
      } catch (URISyntaxException e1) {
        throw new IOException(e1);
      }
    } else {
      Runtime runtime = Runtime.getRuntime();
      runtime.exec("xdg-open " + url);
    }
  }
  
  /**
   * Returns the current date formatted as EXIF timestamp. As timezone the default timezone of the JVM is used
   * ({@link java.util.TimeZone#getDefault()}).
   *
   * @return A {@code String} object containing the current date.
   */
  public static String currentDate() {
    return new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.UK).format(Calendar.getInstance().getTime());
  }

  /**
   * Returns current time in Epoch format (milliseconds since 1970-01-01T00:00:00+0000)
   *
   * @return The current date in Epoch format.
   */
  public static long currentTime() {
    return Calendar.getInstance().getTimeInMillis();
  }

  /**
   * Parses a string with a given format and returns the Epoch time. If no timezone information is given, the default
   * timezone of the JVM is used ({@link java.util.TimeZone#getDefault()}).
   *
   * @param date
   *          The string containing the date.
   * @param format
   *          The format of the date.
   * @return The date in Epoch format.
   * @throws ParseException
   *           if the date cannot be parsed with the given format
   */
  public static long getEpoch(String date, String format) throws ParseException {
    return new SimpleDateFormat(format, Locale.UK).parse(date).getTime();
  }

  /**
   * Calculates the decimal degree-value from a degree value given in degrees-minutes-seconds-format
   *
   * @param degMinSec
   *          an array of length 3, the values in there are (in this order) degrees, minutes and seconds
   * @param ref
   *          the latitude or longitude reference determining if the given value is:
   *          <ul>
   *          <li>north ( {@link GpsTagConstants#GPS_TAG_GPS_LATITUDE_REF_VALUE_NORTH}) or south (
   *          {@link GpsTagConstants#GPS_TAG_GPS_LATITUDE_REF_VALUE_SOUTH}) of the equator</li>
   *          <li>east ( {@link GpsTagConstants#GPS_TAG_GPS_LONGITUDE_REF_VALUE_EAST}) or west
   *          ({@link GpsTagConstants#GPS_TAG_GPS_LONGITUDE_REF_VALUE_WEST} ) of the equator</li>
   *          </ul>
   * @return the decimal degree-value for the given input, negative when west of 0-meridian or south of equator,
   *         positive otherwise
   * @throws IllegalArgumentException
   *           if {@code degMinSec} doesn't have length 3 or if {@code ref} is not one of the values mentioned above
   */
  public static double degMinSecToDouble(RationalNumber[] degMinSec, String ref) {
    if (degMinSec == null || degMinSec.length != 3) {
      throw new IllegalArgumentException("Array's length must be 3.");
    }
    for (int i = 0; i < 3; i++) {
      if (degMinSec[i] == null)
        throw new IllegalArgumentException("Null value in array.");
    }

    switch (ref) {
    case GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF_VALUE_NORTH:
    case GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF_VALUE_SOUTH:
    case GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF_VALUE_EAST:
    case GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF_VALUE_WEST:
      break;
    default:
      throw new IllegalArgumentException("Invalid ref.");
    }

    double result = degMinSec[0].doubleValue(); // degrees
    result += degMinSec[1].doubleValue() / 60; // minutes
    result += degMinSec[2].doubleValue() / 3600; // seconds

    if (GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF_VALUE_SOUTH.equals(ref)
      || GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF_VALUE_WEST.equals(ref)) {
      result *= -1;
    }

    result = 360 * ((result + 180) / 360 - Math.floor((result + 180) / 360)) - 180;
    return result;
  }

  /**
   * Zooms to fit all the {@link AbstractReport} objects stored in the database.
   */
  public static void showAllReports() {
    showReports(ReportLayer.getInstance().getData().getReports(), false);
  }

  /**
   * Zooms to fit all the given {@link AbstractReport} objects.
   *
   * @param reports
   *          The reports your are zooming to.
   * @param select
   *          Whether the added reports must be selected or not.
   */
  public static void showReports(final Set<AbstractReport> reports, final boolean select) {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(() -> showReports(reports, select));
    } else {
      Bounds zoomBounds;
      if (reports.isEmpty()) {
        zoomBounds = new Bounds(new LatLon(0, 0));
      } else {
        zoomBounds = new Bounds(reports.iterator().next().getMovingLatLon());
        for (AbstractReport report : reports) {
          zoomBounds.extend(report.getMovingLatLon());
        }
      }

      // The zoom rectangle must have a minimum size.
      double latExtent = Math.max(zoomBounds.getMaxLat() - zoomBounds.getMinLat(), MIN_ZOOM_SQUARE_SIDE);
      double lonExtent = Math.max(zoomBounds.getMaxLon() - zoomBounds.getMinLon(), MIN_ZOOM_SQUARE_SIDE);
      zoomBounds = new Bounds(zoomBounds.getCenter(), latExtent, lonExtent);

      MainApplication.getMap().mapView.zoomTo(zoomBounds);
      ReportLayer.getInstance().getData().setSelectedReport(null);
      if (select) {
        ReportLayer.getInstance().getData().addMultiSelectedReport(reports);
      }
      ReportLayer.invalidateInstance();
    }

  }

  /**
   * Updates the help text at the bottom of the window.
   */
  public static void updateHelpText() {
    if (MainApplication.getMap() == null || MainApplication.getMap().statusLine == null) {
      return;
    }
    StringBuilder ret = new StringBuilder();
    if (PluginState.isDownloading()) {
      ret.append(I18n.tr("Downloading reports"));
    } else if (ReportLayer.hasInstance() && !ReportLayer.getInstance().getData().getReports().isEmpty()) {
      ret.append(I18n.tr("Total reports: {0}", ReportLayer.getInstance().getToolTipText()));
    } else if (PluginState.isSubmittingChangeset()) {
      ret.append(I18n.tr("Submitting reports"));
    } else {
      ret.append(I18n.tr("No reports found"));
    }
    if (ReportLayer.hasInstance() && ReportLayer.getInstance().mode != null) {
      ret.append(" — ").append(I18n.tr(ReportLayer.getInstance().mode.toString()));
    }
    if (PluginState.isUploading()) {
      ret.append(" — ").append(PluginState.getUploadString());
    }
    MainApplication.getMap().statusLine.setHelpText(ret.toString());
  }
}

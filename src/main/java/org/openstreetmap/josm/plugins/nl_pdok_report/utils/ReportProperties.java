// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.utils;

import java.awt.Color;

import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.data.preferences.IntegerProperty;
import org.openstreetmap.josm.data.preferences.NamedColorProperty;
import org.openstreetmap.josm.data.preferences.StringProperty;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.nl_pdok_report.io.download.ReportDownloader;

public final class ReportProperties {
  public static final StringProperty API_KEY = new StringProperty("nl.bag.pdok.terugmeldapi.api.key", null);
  public static final StringProperty API_URL = new StringProperty("nl.bag.pdok.terugmeldapi.api.url", "https://api.kadaster.nl/tms/v1/terugmeldingen");
  public static final StringProperty USER_EMAIL = new StringProperty("nl.bag.pdok.terugmeldapi.user.email", "");
  public static final StringProperty USER_ORGANISATION = new StringProperty("nl.bag.pdok.terugmeldapi.user.organisation", "OpenStreetMap contributors");
  public static final BooleanProperty SELECT_FROM_OTHER_LAYER = new BooleanProperty("nl.bag.pdok.terugmeldapi.select_from_other_layer", false);

  public static final BooleanProperty SHOW_STATUS_REJECTED = new BooleanProperty("nl.bag.pdok.terugmeldapi.show-status-rejected", true);
  public static final BooleanProperty SHOW_STATUS_CLOSED = new BooleanProperty("nl.bag.pdok.terugmeldapi.show-status-closed", true);

  public static final StringProperty DATE_FORMAT = new StringProperty("nl.bag.pdok.terugmeldapi.dateformat", "yyyy-MM-dd - HH:mm:ss (z)");

  public static final BooleanProperty DEVELOPER = new BooleanProperty("nl.bag.pdok.terugmeldapi.developer", false);
  public static final BooleanProperty USE_FIDDLER = new BooleanProperty("nl.bag.pdok.terugmeldapi.use_fiddler", false);
  public static final BooleanProperty USE_ACT_API = new BooleanProperty("nl.bag.pdok.terugmeldapi.api.use_act", false);
  public static final StringProperty API_KEY_ACT = new StringProperty("nl.bag.pdok.terugmeldapi.api.act_key", null);
  public static final StringProperty API_URL_ACT = new StringProperty("nl.bag.pdok.terugmeldapi.api.act_url", "https://api.acceptatie.kadaster.nl/tms/v1/terugmeldingen");

  public static final IntegerProperty MAPOBJECT_ICON_SIZE = new IntegerProperty("nl.bag.pdok.terugmeldapi.mapobjects.iconsize", 32);
  public static final StringProperty DOWNLOAD_MODE = new StringProperty("nl.bag.pdok.terugmeldapi.download-mode", ReportDownloader.DOWNLOAD_MODE.DEFAULT.getPrefId());
  public static final StringProperty START_DIR = new StringProperty("nl.bag.pdok.terugmeldapi.start-directory", System.getProperty("user.home"));

  /**
   * @see OsmDataLayer#PROPERTY_BACKGROUND_COLOR
   */
  public static final NamedColorProperty BACKGROUND = new NamedColorProperty("background", Color.BLACK);
  /**
   * @see OsmDataLayer#PROPERTY_OUTSIDE_COLOR
   */
  public static final NamedColorProperty OUTSIDE_DOWNLOADED_AREA = new NamedColorProperty(
    "outside downloaded area", Color.YELLOW
  );

  private ReportProperties() {
    // Private constructor to avoid instantiation
  }
}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.utils;

import java.awt.Color;

import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.data.preferences.DoubleProperty;
import org.openstreetmap.josm.data.preferences.IntegerProperty;
import org.openstreetmap.josm.data.preferences.NamedColorProperty;
import org.openstreetmap.josm.data.preferences.StringProperty;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.nl_pdok_report.io.download.ReportDownloader;
import org.openstreetmap.josm.tools.I18n;

public final class ReportProperties {

  public enum REPORT_API {
    PDOK_PRODUCTION("pdokProduction", I18n.tr("PDOK Production"), true, true),
    PDOK_ACCEPTANCE("pdokAcceptance", I18n.tr("PDOK Acceptance"), true, false),
    PROXY_PRODUCTION("proxyProduction", I18n.tr("Proxy Production"), false, true),
    PROXY_ACCEPTANCE("proxyAcceptance", I18n.tr("Proxy Acceptance"), false, false);
    

    public final static REPORT_API DEFAULT = PDOK_PRODUCTION;

    private final String prefId;
    private final String label;
    private final Boolean needsKey;
    private final Boolean isProduction;

    REPORT_API(String prefId, String label, Boolean needsKey, Boolean isProduction) {
      this.prefId = prefId;
      this.label = label;
      this.needsKey = needsKey;
      this.isProduction = isProduction;
    }

    /**
     * @return the ID that is used to represent this report API in the JOSM preferences
     */
    public String getPrefId() {
      return prefId;
    }

    /**
     * @return the (internationalized) label describing this report API
     */
    public String getLabel() {
      return label;
    }
    
    public Boolean needsKey()
    {
      return needsKey;
    }
    
    public Boolean isProduction()
    {
      return isProduction;
    }
    
    public String getToken()
    {
      switch (fromPrefId(prefId))
      {
        case PDOK_PRODUCTION:
          return ReportProperties.API_KEY.get();
        case PDOK_ACCEPTANCE:
          return ReportProperties.API_KEY_ACT.get();
        case PROXY_PRODUCTION:
        case PROXY_ACCEPTANCE:
        default:
          return "NOTNEEDED";
      }
    }

    public static REPORT_API fromPrefId(String prefId) {
      for (REPORT_API mode : REPORT_API.values()) {
        if (mode.getPrefId().equals(prefId)) {
          return mode;
        }
      }
      return DEFAULT;
    }

    public static REPORT_API fromLabel(String label) {
      for (REPORT_API mode : REPORT_API.values()) {
        if (mode.getLabel().equals(label)) {
          return mode;
        }
      }
      return DEFAULT;
    }
  }
  
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
  public static final StringProperty API_REPORT_USE = new StringProperty("nl.bag.pdok.terugmeldapi.api.use_API", REPORT_API.DEFAULT.prefId);

  //  public static final BooleanProperty USE_ACT_API = new BooleanProperty("nl.bag.pdok.terugmeldapi.api.use_act", false);
  public static final StringProperty API_KEY_ACT = new StringProperty("nl.bag.pdok.terugmeldapi.api.act_key", null);
  public static final StringProperty API_URL_ACT = new StringProperty("nl.bag.pdok.terugmeldapi.api.act_url", "https://api.acceptatie.kadaster.nl/tms/v1/terugmeldingen");

  public static final StringProperty API_PROXY_URL = new StringProperty("nl.bag.pdok.terugmeldapi.api.proxy_url", "https://terugmeldingen.proxy.tools4osm.nl/v1");
  public static final StringProperty API_PROXY_URL_ACT = new StringProperty("nl.bag.pdok.terugmeldapi.api.proxy_act_url", "https://terugmeldingen.proxy.tools4osm.nl/act/v1");
  
  public static final IntegerProperty MAPOBJECT_ICON_SIZE = new IntegerProperty("nl.bag.pdok.terugmeldapi.mapobjects.iconsize", 32);
  public static final StringProperty DOWNLOAD_MODE = new StringProperty("nl.bag.pdok.terugmeldapi.download-mode", ReportDownloader.DOWNLOAD_MODE.DEFAULT.getPrefId());
  public static final StringProperty START_DIR = new StringProperty("nl.bag.pdok.terugmeldapi.start-directory", System.getProperty("user.home"));

  public static final BooleanProperty FILTER_HIDE_CLOSED = new BooleanProperty("nl.bag.pdok.terugmeldapi.filter.hideclosed", true);
  public static final DoubleProperty FILTER_HIDE_NUMBER = new DoubleProperty("nl.bag.pdok.terugmeldapi.filter.hidenumber", 1);
  public static final IntegerProperty FILTER_HIDE_PERIOD = new IntegerProperty("nl.bag.pdok.terugmeldapi.filter.hideperiod", 2);

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

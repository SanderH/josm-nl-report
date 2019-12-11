// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginException;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.nl_pdok_report.actions.ReportDownloadAction;
import org.openstreetmap.josm.plugins.nl_pdok_report.actions.ReportDownloadViewAction;
import org.openstreetmap.josm.plugins.nl_pdok_report.actions.ReportUploadAction;
import org.openstreetmap.josm.plugins.nl_pdok_report.actions.ReportZoomAction;
import org.openstreetmap.josm.plugins.nl_pdok_report.gui.ReportNewDialog;
import org.openstreetmap.josm.plugins.nl_pdok_report.gui.ReportFilterDialog;
import org.openstreetmap.josm.plugins.nl_pdok_report.gui.ReportPreferenceSetting;
import org.openstreetmap.josm.plugins.nl_pdok_report.gui.reportinfo.ReportInfoPanel;
import org.openstreetmap.josm.plugins.nl_pdok_report.oauth.ReportUser;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportProperties;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;

/**
 * This is the main class of the Feedback plugin.
 */
public class ReportPlugin extends Plugin {

  public static final ImageProvider LOGO = new ImageProvider("report-logo");

  /** Zoom action */
  private static final ReportZoomAction ZOOM_ACTION = new ReportZoomAction();
  /** Upload action */
  private static final ReportUploadAction UPLOAD_ACTION = new ReportUploadAction();

  static {
    if (MainApplication.getMainFrame() != null) {
      MainMenu.add(MainApplication.getMenu().imagerySubMenu, new ReportDownloadAction(), false);
      MainMenu.add(MainApplication.getMenu().viewMenu, ZOOM_ACTION, false, 15);
      MainMenu.add(MainApplication.getMenu().fileMenu, new ReportDownloadViewAction(), false, 14);
      MainMenu.add(MainApplication.getMenu().fileMenu, UPLOAD_ACTION, false, 14);
    }
  }

  /**
   * Main constructor.
   *
   * @param info
   *          Required information of the plugin. Obtained from the jar file.
   */
  public ReportPlugin(PluginInformation info) {
    super(info);

    if (ReportProperties.API_KEY.get() == null) {
      ReportUser.setTokenValid(false);
    }
  }

  static ReportDataListener[] getReportDataListeners() {
    return new ReportDataListener[] { UPLOAD_ACTION, ZOOM_ACTION };
  }

  /**
   * Called when the JOSM map frame is created or destroyed.
   */
  @Override
  public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
    if (oldFrame == null && newFrame != null) { // map frame added
      // MainApplication.getMap().addToggleDialog(ReportMainDialog.getInstance(), false);
      // MainApplication.getMap().addToggleDialog(ReportHistoryDialog.getInstance(), false);
      MainApplication.getMap().addToggleDialog(ReportInfoPanel.getInstance(), false);
      MainApplication.getMap().addToggleDialog(ReportNewDialog.getInstance(), false);
      MainApplication.getMap().addToggleDialog(ReportFilterDialog.getInstance(), false);
    }
    if (oldFrame != null && newFrame == null) { // map frame destroyed
      // ReportMainDialog.destroyInstance();
      // ReportHistoryDialog.destroyInstance();
      ReportNewDialog.destroyInstance();
      ReportFilterDialog.destroyInstance();
      ReportInfoPanel.destroyInstance();
    }
  }

  @Override
  public PreferenceSetting getPreferenceSetting() {
    return new ReportPreferenceSetting();
  }

  /**
   * @return the current {@link MapView} without throwing a {@link NullPointerException}
   */
  public static MapView getMapView() {
    final MapFrame mf = MainApplication.getMap();
    if (mf != null) {
      return mf.mapView;
    }
    return null;
  }

  public static String getPluginVersionString() {
    try {
      PluginInformation info = PluginInformation.findPlugin("nl-pdok-report");
      if (info != null) {
        return I18n.tr("JOSM plugin: {0}, {1}", info.name, info.version);
      }
    } catch (PluginException e) {
      Logging.error("Unable to get plugin version information", e);
    }
    return "";
  }
}

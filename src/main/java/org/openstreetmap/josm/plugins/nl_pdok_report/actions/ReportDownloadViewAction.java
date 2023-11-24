// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.preferences.AbstractProperty.ValueChangeEvent;
import org.openstreetmap.josm.data.preferences.AbstractProperty.ValueChangeListener;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportLayer;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportPlugin;
import org.openstreetmap.josm.plugins.nl_pdok_report.io.download.ReportDownloader;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportProperties;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.ImageProvider.ImageSizes;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * If in "download images in visible area" mode, downloads all the images in the current view.
 *
 * @author nokutu
 *
 */
public class ReportDownloadViewAction extends JosmAction implements ValueChangeListener<String> {
  private static final long serialVersionUID = -4272239552873243L;
  private static final String DESCRIPTION = I18n.marktr("Download PDOK Reports");
  public static final Shortcut SHORTCUT = Shortcut
    .registerShortcut("Report area", I18n.tr(DESCRIPTION), KeyEvent.VK_CLOSE_BRACKET, Shortcut.CTRL);

  /**
   * Main constructor.
   */
  public ReportDownloadViewAction() {
    super(
      I18n.tr(DESCRIPTION), new ImageProvider(ReportPlugin.LOGO).setSize(ImageSizes.DEFAULT), I18n.tr(DESCRIPTION), SHORTCUT, false, "reportArea", true
    );
    ReportProperties.DOWNLOAD_MODE.addListener(this);
    initEnabledState();
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    ReportDownloader.downloadVisibleArea();
  }

  @Override
  protected boolean listenToSelectionChange() {
    return false;
  }

  /**
   * Enabled when the Report layer is instantiated and download mode is either "osm area" or "manual".
   */
  @Override
  protected void updateEnabledState() {
    super.updateEnabledState();
    setEnabled(
      ReportLayer.hasInstance() && (ReportDownloader.getMode() == ReportDownloader.DOWNLOAD_MODE.OSM_AREA
        || ReportDownloader.getMode() == ReportDownloader.DOWNLOAD_MODE.MANUAL_ONLY)
    );
  }

  @Override
  public void valueChanged(ValueChangeEvent<? extends String> e) {
    updateEnabledState();
  }
}

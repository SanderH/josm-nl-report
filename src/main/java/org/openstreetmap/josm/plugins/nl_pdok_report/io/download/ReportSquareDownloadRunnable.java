// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.io.download;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportLayer;
import org.openstreetmap.josm.plugins.nl_pdok_report.gui.ReportFilterDialog;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportUtils;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.PluginState;

public class ReportSquareDownloadRunnable implements Runnable {

  private final Bounds bounds;

  /**
   * Main constructor.
   *
   * @param bounds
   *          the bounds of the area that should be downloaded
   *
   */
  public ReportSquareDownloadRunnable(Bounds bounds) {
    this.bounds = bounds;
  }

  @Override
  public void run() {
    PluginState.startDownload();
    ReportUtils.updateHelpText();

    // Download basic feedback data synchronously
    new ReportDownloadRunnable(ReportLayer.getInstance().getData(), bounds).run();

    if (Thread.interrupted()) {
      return;
    }

    PluginState.finishDownload();

    ReportUtils.updateHelpText();
    ReportLayer.invalidateInstance();
    ReportFilterDialog.getInstance().refresh();
    // sho ReportMainDialog.getInstance().updateReport();
  }
}

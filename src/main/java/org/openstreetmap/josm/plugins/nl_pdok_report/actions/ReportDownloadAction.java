// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportLayer;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportPlugin;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.ImageProvider.ImageSizes;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Action that triggers the plugin. If in automatic mode, it will automatically download the images in the areas where
 * there is OSM data.
 *
 * @author SanderH
 *
 */
public class ReportDownloadAction extends JosmAction {
  private static final long serialVersionUID = -1864499712816687292L;
  public static final Shortcut SHORTCUT = Shortcut
    .registerShortcut("Report", tr("Open PDOK Report layer"), KeyEvent.VK_OPEN_BRACKET, Shortcut.CTRL);

  /**
   * Main constructor.
   */
  public ReportDownloadAction() {
    super(
      tr("PDOK Reports"), new ImageProvider(ReportPlugin.LOGO).setSize(ImageSizes.DEFAULT), tr("Open PDOK Report layer"), SHORTCUT, false, "reportDownload", false
    );
  }

  @Override
  public void actionPerformed(ActionEvent ae) {
    if (!ReportLayer.hasInstance() || !MainApplication.getLayerManager().containsLayer(ReportLayer.getInstance())) {
      MainApplication.getLayerManager().addLayer(ReportLayer.getInstance());
      return;
    }

    try {
      // Successive calls to this action toggle the active layer between the OSM data layer and the feedback layer
      OsmDataLayer editLayer = MainApplication.getLayerManager().getEditLayer();
      if (MainApplication.getLayerManager().getActiveLayer() != ReportLayer.getInstance()) {
        MainApplication.getLayerManager().setActiveLayer(ReportLayer.getInstance());
      } else if (editLayer != null) {
        MainApplication.getLayerManager().setActiveLayer(editLayer);
      }
    } catch (IllegalArgumentException e) {
      // If the FeedbackLayer is not managed by LayerManager but you try to set it as active layer
      Logging.warn(e);
    }
  }
}

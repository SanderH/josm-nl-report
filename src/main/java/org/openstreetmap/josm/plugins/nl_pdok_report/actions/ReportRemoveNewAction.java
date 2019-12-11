// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportLayer;
import org.openstreetmap.josm.plugins.nl_pdok_report.gui.ReportNewDialog;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.ImageProvider.ImageSizes;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Imports a set of picture files into JOSM. They must be in jpg or png format.
 */
public class ReportRemoveNewAction extends JosmAction {

  private final ReportNewDialog changesetDialog;

  /**
   * Main constructor.
   * 
   * @param changesetDialog
   *          Report upload dialog
   */
  public ReportRemoveNewAction(ReportNewDialog changesetDialog) {
    super(
      I18n.tr("Remove report"), new ImageProvider("dialogs", "report-remove").setSize(ImageSizes.DEFAULT), I18n.tr("Remove the current report"),
      // CHECKSTYLE.OFF: LineLength
      Shortcut.registerShortcut("Remove report", I18n.tr("Remove the current report"), KeyEvent.CHAR_UNDEFINED, Shortcut.NONE),
      // CHECKSTYLE.ON: LineLength
      false, "reportSubmitChangeset", false
    );
    this.changesetDialog = changesetDialog;
    setEnabled(false);
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    ReportLayer.getInstance().getData().remove(changesetDialog.getSelectedReport());
  }

}

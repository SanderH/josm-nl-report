// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.actions;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.nl_pdok_report.AbstractReport;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportDataListener;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportLayer;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportPlugin;
import org.openstreetmap.josm.plugins.nl_pdok_report.gui.ReportUploadDialog;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.ImageProvider.ImageSizes;

/**
 * Action called when an upload to the Feedback servers is going to be performed. It lets you select a couple of
 * options.
 *
 * @author nokutu
 *
 */
public class ReportUploadAction extends JosmAction implements ReportDataListener {
  private static final long serialVersionUID = -770611576686314292L;
  private static final String TITLE = I18n.tr("Upload PDOK Report");

  /**
   * Main constructor.
   */
  public ReportUploadAction() {
    super(
      TITLE, new ImageProvider(ReportPlugin.LOGO).setSize(ImageSizes.DEFAULT), TITLE, null, false, "reportUpload", true
    );
    this.setEnabled(false);
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    ReportUploadDialog dialog = new ReportUploadDialog();
    JOptionPane pane = new JOptionPane(dialog, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
    JDialog dlg = pane.createDialog(MainApplication.getMainFrame(), TITLE);
    dlg.setMinimumSize(new Dimension(400, 150));
    dlg.setVisible(true);

    if (pane.getValue() != null && (int) pane.getValue() == JOptionPane.OK_OPTION
      && dialog.getSequence().isSelected()) {
      // sho UploadUtils.uploadSequence(
      // FeedbackLayer.getInstance().getData().getSelectedBAG(),
      // dialog.getDelete().isSelected()
      // );
    }
  }

  /**
   * Enabled if a mapillary image is selected.
   */
  @Override
  protected void updateEnabledState() {
    super.updateEnabledState();
    setEnabled(ReportLayer.hasInstance() && ReportLayer.getInstance().getData().getSelectedReport() != null);
  }

  @Override
  protected boolean listenToSelectionChange() {
    return false;
  }

  @Override
  public void reportsAdded() {
    // Enforced by {@link ReportDataListener}
  }

  @Override
  public void reportsRemoved() {
    // Enforced by {@link ReportDataListener}
  }

  @Override
  public void selectedReportChanged(AbstractReport oldReport, AbstractReport newReport) {
    if (oldReport == null && newReport != null) {
      setEnabled(true);
    } else if (oldReport != null && newReport == null) {
      setEnabled(false);
    }
  }
}

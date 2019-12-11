// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.openstreetmap.josm.plugins.nl_pdok_report.ReportLayer;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportNewBAG;
import org.openstreetmap.josm.plugins.nl_pdok_report.oauth.ReportUser;

/**
 * JPanel used when uploading pictures.
 *
 * @author nokutu
 *
 */
public class ReportUploadDialog extends JPanel {
  private static final long serialVersionUID = -5039621759165863370L;
  /** Upload the whole sequence. */
  private JRadioButton sequence;

  /**
   * Creates the JPanel and adds the needed elements.
   */
  public ReportUploadDialog() {
    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    if (ReportUser.getApiKey() == null) {
      this.add(new JLabel(tr("Go to setting and enter your user API key")));
    } else {
      ButtonGroup group = new ButtonGroup();
      this.sequence = new JRadioButton(tr("Upload selected sequence"));
      if (!(ReportLayer.getInstance().getData().getSelectedReport() instanceof ReportNewBAG)) {
        this.sequence.setEnabled(false);
      }
      group.add(this.sequence);
      add(this.sequence);
      group.setSelected(this.sequence.getModel(), true);
    }
  }

  /**
   * @return the sequence radio button of the dialog
   */
  public JRadioButton getSequence() {
    return sequence;
  }
}

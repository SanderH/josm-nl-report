// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.gui.reportinfo;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;
import java.util.Optional;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openstreetmap.josm.data.osm.DataSelectionListener;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.preferences.AbstractProperty.ValueChangeListener;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.plugins.nl_pdok_report.AbstractReport;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportBAG;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportDataListener;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportNewBAG;
import org.openstreetmap.josm.plugins.nl_pdok_report.gui.boilerplate.ReportButton;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportProperties;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportURL;
import org.openstreetmap.josm.tools.I18n;

public final class ReportInfoPanel extends ToggleDialog implements ReportDataListener, DataSelectionListener {
  private static final long serialVersionUID = 8754784357845006162L;
  private static final Log L = LogFactory.getLog(ReportInfoPanel.class);
  private static ReportInfoPanel instance;

  private final JLabel lblReportNumber = new JLabel(tr("Registration number"));
  private final JLabel lblRegistrationDate = new JLabel(tr("Registration date"));
  private final JLabel lblDescription = new JLabel(tr("Description"));
  private final JLabel lblStatus = new JLabel(tr("Status"));
  private final JLabel lblStatusDate = new JLabel(tr("Status date"));
  private final JLabel lblResponsible = new JLabel(tr("Responsible"));
  private final JLabel lblExplanation = new JLabel(tr("Explanation"));
  private final JLabel lblObjectId = new JLabel(tr("Object Id"));
  private final JLabel lblObjectType = new JLabel(tr("Object Type"));

  private final JLabel reportNumber = new JLabel();
  private final JLabel registrationDate = new JLabel();
  private final JTextArea description;
  private final JLabel status = new JLabel();
  private final JLabel statusDate = new JLabel();
  private final JLabel responsible = new JLabel();
  private final JLabel explanation = new JLabel();
  private final JLabel objectId = new JLabel();
  private final JLabel objectType = new JLabel();
  private final WebLinkAction wlaLocationLink;

  private ValueChangeListener<Boolean> reportLinkChangeListener;

  private ReportInfoPanel() {
    super(
      I18n.tr("Report info"), "report-info", I18n.tr("Displays detail information on the currently selected report"), null, 250
    );
    MainApplication.getLayerManager().addAndFireActiveLayerChangeListener(event -> {
      try {
        Optional.ofNullable(event.getPreviousDataSet()).ifPresent(it -> it.removeSelectionListener(this));
      } catch (IllegalArgumentException e) {
        // The selection listener was not registered
      }
      Optional.ofNullable(MainApplication.getLayerManager().getActiveDataSet())
        .ifPresent(it -> it.addSelectionListener(this));
    });

    description = new JTextArea();
    description.setEditable(false);
    description.setLineWrap(true);
    description.setWrapStyleWord(true);
    description.setFont(lblReportNumber.getFont());
    description.setSize(this.getWidth() - lblDescription.getWidth() - 60, description.getHeight());

    wlaLocationLink = new WebLinkAction(I18n.tr("Visit report link"), null);

    JPanel root = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(0, 8, 0, 8);
    
    // Left column
    gbc.gridx = 0;
    gbc.gridy = 0;
//    gbc.anchor = GridBagConstraints.LINE_END;
    gbc.anchor = GridBagConstraints.FIRST_LINE_END;
    gbc.gridwidth = 1;
    gbc.gridheight = 2;
    root.add(lblReportNumber, gbc);
    gbc.gridy += 2;
    root.add(lblRegistrationDate, gbc);
    gbc.gridy += 2;
    root.add(lblDescription, gbc);
    gbc.gridy += 2;
    gbc.gridheight = 1;
    root.add(lblStatus, gbc);
    gbc.gridy++;
    root.add(lblStatusDate, gbc);
    gbc.gridy++;
    root.add(lblResponsible, gbc);
    gbc.gridy++;
    root.add(lblExplanation, gbc);
    gbc.gridy++;
    root.add(lblObjectType, gbc);
    gbc.gridy++;
    root.add(lblObjectId, gbc);
    gbc.gridy++;
    root.add(new JLabel(I18n.tr("Link")), gbc);

    // Right column
    gbc.weightx = 1;
    gbc.gridx++;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.LINE_START;
    root.add(reportNumber, gbc);
    gbc.gridy += 2;
    root.add(registrationDate, gbc);
    gbc.gridy += 2;
    root.add(description, gbc);
    gbc.gridy += 2;
    gbc.gridheight = 1;
    root.add(status, gbc);
    gbc.gridy++;
    root.add(statusDate, gbc);
    gbc.gridy++;
    root.add(responsible, gbc);
    gbc.gridy++;
    root.add(explanation, gbc);
    gbc.gridy++;
    root.add(objectType, gbc);
    gbc.gridy++;
    root.add(objectId, gbc);
    gbc.gridy++;
    root.add(new ReportButton(wlaLocationLink), gbc);

    createLayout(root, true, null);
    selectedReportChanged(null, null);
  }

  public static ReportInfoPanel getInstance() {
    synchronized (ReportInfoPanel.class) {
      if (instance == null) {
        instance = new ReportInfoPanel();
      }
      return instance;
    }
  }

  /**
   * Destroys the unique instance of the class.
   */
  public static synchronized void destroyInstance() {
    instance = null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openstreetmap.josm.gui.dialogs.ToggleDialog#stateChanged()
   */
  @Override
  protected void stateChanged() {
    super.stateChanged();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openstreetmap.josm.plugins.nl_pdok_feedback.ReportDataListener#imagesAdded()
   */
  @Override
  public void reportsAdded() {
    // Method is not needed, but enforced by the interface ReportDataListener
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openstreetmap.josm.plugins.nl_pdok_feedback.ReportDataListener#imagesAdded()
   */
  @Override
  public void reportsRemoved() {
    // Method is not needed, but enforced by the interface ReportDataListener
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.openstreetmap.josm.plugins.nl_pdok_feedback.ReportDataListener#selectedImageChanged(org.openstreetmap.josm.
   * plugins.nl_pdok_feedback.FeedbackAbstractImage,
   * org.openstreetmap.josm.plugins.nl_pdok_feedback.FeedbackAbstractImage)
   */
  @Override
  public synchronized void selectedReportChanged(final AbstractReport oldReport, final AbstractReport newReport) {
    L.debug(
      String.format(
        "Selected report changed from %s to %s.",
        oldReport instanceof ReportBAG ? ((ReportBAG) oldReport).getReportNumberFull() : "‹none›",
        newReport instanceof ReportBAG ? ((ReportBAG) newReport).getReportNumberFull() : "‹none›"
      )
    );

    wlaLocationLink.setEnabled(newReport instanceof ReportBAG);
    if (newReport instanceof ReportBAG) {
      ReportBAG newReportBAG = ((ReportBAG) newReport);
      reportNumber.setText(newReportBAG.getReportNumberFull());
      registrationDate.setText(newReportBAG.getReportedAt(ReportProperties.DATE_FORMAT.get()));
      description.setText(newReportBAG.getDescription());
      explanation.setText(newReportBAG.getExplanation());
      responsible.setText(newReportBAG.getSourceMaintainerName());
      status.setText(newReportBAG.getStatus());
      statusDate.setText(newReportBAG.getStatusModifiedAt(ReportProperties.DATE_FORMAT.get()));
      wlaLocationLink.setURL(newReportBAG.getLocationLink());

      if (!ReportProperties.API_KEY.isSet()) {
        reportLinkChangeListener = b -> wlaLocationLink.setURL(ReportURL.requestApiKeyURL());
      }
    } else if (newReport instanceof ReportNewBAG) {
      ReportNewBAG newReportBAG = ((ReportNewBAG) newReport);
      reportNumber.setText(null);
      registrationDate.setText(null);
      description.setText(newReportBAG.getDescription());
      explanation.setText(null);
      responsible.setText(null);
      status.setText(I18n.tr("Pending upload"));
      statusDate.setText(null);
      wlaLocationLink.setURL(null);
    } else {
      reportNumber.setText(null);
      registrationDate.setText(null);
      description.setText(null);
      explanation.setText(null);
      responsible.setText(null);
      status.setText(null);
      statusDate.setText(null);
      wlaLocationLink.setURL(null);
    }

    description.setSize(this.getWidth() - lblDescription.getWidth() - 100, description.getHeight());
    description.setBackground(lblDescription.getBackground());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openstreetmap.josm.data.SelectionChangedListener#selectionChanged(java.util.Collection)
   */
  @Override
  public synchronized void selectionChanged(final SelectionChangeEvent event) {
    final Collection<OsmPrimitive> sel = event.getSelection();
    L.debug(String.format("Selection changed. %d primitives are selected.", sel == null ? 0 : sel.size()));
  }
}

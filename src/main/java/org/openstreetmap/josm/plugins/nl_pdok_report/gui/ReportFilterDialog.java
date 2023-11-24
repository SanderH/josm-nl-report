// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Calendar;
import java.util.function.Predicate;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.plugins.nl_pdok_report.AbstractReport;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportBAG;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportDataListener;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportLayer;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportNewBAG;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportProperties;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * ToggleDialog that lets you filter the images that are being shown.
 *
 * @author nokutu
 * @see ReportFilterChooseSigns
 */
public final class ReportFilterDialog extends ToggleDialog implements ReportDataListener {
  private static final long serialVersionUID = -3833041091569456525L;

  private static ReportFilterDialog instance;

  private static final String[] TIME_LIST = { tr("Years"), tr("Months"), tr("Days") };

  private static final long[] TIME_FACTOR = new long[] { 31_536_000_000L, // = 365 * 24 * 60 * 60 * 1000 = number of ms
                                                                          // in a year
      2_592_000_000L, // = 30 * 24 * 60 * 60 * 1000 = number of ms in a month
      86_400_000 // = 24 * 60 * 60 * 1000 = number of ms in a day
  };

  private final JCheckBox filterByDateCheckbox;
  /**
   * Spinner to choose the range of dates.
   */
  private final SpinnerNumberModel spinnerModel;

  private final JCheckBox newReports = new JCheckBox(tr("New reports"));
  private final JCheckBox downloadedReports = new JCheckBox(new DownloadCheckBoxAction());
  private final JComboBox<String> time;
//  private final JTextField user;

  private ReportFilterDialog() {
    super(
      tr("Report filter"), "report-filter", tr("Open report filter dialog"), null, 150, false, ReportPreferenceSetting.class
    );

    JPanel fromPanel = new JPanel();
    fromPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    filterByDateCheckbox = new JCheckBox(tr("Hide completed Reports older than:"));
    fromPanel.add(filterByDateCheckbox);
    this.spinnerModel = new SpinnerNumberModel(1.0, 0, 10000, 1);
    JSpinner spinner = new JSpinner(spinnerModel);
    spinner.setEnabled(false);
    fromPanel.add(spinner);
    time = new JComboBox<>(TIME_LIST);
    time.setEnabled(false);
    fromPanel.add(this.time);

    filterByDateCheckbox.addItemListener(itemE -> {
      spinner.setEnabled(filterByDateCheckbox.isSelected());
      time.setEnabled(filterByDateCheckbox.isSelected());
    });

    this.newReports.setSelected(true);
    this.downloadedReports.setSelected(true);

    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.LINE_START;
    panel.add(this.downloadedReports, c);
    c.gridx = 1;
    panel.add(this.newReports, c);
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 2;
    panel.add(fromPanel, c);
    c.gridy = 2;
//    panel.add(userSearchPanel, c);

    createLayout(panel, true, Arrays.asList(new SideButton(new UpdateAction()), new SideButton(new ResetAction())));
  }

  /**
   * @return the unique instance of the class.
   */
  public static synchronized ReportFilterDialog getInstance() {
    if (instance == null)
      instance = new ReportFilterDialog();
    return instance;
  }

  @Override
  public void reportsAdded() {
    refresh();
  }

  @Override
  public void reportsRemoved() {
    refresh();
  }

  @Override
  public void selectedReportChanged(AbstractReport oldReport, AbstractReport newReport) {
    // Do nothing when image selection changed
  }

  /**
   * Resets the dialog to its default state.
   */
  public void reset() {
    this.newReports.setSelected(true);
    this.downloadedReports.setSelected(true);
//    this.user.setText("");
    this.filterByDateCheckbox.setSelected(ReportProperties.FILTER_HIDE_CLOSED.get());
    this.time.setSelectedItem(TIME_LIST[ReportProperties.FILTER_HIDE_PERIOD.get()]);
    this.spinnerModel.setValue(ReportProperties.FILTER_HIDE_NUMBER.get());
    refresh();
  }

  /**
   * Applies the selected filter.
   */
  public synchronized void refresh() {
    final boolean layerVisible = ReportLayer.hasInstance() && ReportLayer.getInstance().isVisible();
    final boolean newReports = this.newReports.isSelected();
    final boolean downloaded = this.downloadedReports.isSelected();
    final boolean timeFilter = filterByDateCheckbox.isSelected();

    // This predicate returns true is the image should be made invisible
    Predicate<AbstractReport> shouldHide = report -> {
      if (!layerVisible) {
        return true;
      }
      if (timeFilter && checkValidTime(report) && report instanceof ReportBAG && 
        (((ReportBAG)report).getStatusCode().equals("AFGEROND") || ((ReportBAG)report).getStatusCode().equals("AFGEWEZEN"))) {
        return true;
      }
      if (!newReports && report instanceof ReportNewBAG) {
        return true;
      }
      if (report instanceof ReportBAG) {
        if (!downloaded) {
          return true;
        }
      }
      return false;
    };

    if (ReportLayer.hasInstance()) {
      ReportLayer.getInstance().getData().getReports().parallelStream()
        .forEach(report -> report.setVisible(!shouldHide.test(report)));
    }

    ReportLayer.invalidateInstance();
  }

  private boolean checkValidTime(AbstractReport bag) {
    if (bag instanceof ReportBAG) {
      ReportBAG feedbackBAG = (ReportBAG) bag;
      Long currentTime = currentTime();
      for (int i = 0; i < 3; i++) {
        if (TIME_LIST[i].equals(time.getSelectedItem()) && feedbackBAG.getReportedAt().getTime() < currentTime
          - spinnerModel.getNumber().doubleValue() * TIME_FACTOR[i]) {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * Save the selected filter settings.
   */
  public synchronized void save() {
    ReportProperties.FILTER_HIDE_CLOSED.put(filterByDateCheckbox.isSelected());
    ReportProperties.FILTER_HIDE_NUMBER.put(spinnerModel.getNumber().doubleValue());
    ReportProperties.FILTER_HIDE_PERIOD.put(time.getSelectedIndex());
  }
  
  private static long currentTime() {
    Calendar cal = Calendar.getInstance();
    return cal.getTimeInMillis();
  }

  /**
   * Destroys the unique instance of the class.
   */
  public static synchronized void destroyInstance() {
    instance = null;
  }

  private class DownloadCheckBoxAction extends AbstractAction {
    private static final long serialVersionUID = 1198389258289785522L;

    DownloadCheckBoxAction() {
      putValue(NAME, tr("Downloaded reports"));
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
      //
    }
  }

  private static class UpdateAction extends AbstractAction {
    private static final long serialVersionUID = 1776993064234158637L;

    UpdateAction() {
      putValue(NAME, tr("Update"));
      new ImageProvider("dialogs", "refresh").getResource().attachImageIcon(this, true);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
      ReportFilterDialog.getInstance().refresh();
    }
  }

  private static class ResetAction extends AbstractAction {
    private static final long serialVersionUID = 6679721670983203591L;

    ResetAction() {
      putValue(NAME, tr("Reset"));
      new ImageProvider("preferences", "reset").getResource().attachImageIcon(this, true);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
      ReportFilterDialog.getInstance().reset();
    }
  }
  
  private static class SaveAction extends AbstractAction {
    private static final long serialVersionUID = 7302498164858018650L;

    @SuppressWarnings("unused")
    SaveAction() {
      putValue(NAME, tr("Save"));
      new ImageProvider("dialogs", "save").getResource().attachImageIcon(this, true);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
      ReportFilterDialog.getInstance().save();
    }
  }


}

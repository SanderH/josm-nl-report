// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.plugins.nl_pdok_report.gui.ReportNewDialog;
import org.openstreetmap.josm.plugins.nl_pdok_report.gui.reportinfo.ReportInfoPanel;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.api.JsonNewReportEncoder;
import org.openstreetmap.josm.tools.Logging;

/**
 * Database class for all the {@link AbstractReport} objects.
 *
 * @author SanderH
 * @see AbstractReport
 */
public class ReportData {
  private Set<AbstractReport> reports = ConcurrentHashMap.newKeySet();
  /**
   * The image currently selected, this is the one being shown.
   */
  private AbstractReport selectedReport;
  /**
   * The image under the cursor.
   */
  private AbstractReport highlightedReport;
  /**
   * All the images selected, can be more than one.
   */
  private final Set<AbstractReport> multiSelectedReports = ConcurrentHashMap.newKeySet();
  /**
   * Listeners of the class.
   */
  private final List<ReportDataListener> listeners = new CopyOnWriteArrayList<>();
  /**
   * The bounds of the areas for which the reports have been downloaded.
   */
  private final List<Bounds> bounds;

  /**
   * Creates a new object and adds the initial set of listeners.
   */
  protected ReportData() {
    this.selectedReport = null;
    this.bounds = new CopyOnWriteArrayList<>();

    // Adds the basic set of listeners.
    Arrays.stream(ReportPlugin.getReportDataListeners()).forEach(this::addListener);
    if (MainApplication.getMainFrame() != null) {
      addListener(ReportInfoPanel.getInstance());
      addListener(ReportNewDialog.getInstance());
    }
  }

  /**
   * Adds an ReportBAG to the object, and then repaints mapView.
   *
   * @param image
   *          The report to be added.
   */
  public void add(AbstractReport report) {
    add(report, true);
  }

  /**
   * Adds a ReportBAG to the object, but doesn't repaint mapView. This is needed for concurrency.
   *
   * @param report
   *          The report to be added.
   * @param update
   *          Whether the map must be updated or not.
   * @throws NullPointerException
   *           if parameter <code>image</code> is <code>null</code>
   */
  public void add(AbstractReport report, boolean update) {
    if (!reports.add(report)) {
      fireReportsAdded();
    }
    if (update) {
      ReportLayer.invalidateInstance();
    }
    fireReportsAdded();
  }

  /**
   * Adds a set of ReportBAGs to the object, and then repaints mapView.
   *
   * @param images
   *          The set of images to be added.
   */
  public void addAll(Collection<? extends AbstractReport> reports) {
    addAll(reports, true);
  }

  /**
   * Adds a set of {link AbstractReport} objects to this object.
   *
   * @param newReports
   *          The set of images to be added.
   * @param update
   *          Whether the map must be updated or not.
   */
  public void addAll(Collection<? extends AbstractReport> newReports, boolean update) {
    reports.addAll(newReports);
    if (update) {
      ReportLayer.invalidateInstance();
    }
    fireReportsAdded();
  }

  /**
   * Adds a new listener.
   *
   * @param lis
   *          Listener to be added.
   */
  public final void addListener(final ReportDataListener lis) {
    listeners.add(lis);
  }

  /**
   * Adds a {@link ReportBAG} object to the list of selected images, (when ctrl + click)
   *
   * @param image
   *          The {@link ReportBAG} object to be added.
   */
  public void addMultiSelectedReport(final AbstractReport report) {
    if (!this.multiSelectedReports.contains(report)) {
      if (this.getSelectedReport() == null) {
        this.setSelectedReport(report);
      } else {
        this.multiSelectedReports.add(report);
      }
    }
    ReportLayer.invalidateInstance();
  }

  /**
   * Adds a set of {@code AbstractReport} objects to the list of selected reports.
   *
   * @param images
   *          A {@link Collection} object containing the set of images to be added.
   */
  public void addMultiSelectedReport(Collection<AbstractReport> reports) {
    reports.stream().filter(report -> !this.multiSelectedReports.contains(report)).forEach(report -> {
      if (this.getSelectedReport() == null) {
        this.setSelectedReport(report);
      } else {
        this.multiSelectedReports.add(report);
      }
    });
    ReportLayer.invalidateInstance();
  }

  public List<Bounds> getBounds() {
    return bounds;
  }

  /**
   * Removes an report from the database. From the {@link List} in this object
   *
   * @param image
   *          The {@link AbstractReport} that is going to be deleted.
   */
  public void remove(AbstractReport report) {
    reports.remove(report);
    if (getMultiSelectedReports().contains(report)) {
      setSelectedReport(null);
    }
    ReportLayer.invalidateInstance();
    fireReportsRemoved();
  }

  /**
   * Removes a set of images from the database.
   *
   * @param images
   *          A {@link Collection} of {@link AbstractReport} objects that are going to be removed.
   */
  public void remove(Collection<AbstractReport> reports) {
    reports.forEach(this::remove);
  }

  /**
   * Removes a listener.
   *
   * @param lis
   *          Listener to be removed.
   */
  public void removeListener(ReportDataListener lis) {
    this.listeners.remove(lis);
  }

  public void createReport(LatLon latLon, String description) {
    ReportNewBAG newReport = new ReportNewBAG(latLon);
    newReport.setDescription(description);
    add(newReport);
  }

  /**
   * Returns the {@link List<AbstractReport>} object, which acts as the database of the Layer.
   *
   * @return The {@link ReportData} object that stores the database.
   */
  public List<AbstractReport> getNewReports() {
    return getReports().stream().filter(report -> (report instanceof ReportNewBAG)).collect(Collectors.toList());
  }

  /**
   * Highlights the report under the cursor.
   *
   * @param image
   *          The report under the cursor.
   */
  public void setHighlightedReport(AbstractReport report) {
    this.highlightedReport = report;
  }

  /**
   * Returns the report under the mouse cursor.
   *
   * @return The report under the mouse cursor.
   */
  public AbstractReport getHighlightedReport() {
    return this.highlightedReport;
  }

  /**
   * Returns a Set containing all reports.
   *
   * @return A Set object containing all reports.
   */
  public Set<AbstractReport> getReports() {
    return reports;
  }

  /**
   * Returns the ReportBAG object that is currently selected.
   *
   * @return The selected ReportBAG object.
   */
  public AbstractReport getSelectedReport() {
    return this.selectedReport;
  }

  private void fireReportsAdded() {
    listeners.stream().filter(Objects::nonNull).forEach(ReportDataListener::reportsAdded);
  }

  private void fireReportsRemoved() {
    listeners.stream().filter(Objects::nonNull).forEach(ReportDataListener::reportsRemoved);
  }

  /**
   * Selects a new image.If the user does ctrl + click, this isn't triggered.
   *
   * @param report
   *          The ReportBAG which is going to be selected
   */
  public void setSelectedReport(AbstractReport report) {
    setSelectedReport(report, false);
  }

  /**
   * Selects a new report. If the user does ctrl+click, this isn't triggered. You can choose whether to center the view
   * on the new image or not.
   *
   * @param image
   *          The {@link ReportBAG} which is going to be selected.
   * @param zoom
   *          True if the view must be centered on the image; false otherwise.
   */
  public void setSelectedReport(AbstractReport report, boolean zoom) {
    AbstractReport oldReport = this.selectedReport;
    this.selectedReport = report;
    this.multiSelectedReports.clear();
    final MapView mv = ReportPlugin.getMapView();
    if (mv != null && zoom && selectedReport != null) {
      mv.zoomTo(selectedReport.getMovingLatLon());
    }
    fireSelectedReportChanged(oldReport, this.selectedReport);
    ReportLayer.invalidateInstance();

    if (report != null) {
      String coordOSM = report.getTempLatLon().toDisplayString();
      Logging.debug("OSM: " + coordOSM);
      String coordPDOK = JsonNewReportEncoder.reportLatLon(report.getTempLatLon()).toDisplayString();
      Logging.debug("PDOK: " + coordPDOK);
    }
  }

  private void fireSelectedReportChanged(AbstractReport oldReport, AbstractReport newReport) {
    listeners.stream().filter(Objects::nonNull).forEach(lis -> lis.selectedReportChanged(oldReport, newReport));
  }

  /**
   * Returns a List containing all {@code AbstractReport} objects selected with ctrl + click.
   *
   * @return A List object containing all the images selected.
   */
  public Set<AbstractReport> getMultiSelectedReports() {
    return this.multiSelectedReports;
  }

  /**
   * Sets a new {@link Collection} object as the used set of reports. Any reports that are already present, are removed.
   *
   * @param newImages
   *          the new image list (previously set images are completely replaced)
   */
  public void setReports(Collection<AbstractReport> newReport) {
    synchronized (this) {
      reports.clear();
      reports.addAll(newReport);
    }
  }
}

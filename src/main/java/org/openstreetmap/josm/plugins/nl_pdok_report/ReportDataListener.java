// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report;

/**
 * Interface for listeners of the class {@link ReportData}.
 *
 * @author SanderH
 *
 */
public interface ReportDataListener {

  /**
   * Fired when any report is added to the database.
   */
  void reportsAdded();

  /**
   * Fired when any report is added to the database.
   */
  void reportsRemoved();

  /**
   * Fired when the selected image is changed by something different from manually clicking on the icon.
   *
   * @param oldImage
   *          Old selected {@link AbstractReport}
   * @param newImage
   *          New selected {@link AbstractReport}
   */
  void selectedReportChanged(AbstractReport oldReport, AbstractReport newReport);
}

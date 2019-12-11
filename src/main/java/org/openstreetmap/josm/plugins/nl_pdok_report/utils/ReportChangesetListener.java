// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.utils;

import org.openstreetmap.josm.plugins.nl_pdok_report.ReportData;

/**
 * Interface for listeners of the class {@link ReportData}.
 */
@FunctionalInterface
public interface ReportChangesetListener {

  /**
   * Fired when the a report is added or removed
   */
  void changesetChanged();
}

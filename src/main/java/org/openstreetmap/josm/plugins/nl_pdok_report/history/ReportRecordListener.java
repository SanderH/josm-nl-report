// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.history;

/**
 * Interface for the listener of the {@link ReportRecord} class
 *
 * @author nokutu
 * @see ReportRecord
 */
@FunctionalInterface
public interface ReportRecordListener {

  /**
   * Fired when any command is undone or redone.
   */
  void recordChanged();
}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.history.commands;

import java.util.Set;

import org.openstreetmap.josm.plugins.nl_pdok_report.AbstractReport;

/**
 * Superclass for those commands that must be executed after creation.
 *
 * @author SanderH
 *
 */
public abstract class ReportExecutableCommand extends ReportCommand {

  /**
   * Main constructor.
   *
   * @param images
   *          The set of images affected by the command.
   */
  public ReportExecutableCommand(final Set<? extends AbstractReport> images) {
    super(images);
  }

  /**
   * Executes the command. It is run when the command is added to the history record.
   */
  public abstract void execute();
}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.history.commands;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.openstreetmap.josm.plugins.nl_pdok_report.AbstractReport;

/**
 * Abstract class for any Feedback command.
 *
 * @author nokutu
 *
 */
public abstract class ReportCommand {
  /** Set of {@link FeedbackAbstractImage} objects affected by the command */
  public final Set<AbstractReport> images;

  /**
   * Main constructor.
   *
   * @param images
   *          The images that are affected by the command.
   */
  public ReportCommand(final Set<? extends AbstractReport> images) {
    this.images = new ConcurrentSkipListSet<>(images);
  }

  /**
   * Undoes the action.
   */
  public abstract void undo();

  /**
   * Redoes the action.
   */
  public abstract void redo();

  /**
   * If two equal commands are applied consecutively to the same set of images, they are summed in order to reduce them
   * to just one command.
   *
   * @param command
   *          The command to be summed to last command.
   */
  public abstract void sum(ReportCommand command);

  @Override
  public abstract String toString();
}

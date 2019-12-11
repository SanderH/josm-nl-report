// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.history.commands;

import static org.openstreetmap.josm.tools.I18n.trn;

import java.util.Set;

import org.openstreetmap.josm.plugins.nl_pdok_report.AbstractReport;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportLayer;

/**
 * Imports a set of images stored locally.
 *
 * @author nokutu
 *
 */
public class CommandImport extends ReportExecutableCommand {

  /**
   * Main constructor.
   *
   * @param images
   *          The set of images that are going to be added. Might be in the same sequence or not.
   */
  public CommandImport(Set<AbstractReport> images) {
    super(images);
  }

  @Override
  public void execute() {
    ReportLayer.getInstance().getData().addAll(this.images);
  }

  @Override
  public void undo() {
    for (AbstractReport img : this.images) {
      ReportLayer.getInstance().getData().getReports().remove(img);
    }
    ReportLayer.invalidateInstance();
  }

  @Override
  public void redo() {
    this.execute();
  }

  @Override
  public void sum(ReportCommand command) {
  }

  @Override
  public String toString() {
    return trn("Imported {0} image", "Imported {0} images", this.images.size(), this.images.size());
  }
}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.history.commands;

import static org.openstreetmap.josm.tools.I18n.trn;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.josm.plugins.nl_pdok_report.AbstractReport;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportLayer;

/**
 * Command used to delete a set of images.
 *
 * @author nokutu
 *
 */
public class CommandDelete extends ReportExecutableCommand {

  private final Map<AbstractReport, Integer> changesHash = new HashMap<>();

  /**
   * Main constructor.
   *
   * @param images
   *          The set of images that are going to be deleted.
   */
  public CommandDelete(final Set<? extends AbstractReport> images) {
    super(images);
  }

  @Override
  public void sum(ReportCommand command) {
    // TODO: Implement
  }

  @Override
  public void execute() {
    for (AbstractReport img : this.images) {
      // sho this.changesHash.put(img, img.getSequence().getImages().indexOf(img));
      ReportLayer.getInstance().getData().remove(img);
    }
  }

  @Override
  public String toString() {
    return trn("Deleted {0} image", "Deleted {0} images", this.images.size(), this.images.size());
  }

  @Override
  public void undo() {
    for (AbstractReport img : images) {
      ReportLayer.getInstance().getData().add(img);
      // sho img.getSequence().getImages().add(this.changesHash.get(img), img);
    }
  }

  @Override
  public void redo() {
    ReportLayer.getInstance().getData().remove(this.images);
  }
}

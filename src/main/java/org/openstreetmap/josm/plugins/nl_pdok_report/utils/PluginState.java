// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.utils;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainFrame;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.Logging;

/**
 * @author SanderH
 *
 */
public final class PluginState {

  private static boolean submittingChangeset;

  private static int runningDownloads;
  /** Images that have to be uploaded. */
  private static int reportsToUpload;
  /** Images that have been uploaded. */
  private static int reportsUploaded;

  private PluginState() {
    // Empty constructor to avoid instantiation
  }

  /**
   * Called when a download is started.
   */
  public static void startDownload() {
    runningDownloads++;
  }

  /**
   * Called when a download is finished.
   */
  public static void finishDownload() {
    if (runningDownloads == 0) {
      Logging.warn("The amount of running downloads is equal to 0");
      return;
    }
    runningDownloads--;
  }

  /**
   * Checks if there is any running download.
   *
   * @return true if the plugin is downloading; false otherwise.
   */
  public static boolean isDownloading() {
    return runningDownloads > 0;
  }

  /**
   * Checks if there is a changeset being submitted.
   *
   * @return true if the plugin is submitting a changeset false otherwise.
   */
  public static boolean isSubmittingChangeset() {
    return submittingChangeset;
  }

  /**
   * Checks if there is any running upload.
   *
   * @return true if the plugin is uploading; false otherwise.
   */
  public static boolean isUploading() {
    return reportsToUpload > reportsUploaded;
  }

  /**
   * Sets the amount of images that are going to be uploaded.
   *
   * @param amount
   *          The amount of images that are going to be uploaded.
   */
  public static void addReportsToUpload(int amount) {
    if (reportsToUpload <= reportsUploaded) {
      reportsToUpload = 0;
      reportsUploaded = 0;
    }
    reportsToUpload += amount;
  }

  public static int getReportsToUpload() {
    return reportsToUpload;
  }

  public static int getReportsUploaded() {
    return reportsUploaded;
  }

  /**
   * Called when an image is uploaded.
   */
  public static void reportsUploaded() {
    reportsUploaded++;
    if (reportsToUpload == reportsUploaded && MainApplication.getMainFrame() != null) {
      finishedUploadDialog(reportsUploaded);
    }
  }

  private static void finishedUploadDialog(int numReports) {
    JOptionPane.showMessageDialog(
      MainApplication.getMainFrame(),
      I18n.trn(
        "You have successfully submitted {0} report to pdok.nl",
        "You have successfully submitted {0} reports to pdok.nl", numReports, numReports
      ), tr("Finished upload"), JOptionPane.INFORMATION_MESSAGE
    );
  }

  public static void notLoggedInToReportDialog() {
    final MainFrame mainFrame = MainApplication.getMainFrame();
    if (mainFrame != null) {
      JOptionPane.showMessageDialog(
        mainFrame, tr("No API key set for the report API, please enter yours in the preferences"), tr("No API key set for report API"), JOptionPane.WARNING_MESSAGE
      );
    }
  }

  /**
   * Returns the text to be written in the status bar.
   *
   * @return The {@code String} that is going to be written in the status bar.
   */
  public static String getUploadString() {
    return tr("Uploading: {0}", "(" + reportsUploaded + "/" + reportsToUpload + ")");
  }

  public static void setSubmittingChangeset(boolean isSubmitting) {
    submittingChangeset = isSubmitting;
  }
}

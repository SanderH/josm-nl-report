// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.oauth;

import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportProperties;

/**
 * Represents the current logged in user and stores its data.
 *
 * @author nokutu
 *
 */
public final class ReportUser {

  private static String email;
  private static String apikey;
  /** If the stored token is valid or not. */
  private static boolean isTokenValid = true;

  private ReportUser() {
    // Private constructor to avoid instantiation
    email = ReportProperties.USER_EMAIL.get();
    apikey = ReportProperties.API_KEY.get();
  }

  public static String getEmail() {
    return email;
  }

  public static String getApiKey() {
    return apikey;
  }

  /**
   * Resets the FeedbackUser to null values.
   */
  public static synchronized void reset() {
    email = null;
    apikey = null;

    isTokenValid = false;
  }

  public static synchronized void setTokenValid(boolean value) {
    isTokenValid = value;
  }
}

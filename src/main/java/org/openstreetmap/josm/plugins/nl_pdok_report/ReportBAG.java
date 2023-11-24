// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.tools.Logging;

/**
 * A ReportBAG object represents each of the reports.
 *
 * @author SanderH
 * @see ReportData
 */
public class ReportBAG extends AbstractReport {
  private final String source;
  private final String sourceMaintainerCode;
  private final String sourceMaintainerName;
  private final URL locationLink;
  private final Long reportNumber;
  private final String reportNumberFull;
  private final String product;
  private final String status;
  private final String statusCode;
  private final Date reportedAt;
  private final Date statusModifiedAt;
  private final Date modifiedAt;
  private final String explanation;
  private final String objectId;
  private final String objectType;

  /**
   * Main constructor of the class ReportBAG
   *
   * @param key
   *          The unique identifier of the image.
   * @param latLon
   *          The latitude and longitude where it is positioned.
   * @param ca
   *          The direction of the images in degrees, meaning 0 north.
   */
  public ReportBAG(
    final LatLon latLon, final String baseRegistration, final String source, final String sourceMaintainerCode,
    final String sourceMaintainerName, final String locationLink, final Long reportNumber,
    final String reportNumberFull, final String description, final String product, final String status,
    final String statusCode, final Date reportedAt, final Date statusModifiedAt, final Date modifiedAt,
    final String explanation, final String objectId, final String objectType
  ) {
    super(latLon);
    this.source = source;
    this.sourceMaintainerCode = sourceMaintainerCode;
    this.sourceMaintainerName = sourceMaintainerName;
    URL tempLocationLink = null;
    try {
      if (locationLink != null) {
        tempLocationLink = new URL(locationLink);
      }
    } catch (MalformedURLException e) {
      Logging.error("URL is malformed", e);
    }
    this.locationLink = tempLocationLink;
    this.reportNumber = reportNumber;
    this.reportNumberFull = reportNumberFull;
    this.description = description;
    this.product = product;
    this.status = status;
    this.statusCode = statusCode;
    this.reportedAt = reportedAt;
    this.statusModifiedAt = statusModifiedAt;
    this.modifiedAt = modifiedAt;
    this.explanation = explanation;
    this.objectId = objectId;
    this.objectType = objectType;
  }

  public String getSource() {
    return source;
  }

  public String getSourceMaintainerCode() {
    return sourceMaintainerCode;
  }

  public String getSourceMaintainerName() {
    return sourceMaintainerName;
  }

  public URL getLocationLink() {
    return locationLink;
  }

  public Long getReportNumber() {
    return reportNumber;
  }

  /**
   * Returns the unique identifier of the object.
   *
   * @return A {@code String} containing the unique identifier of the object.
   */
  public String getReportNumberFull() {
    return reportNumberFull;
  }

  public String getProduct() {
    return product;
  }

  public String getStatus() {
    return status;
  }

  public String getStatusCode() {
    return statusCode;
  }

  public Date getReportedAt() {
    return reportedAt;
  }

  public String getReportedAt(String dateformat) {
    return getDate(reportedAt, dateformat);
  }

  public Date getStatusModifiedAt() {
    return statusModifiedAt;
  }

  public String getStatusModifiedAt(String dateformat) {
    return getDate(statusModifiedAt, dateformat);
  }

  public Date getModifiedAt() {
    return modifiedAt;
  }

  public String getModifiedAt(String dateformat) {
    return getDate(modifiedAt, dateformat);
  }

  /**
   * Returns the date the picture was taken in the given format.
   *
   * @param format
   *          Format of the date. See {@link SimpleDateFormat}.
   * @return A String containing the date the picture was taken using the given format.
   * @throws NullPointerException
   *           if parameter format is <code>null</code>
   */
  public String getDate(Date date, String format) {
    final SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.US);
    formatter.setTimeZone(Calendar.getInstance().getTimeZone());
    return date == null ? "" : formatter.format(date);
  }

  public String getExplanation() {
    return explanation;
  }

  public String getObjectId() {
    return objectId;
  }

  public String getObjectType() {
    return objectType;
  }

  @Override
  public void setDescription(final String description) {
    // only applicable to ReportNewBAG
  }

  @Override
  public String toString() {
    return String.format("Report[report=%s,lat=%f,lon=%f,ca=%f]", reportNumberFull, latLon.lat(), latLon.lon());
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof ReportBAG && this.reportNumberFull.equals(((ReportBAG) object).getReportNumberFull());
  }

  @Override
  public int compareTo(AbstractReport image) {
    if (image instanceof ReportBAG) {
      return this.reportNumberFull.compareTo(((ReportBAG) image).getReportNumberFull());
    }
    return hashCode() - image.hashCode();
  }

  @Override
  public int hashCode() {
    return this.reportNumberFull.hashCode();
  }

  @Override
  public void stopMoving() {
    super.stopMoving();
  }
}

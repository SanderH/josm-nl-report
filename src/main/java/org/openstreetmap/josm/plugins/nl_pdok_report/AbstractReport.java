// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report;

import org.openstreetmap.josm.data.coor.LatLon;

/**
 * Abstract superclass for all image objects. At the moment there are just 2, {@link ReportNewBAG} and
 * {@link ReportBAG}.
 *
 * @author nokutu
 *
 */
public abstract class AbstractReport implements Comparable<AbstractReport> {
  private final String baseRegistration;
  protected String description;
  /** Position of the report. */
  protected LatLon latLon;
  /** Temporal position of the report until it is uploaded. */
  private LatLon tempLatLon;

  /**
   * When the object is being dragged in the map, the temporal position is stored here.
   */
  private LatLon movingLatLon;
  /** Whether the image must be drown in the map or not */
  private boolean visible;

  /**
   * Creates a new object in the given position and with the given direction.
   *
   * @param latLon
   *          The latitude and longitude where the picture was taken.
   */
  protected AbstractReport(final LatLon latLon) {
    this(latLon, "BAG");
  }

  /**
   * Creates a new object in the given position and with the given direction.
   *
   * @param latLon
   *          The latitude and longitude where the picture was taken.
   */
  protected AbstractReport(final LatLon latLon, final String baseRegistration) {
    this(latLon, baseRegistration, null);
  }

  /**
   * Creates a new object in the given position and with the given direction.
   *
   * @param latLon
   *          The latitude and longitude where the picture was taken.
   * @param ca
   *          The direction of the picture (0 means north).
   */
  protected AbstractReport(final LatLon latLon, final String baseRegistration, String description) {
    this.baseRegistration = baseRegistration;
    this.latLon = latLon;
    this.tempLatLon = this.latLon;
    this.movingLatLon = this.latLon;
    this.visible = true;
    this.description = description;
  }

  /**
   * Returns the Epoch time when the image was captured.
   *
   * @return The long containing the Epoch time when the image was captured.
   */
  public String getBaseRegistration() {
    return this.baseRegistration;
  }

  public String getDescription() {
    return this.description;
  }

  /**
   * Returns a LatLon object containing the original coordinates of the object.
   *
   * @return The LatLon object with the position of the object.
   */
  public LatLon getLatLon() {
    return latLon;
  }

  /**
   * Returns a LatLon object containing the current coordinates of the object. When you are dragging the image this
   * changes.
   *
   * @return The LatLon object with the position of the object.
   */
  public LatLon getMovingLatLon() {
    return movingLatLon;
  }

  /**
   * Returns the last fixed coordinates of the object.
   *
   * @return A LatLon object containing.
   */
  public LatLon getTempLatLon() {
    return this.tempLatLon;
  }

  /**
   * Returns whether the object has been modified or not.
   *
   * @return true if the object has been modified; false otherwise.
   */
  public boolean isModified() {
    return !this.getMovingLatLon().equals(this.latLon);
  }

  /**
   * Returns whether the image is visible on the map or not.
   *
   * @return True if the image is visible; false otherwise.
   */
  public boolean isVisible() {
    return this.visible;
  }

  /**
   * Moves the image temporally to another position
   *
   * @param lonDelta
   *          The movement of the image in longitude units.
   * @param latDelta
   *          The movement of the image in latitude units.
   */
  public void move(final double lonDelta, final double latDelta) {
    this.movingLatLon = new LatLon(this.tempLatLon.getY() + latDelta, this.tempLatLon.getX() + lonDelta);
  }

  public void setLatLon(final LatLon latLon) {
    if (latLon != null) {
      this.latLon = latLon;
    }
  }

  /**
   * Sets Description of the report
   *
   * @param description
   *          Description of the report
   */
  public abstract void setDescription(final String description);

  /**
   * Set's whether the image should be visible on the map or not.
   *
   * @param visible
   *          true if the image is set to be visible; false otherwise.
   */
  public void setVisible(final boolean visible) {
    this.visible = visible;
  }

  /**
   * Called when the mouse button is released, meaning that the picture has stopped being dragged, so the temporal
   * values are saved.
   */
  public void stopMoving() {
    this.tempLatLon = this.movingLatLon;
  }
}

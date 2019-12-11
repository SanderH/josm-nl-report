// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.openstreetmap.josm.data.coor.CachedLatLon;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.geoimage.GeoImageLayer;
import org.openstreetmap.josm.gui.layer.geoimage.ImageEntry;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportUtils;

/**
 * A FeedbackImoprtedImage object represents a picture imported locally.
 *
 * @author nokutu
 *
 */
public class ReportNewBAG extends AbstractReport {

  /** The picture files. */
  protected List<File> files;

  protected final String id;

  /**
   * Creates a new FeedbackImportedImage object using as date the current date. Using when the EXIF tags doesn't contain
   * that info.
   *
   * @param latLon
   *          The latitude and longitude where the picture was taken.
   */
  public ReportNewBAG(final LatLon latLon) {
    super(latLon);
    this.id = UUID.randomUUID().toString();
  }

  /**
   * Constructs a new image from an image entry of a {@link GeoImageLayer}.
   * 
   * @param geoImage
   *          the {@link ImageEntry}, from which the corresponding fields are taken
   * @return new image
   */
  public static ReportNewBAG createInstance(final Node node) {
    if (node == null) {
      return null;
    }
    final CachedLatLon cachedCoord = new CachedLatLon(node.getCoor());
    LatLon coord = cachedCoord == null ? null : cachedCoord.getRoundedToOsmPrecision();
    if (coord == null) {
      final MapView mv = ReportPlugin.getMapView();
      coord = mv == null ? new LatLon(0, 0) : mv.getProjection().eastNorth2latlon(mv.getCenter());
    }
    return new ReportNewBAG(coord);
  }

  /**
   * Returns the pictures of the file.
   *
   * @return A {@link BufferedImage} object containing the picture, or null if the {@link File} given in the constructor
   *         was null.
   * @throws IOException
   *           If the file parameter of the object isn't an image.
   */
  public BufferedImage getImage(int index) throws IOException {
    if (this.files.size() < index) {
      return ImageIO.read(this.files.get(index));
    }
    return null;
  }

  public String getId() {
    return id;
  }

  /**
   * Returns the {@link File} object where the picture is located.
   *
   * @return The {@link File} object where the picture is located.
   */
  public File getFile(int index) {
    if (this.files.size() < index) {
      return this.files.get(index);
    }
    return null;
  }

  public int getFileCount() {
    return this.files.size();
  }

  @Override
  public void setDescription(final String description) {
    this.description = description;
  }

  @Override
  public int compareTo(AbstractReport image) {
    if (image instanceof ReportNewBAG)
      return this.id.compareTo(((ReportNewBAG) image).getId());
    return hashCode() - image.hashCode();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ReportNewBAG)) {
      return false;
    }
    ReportNewBAG other = (ReportNewBAG) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    return true;
  }
  
  @Override
  public String toString()
  {
    return description;
  }
}

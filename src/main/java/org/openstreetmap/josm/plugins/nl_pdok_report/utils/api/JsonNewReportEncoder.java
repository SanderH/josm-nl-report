// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.utils.api;

import java.util.Objects;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObjectBuilder;

import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.IdentityTransform;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportNewBAG;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportPlugin;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportProperties;

/**
 * Encodes in JSON a location changeset. Former location and compass angle (CA) are systematically provided, even if not
 * changed.
 */
public final class JsonNewReportEncoder {
  private static MathTransform transform;

  private JsonNewReportEncoder() {
    // Private constructor to avoid instantiation
  }

  public static JsonObjectBuilder encodeNewReport(ReportNewBAG report) {
    LatLon reportLatLon = reportLatLon(report.getLatLon());
    JsonObjectBuilder result = Json.createObjectBuilder();
    Objects.requireNonNull(report);
    
    result.add("type", "FeatureCollection");
    result.add("name", "TerugmeldingGeneriek");
    result
      .add("crs", Json.createObjectBuilder()
        .add("type", "name")
        .add("properties", Json.createObjectBuilder()
          .add("name", "urn:ogc:def:crs:EPSG::28992")));
    result.add(
      "features", Json.createArrayBuilder()
      .add(Json.createObjectBuilder()
        .add("type", "Feature")
        .addAll(getReport(report))
        .add("geometry", Json.createObjectBuilder()
          .add("type", "Point")
          .add("coordinates", Json.createArrayBuilder()
            .add(reportLatLon.getY())
            .add(reportLatLon.getX())
          )
        )
      )
    );
    return result;
  }

  private static JsonObjectBuilder getReport(ReportNewBAG report) {
    JsonObjectBuilder result = Json.createObjectBuilder();
    JsonObjectBuilder properties = Json.createObjectBuilder();
    
    properties.add("registratie", report.getBaseRegistration());
          // Verplicht. Keuze uit BGT,BAG,BRT of KLIC
    
    // properties.add("product", ...);
          // Optioneel. Let op: alleen gebruiken bij de registratie BRT met TOP10,TOP25,TOP50,TOP100,TOP250,TOP500 of
          // TOP1000, anders veld weglaten.
    
    properties.add("bron", ReportPlugin.getPluginVersionString());
          // Verplicht, applicatie (app, portal, site e.d.) waar de terugmelding vandaan komt.
    
    properties.add("omschrijving", report.getDescription());
          // Verplicht. Omschrijf zo duidelijk mogelijk wat er onjuist is op de locatie. Let op: deze omschrijving wordt
          // openbaar gemaakt! Geen persoonlijke gegevens invullen en minimaal 5 karakters.
    
    // properties.add("objectId", "");
          // Optioneel, het id (referentie/nummer) van het object waar de terugmelding betrekking op heeft. Let op: op dit
          // moment alleen bruikbaar bij registraties BAG en KLIC.
    
    // properties.add("objectType", "");
          // Optioneel, het type (soort) van het object waar de terugmelding betrekking op heeft. Let op: op dit moment
          // alleen bruikbaar bij registraties BAG en KLIC.
    
    // properties.add("klicmeldnummer", )
          // Verplicht bij registratie KLIC, het graaf (meld)nummer. Let op: alleen gebruiken bij de registratie KLIC,
          // anders veld weglaten.

    // properties.add("EigenVeld2", "");
          // Optioneel. U kunt diverse eigen velden naar wens toevoegen. Alleen de JSON data types Number, String en Boolean
          // zijn toegestaan. Let op: op dit moment alleen bruikbaar bij registraties BAG en KLIC.

    if (ReportProperties.USER_EMAIL.isSet()) {
      properties.add("email", ReportProperties.USER_EMAIL.get());
      // Optioneel. E-mail adres van de terugmelder waarop status updates van de bronhouder zullen worden ontvangen.
      // NB Op de acceptatie omgeving zullen er geen daadwerkelijke e-mails worden gestuurd."
    }
  
    if (!ReportProperties.USER_ORGANISATION.get().isEmpty())
    {
      properties.add("EigenVeld1", ReportProperties.USER_ORGANISATION.get());
      // Optioneel. Aanvullende informatie kunt u kwijt in extra velden: denk aan organisatie van de terugmelder, extra
      // informatie voor de bronhouder, contactinformatie of een eigen referentie van de terugmelding. Let op: op dit
      // moment alleen bruikbaar bij registraties BAG en KLIC.
    }
    
    // final JsonArrayBuilder bagChanges = Json.createArrayBuilder();
    // for (File file : report.getFile) {
    // bagChanges.add(encodeBAGChanges(img));
    // }

    result.add("properties", properties);
    
    return result;
  }
  
  /**
   * Decodes a {@link JsonArray} of exactly size 2 to a {@link LatLon} instance. The first value in the
   * {@link JsonArray} is treated as longitude, the second one as latitude.
   * 
   * @param json
   *          the {@link JsonArray} containing the two numbers
   * @return the decoded {@link LatLon} instance, or <code>null</code> if the parameter is not a {@link JsonArray} of
   *         exactly size 2 containing two {@link JsonNumber}s.
   */
  public static LatLon reportLatLon(LatLon osmLatLon) {
    final double[] result = {osmLatLon.getY(), osmLatLon.getX()};

    if (transform == null)
    {
      transform = IdentityTransform.create(2);
      try {
        CoordinateReferenceSystem osmCrs = CRS.decode("EPSG:4326");
        CoordinateReferenceSystem crsReport = CRS.decode("EPSG:28992", true);
        transform = CRS.findMathTransform(osmCrs, crsReport);
      } catch (FactoryException e) {
        throw new UnsupportedOperationException(e);
      }
    }

    try {
      transform.transform(result, 0, result, 0, 1);
    } catch (TransformException e) {
      throw new UnsupportedOperationException("Cannot transform a point from the input dataset", e);
    }
    return new LatLon(result[1], result[0]); // swap coordinated???
  }

}

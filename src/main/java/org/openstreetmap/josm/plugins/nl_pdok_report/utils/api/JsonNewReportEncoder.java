// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.utils.api;

import java.util.Objects;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;

import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.IdentityTransform;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportNewBAG;
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
    JsonObjectBuilder result = Json.createObjectBuilder();
    Objects.requireNonNull(report);
    
    result.add("type", "FeatureCollection");
    result.add("name", "TerugmeldingGeneriek");
    result.add(
      "features", Json.createArrayBuilder()
      .add(Json.createObjectBuilder()
        .add("type", "Feature")
        .addAll(getReport(report))
        .add("geometry", Json.createObjectBuilder()
          .add("type", "Point")
          .add("coordinates", Json.createArrayBuilder()
            .add(report.getLatLon().getX())
            .add(report.getLatLon().getY())
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
    
    properties.add("bron", "OpenStreetMap (JOSM plugin)"); //ReportPlugin.getPluginVersionString());
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
      properties.add("Organisatie", ReportProperties.USER_ORGANISATION.get());
      // Optioneel. Aanvullende informatie kunt u kwijt in extra velden: denk aan organisatie van de terugmelder, extra
      // informatie voor de bronhouder, contactinformatie of een eigen referentie van de terugmelding. Let op: op dit
      // moment alleen bruikbaar bij registraties BAG en KLIC.
      // Optioneel. U kunt diverse eigen velden naar wens toevoegen. Alleen de JSON data types Number, String en Boolean 
      // zijn toegestaan. In plaats van EigenVeld1 of EigenVeld2 kunt u zelf een passende naam kiezen.
    }
    
    // final JsonArrayBuilder bagChanges = Json.createArrayBuilder();
    // for (File file : report.getFile) {
    // bagChanges.add(encodeBAGChanges(img));
    // }

    result.add("properties", properties);
    
    return result;
  }
  
  /**
   * Encodes a {@link LatLon} with projection EPSG:4326 to a {@link LatLon} with projection EPSG:28992.
   * 
   * @param osmLatLon
   *          the {@link LatLon} containing coordinate in EPSG:4326
   * @return the encoded {@link LatLon} coordinate in EPSG:28992.
   */
  public static LatLon reportLatLon(LatLon osmLatLon) {
    final double[] result = {osmLatLon.getX(), osmLatLon.getY()};

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

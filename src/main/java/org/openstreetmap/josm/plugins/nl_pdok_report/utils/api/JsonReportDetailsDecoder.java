// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.utils.api;

import java.util.Date;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportBAG;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportData;

/**
 * Decodes the JSON returned by {@link APIv3} into Java objects. Takes a {@link JsonObject} and
 * {@link #decodeImageInfos(JsonObject, ReportData)} tries to add the timestamps.
 */
public final class JsonReportDetailsDecoder {
  private JsonReportDetailsDecoder() {
    // Private constructor to avoid instantiation
  }

  public static void decodeReportInfos(final JsonObject json, final ReportData data) {
    if (data != null) {
      JsonDecoder.decodeFeatureCollection(json, j -> {
        decodeReportInfo(j, data);
        return null;
      });
    }
  }

  private static void decodeReportInfo(final JsonObject json, final ReportData data) {
    if (json != null && data != null) {
      JsonValue geometry = json.get("geometry");
      if (geometry instanceof JsonObject) {
        JsonArray coordinates = ((JsonObject) geometry).get("coordinates").asJsonArray();
        LatLon latLon = JsonDecoder.decodeLatLon(coordinates);

        JsonValue properties = json.get("properties");
        if (properties instanceof JsonObject) {
          String baseRegistration = ((JsonObject) properties).getString("basisregistratie", null);
          String source = ((JsonObject) properties).getString("bron", null);
          String sourceMaintainerCode = ((JsonObject) properties).getString("bronhoudercode", null);
          String sourceMaintainerName = ((JsonObject) properties).getString("bronhoudernaam", null);
          String locationLink = ((JsonObject) properties).getString("locatieLink", null);
          Long reportNumber = ((JsonObject) properties).getJsonNumber("meldingsNummer").longValueExact();
          String reportNumberFull = ((JsonObject) properties).getString("meldingsNummerVolledig", null);
          String description = ((JsonObject) properties).getString("omschrijving", null);
          String product = ((JsonObject) properties).getString("product", null);
          String status = ((JsonObject) properties).getString("status", null);
          String statusCode = ((JsonObject) properties).getString("statusCode", null);
          Date reportedAt = JsonDecoder.decodeDate(((JsonObject) properties).getString("tijdstipRegistratie", null));
          Date statusModifiedAt = JsonDecoder.decodeDate(((JsonObject) properties).getString("tijdstipStatusWijziging", null));
          Date modifiedAt = JsonDecoder.decodeDate(((JsonObject) properties).getString("tijdstipWijziging", null));
          String explanation = ((JsonObject) properties).getString("toelichting", null);
          String objectId = ((JsonObject) properties).getString("objectId", null);
          String objectType = ((JsonObject) properties).getString("objectType", null);

          if (reportNumber != null && reportedAt != null) {
            data.add(
              new ReportBAG(
                latLon, baseRegistration, source, sourceMaintainerCode, sourceMaintainerName, locationLink,
                reportNumber, reportNumberFull, description, product, status, statusCode, reportedAt, statusModifiedAt,
                modifiedAt, explanation, objectId, objectType
              )
            );

            // add to data collection
            // data.getReports().stream().filter(
            // img -> img instanceof ReportBAG && key.equals(((ReportBAG) img).getKey())
            // ).forEach(img -> img.setReportedAt(reportedAt));
          }
        }
      }
    }
  }
}

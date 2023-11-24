// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.utils.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.function.Function;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.IdentityTransform;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.tools.Logging;

public final class JsonDecoder {
  private static MathTransform transform;

  private JsonDecoder() {
    // Private constructor to avoid instantiation
  }

  /**
   * Parses a given {@link JsonObject} as a GeoJSON FeatureCollection into a {@link Collection} of the desired Java
   * objects. The method, which converts the GeoJSON features into Java objects is given as a parameter to this method.
   * 
   * @param <T>
   *          feature type
   * @param json
   *          the {@link JsonObject} to be parsed
   * @param featureDecoder
   *          feature decoder which transforms JSON objects to Java objects
   * @return a {@link Collection} which is parsed from the given {@link JsonObject}, which contains GeoJSON. Currently a
   *         {@link HashSet} is used, but please don't rely on it, this could change at any time without prior notice.
   *         The return value will not be <code>null</code>.
   */
  public static <T> Collection<T> decodeFeatureCollection(
    final JsonObject json, Function<JsonObject, T> featureDecoder
  ) {
    final Collection<T> result = new HashSet<>();
    if (json != null && "FeatureCollection".equals(json.getString("type", null)) && json.containsKey("features")) {
      final JsonValue features = json.get("features");
      for (int i = 0; features instanceof JsonArray && i < ((JsonArray) features).size(); i++) {
        final JsonValue val = ((JsonArray) features).get(i);
        if (val instanceof JsonObject) {
          final T feature = featureDecoder.apply((JsonObject) val);
          if (feature != null) {
            result.add(feature);
          }
        }
      }
    }
    return result;
  }

  public static void decodeCRS(final JsonObject json) {
    transform = IdentityTransform.create(2);
    JsonValue crs = json.get("crs");
    JsonValue properties = ((JsonObject) crs).get("properties");
    String crsName = ((JsonObject) properties).getString("name");

    try {
      CoordinateReferenceSystem crsReport = CRS.decode(crsName, true);
      CoordinateReferenceSystem osmCrs = CRS.decode("EPSG:4326");
      transform = CRS.findMathTransform(crsReport, osmCrs);
    } catch (FactoryException e) {
      throw new UnsupportedOperationException("Unknown CRS " + crsName, e);
    }
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
  static LatLon decodeLatLon(final JsonArray json) {
    final double[] result = decodeDoublePair(json);
    try {
      transform.transform(result, 0, result, 0, 1);
    } catch (TransformException e) {
      throw new UnsupportedOperationException("Cannot transform a point from the input dataset", e);
    }
    if (result != null) {
      return new LatLon(result[1], result[0]);  // swap coordinated???
    }
    return null;
  }

  /**
   * Decodes a pair of double values, which are stored in a {@link JsonArray} of exactly size 2.
   * 
   * @param json
   *          the {@link JsonArray} containing the two values
   * @return a double array which contains the two values in the same order, or <code>null</code> if the parameter was
   *         not a {@link JsonArray} of exactly size 2 containing two {@link JsonNumber}s
   */
  static double[] decodeDoublePair(final JsonArray json) {
    if (json != null && json.size() == 2 && json.get(0) instanceof JsonNumber && json.get(1) instanceof JsonNumber) {
      return new double[] { json.getJsonNumber(0).doubleValue(), json.getJsonNumber(1).doubleValue() };
    }
    return null;
  }

  /**
   * Decodes a timestamp formatted as a {@link String} to the equivalent UNIX epoch timestamp (number of milliseconds
   * since 1970-01-01T00:00:00.000+0000).
   * 
   * @param timestamp
   *          the timestamp formatted according to the format <code>yyyy-MM-dd'T'HH:mm:ss.SSSSSSX</code> or <code>yyyy-MM-dd'T'HH:mm:ss.SSSX</code> or <code>yyyy-MM-dd'T'HH:mm:ss</code>
   * @return the point in time as a {@link Long} value representing the UNIX epoch time, or <code>null</code> if the
   *         parameter does not match the required format (this also triggers a warning via
   *         {@link Logging#warn(Throwable)}), or the parameter is <code>null</code>.
   */
  static Long decodeTimestamp(final String timestamp) {
    if (timestamp != null) {
      try {
    	if (timestamp.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{6}[\\+-]\\d{2}:\\d{2}$"))
    	{
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX", Locale.UK).parse(timestamp).getTime();
    	}
    	else if (timestamp.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}[\\+-]\\d{2}:\\d{2}$"))
        {
          return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.UK).parse(timestamp).getTime();
        }
        else if (timestamp.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[\\+-]\\d{2}:\\d{2}$"))
        {
          return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.UK).parse(timestamp).getTime();
        }
        else
        {
          throw new ParseException("Could not decode time from the timestamp `%s`", 0);
        }
      } catch (ParseException e) {
        StackTraceElement calledBy = e.getStackTrace()[Math.min(e.getStackTrace().length - 1, 2)];
        Logging.log(
          Logging.LEVEL_WARN,
          String.format(
            "Could not decode time from the timestamp `%s` (called by %s.%s:%d)", timestamp, calledBy.getClassName(),
            calledBy.getMethodName(), calledBy.getLineNumber()
          ), e
        );
      }
    }
    return null;
  }

  /**
   * Decodes a date formatted as a {@link String} to the equivalent UNIX epoch timestamp (number of milliseconds since
   * 1970-01-01T00:00:00.000+0000).
   * 
   * @param date
   *          the date formatted according to the format <code>yyyy-MM-dd'T'HH:mm:ss.SSSSSSX</code> or <code>yyyy-MM-dd'T'HH:mm:ss.SSSX</code> or <code>yyyy-MM-dd'T'HH:mm:ssX</code>
   * @return the point in time as a {@link Long} value representing a {@link Date}, or <code>null</code> if the
   *         parameter does not match the required format (this also triggers a warning via
   *         {@link Logging#warn(Throwable)}), or the parameter is <code>null</code>.
   */
  static Date decodeDate(final String date) {
    if (date != null) {
      try {
        if (date.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{6}[\\+-]\\d{2}:\\d{2}$"))
        {
          return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX", Locale.UK).parse(date);
        }
        else if (date.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}[\\+-]\\d{2}:\\d{2}$"))
        {
          return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.UK).parse(date);
        }
        else if (date.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[\\+-]\\d{2}:\\d{2}$"))
        {
          return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.UK).parse(date);
        }
        else
        {
          throw new ParseException("Could not decode time from the timestamp `%s`", 0);
        }
      } catch (ParseException e) {
        StackTraceElement calledBy = e.getStackTrace()[Math.min(e.getStackTrace().length - 1, 2)];
        Logging.log(
          Logging.LEVEL_WARN,
          String.format(
            "Could not decode time from the timestamp `%s` (called by %s.%s:%d)", date, calledBy.getClassName(),
            calledBy.getMethodName(), calledBy.getLineNumber()
          ), e
        );
      }
    }
    return null;
  }
}

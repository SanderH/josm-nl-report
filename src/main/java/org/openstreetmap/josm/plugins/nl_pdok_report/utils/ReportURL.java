// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.utils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportProperties.REPORT_API;
import org.openstreetmap.josm.tools.Logging;

public final class ReportURL {
  /** Base URL of the Report API. */
  private static final String DEFAULT_API_URL = "https://api.kadaster.nl/tms/v1/terugmeldingen";
  private static final String DEFAULT_API_URL_ACT = "https://api.acceptatie.kadaster.nl/tms/v1/terugmeldingen";
  private static final String REQUEST_API_KEY_URL = "https://formulieren.kadaster.nl/aanvragen_api_key_terumelding_api";
  private static final SimpleDateFormat API_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  private static final String REPORT_TYPE = "BAG";
  private static final String REPORT_STATUS_CODES = "NIEUW,IN_ONDERZOEK,GOEDGEKEURD,AFGEWEZEN,AFGEROND";
  private static final String DEFAULT_API_PROXY_URL = "https://terugmeldingen.proxy.tools4osm.nl/v1";
  private static final String DEFAULT_API_PROXY_URL_ACT = "https://terugmeldingen.proxy.tools4osm.nl/act/v1";

  private ReportURL() {
    // Private constructor to avoid instantiation
  }

  /**
   * @return the URL where you'll find an empty result usefull for validating the API key

   */
  private static String getBaseApiUrl() {
    return getBaseApiUrl(REPORT_API.fromPrefId(ReportProperties.API_REPORT_USE.get()));
  }
  
  /**
   * @return the URL where you'll find an empty result usefull for validating the API key
   * 
   * @param act Validate acceptance environment instead of production
   */
  private static String getBaseApiUrl(REPORT_API reportApi) {

    switch (reportApi)
    {
      default:
      case PDOK_PRODUCTION:
        if (ReportProperties.API_URL.isSet()) {
          return ReportProperties.API_URL.get();
        }
        return DEFAULT_API_URL;
      case PDOK_ACCEPTANCE:
        if (ReportProperties.API_URL_ACT.isSet()) {
          return ReportProperties.API_URL_ACT.get();
        }
        return DEFAULT_API_URL_ACT;
      case PROXY_PRODUCTION:
        if (ReportProperties.API_PROXY_URL.isSet()) {
          return ReportProperties.API_PROXY_URL.get();
        }
        return DEFAULT_API_PROXY_URL;
      case PROXY_ACCEPTANCE:
        if (ReportProperties.API_PROXY_URL_ACT.isSet()) {
          return ReportProperties.API_PROXY_URL_ACT.get();
        }
        return DEFAULT_API_PROXY_URL_ACT;
    }

  }

  public static URL requestApiKeyURL() {
    return string2URL(REQUEST_API_KEY_URL, "", "");
  }

  /**
   * @return the URL where you'll find the reports as JSON
   */
  public static URL downloadReportURL(Bounds bounds) {
    Map<String, String> querystring = new HashMap<>();
    querystring.put("peildatum", API_DATE_FORMAT.format(new Date(System.currentTimeMillis())));
    querystring.put("registratie", REPORT_TYPE);
    querystring.put("statusCode", REPORT_STATUS_CODES);

    return string2URL(getBaseApiUrl(), "", queryString(querystring));
  }

  /**
   * @return the URL where you'll find an empty result usefull for validating the API key
   * 
   * @param act Validate acceptance environment instead of production
   */
  public static URL validateApiURL(REPORT_API reportApi) {
    Map<String, String> querystring = new HashMap<>();
    querystring.put("peildatum", "1000-01-01");
    querystring.put("registratie", REPORT_TYPE);
    querystring.put("statusCode", REPORT_STATUS_CODES);

    return string2URL(getBaseApiUrl(reportApi), "", queryString(querystring));
  }

  /**
   * @return the URL where you can submit feedback
   */
  public static URL submitReport() {
    return string2URL(getBaseApiUrl(), "", queryString(null));
  }

  /**
   * Builds a query string from it's parts that are supplied as a {@link Map}
   * 
   * @param parts
   *          the parts of the query string
   * @return the constructed query string (including a leading ?)
   */
  static String queryString(Map<String, String> parts) {
    StringBuilder ret = new StringBuilder("?");
    if (parts != null) {
      for (Entry<String, String> entry : parts.entrySet()) {
        try {
          ret.append('&').append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name())).append('=')
            .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
          Logging.error(e); // This should not happen, as the encoding is hard-coded
        }
      }
    }
    return ret.toString();
  }

  /**
   * Converts a {@link String} into a {@link URL} without throwing a {@link MalformedURLException}. Instead such an
   * exception will lead to an {@link Logging#error(Throwable)}. So you should be very confident that your URL is
   * well-formed when calling this method.
   * 
   * @param strings
   *          the Strings describing the URL
   * @return the URL that is constructed from the given string
   */
  static URL string2URL(String... strings) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; strings != null && i < strings.length; i++) {
      builder.append(strings[i]);
    }
    try {
      return new URL(builder.toString());
    } catch (MalformedURLException e) {
      Logging.log(
        Logging.LEVEL_ERROR,
        String.format("The class '%s' produces malformed URLs like '%s'!", ReportURL.class.getName(), builder), e
      );
      return null;
    }
  }

}

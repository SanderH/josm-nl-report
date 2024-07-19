// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.io.download;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.Function;

import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonReader;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportData;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportURL;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.api.JsonReportDetailsDecoder;

public class ReportDownloadRunnable extends BoundsDownloadRunnable {
  private static final Function<Bounds, URL> URL_GEN = ReportURL::downloadReportURL;

  private final ReportData data;

  public ReportDownloadRunnable(final ReportData data, final Bounds bounds) {
    super(bounds);
    this.data = data;
  }

  @Override
  public void run(final URLConnection con) throws IOException {
    if (Thread.interrupted()) {
      return;
    }
    try (JsonReader reader = Json.createReader(new BufferedInputStream(con.getInputStream()))) {
      JsonReportDetailsDecoder.decodeReportInfos(reader.readObject(), data);
      logConnectionInfo(con, null);
      // sho ReportMainDialog.getInstance().updateTitle();
    } catch (JsonException | NumberFormatException e) {
      throw new IOException(e);
    }
  }

  @Override
  protected Function<Bounds, URL> getUrlGenerator() {
    return URL_GEN;
  }

}

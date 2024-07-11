// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.StringReader;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.plugins.nl_pdok_report.AbstractReport;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportLayer;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportNewBAG;
import org.openstreetmap.josm.plugins.nl_pdok_report.gui.ReportNewDialog;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportProperties;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportProperties.REPORT_API;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportURL;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportUtils;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.MultipartUtility;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.MultipartUtility.MultiPartResponse;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.PluginState;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.api.JsonNewReportEncoder;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.ImageProvider.ImageSizes;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Imports a set of picture files into JOSM. They must be in jpg or png format.
 */
public class ReportUploadNewAction extends JosmAction {
  private static final long serialVersionUID = 2425751012148709519L;
  private final ReportNewDialog changesetDialog;

  /**
   * Main constructor.
   * 
   * @param changesetDialog
   *          Report upload dialog
   */
  public ReportUploadNewAction(ReportNewDialog changesetDialog) {
    super(
      I18n.tr("Submit report"), new ImageProvider("dialogs", "report-upload").setSize(ImageSizes.DEFAULT), I18n.tr("Submit the current report"),
      // CHECKSTYLE.OFF: LineLength
      Shortcut.registerShortcut("Submit report", I18n.tr("Submit the current report"), KeyEvent.CHAR_UNDEFINED, Shortcut.NONE),
      // CHECKSTYLE.ON: LineLength
      false, "reportSubmitChangeset", false
    );
    this.changesetDialog = changesetDialog;
    setEnabled(false);
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    new Thread(() -> {
      changesetDialog.setUploadPending(true);
      
      REPORT_API reportApi = REPORT_API.fromPrefId(ReportProperties.API_REPORT_USE.get());
      String token = reportApi.getToken();
      
//      String token = ReportProperties.USE_ACT_API.get() ? ReportProperties.API_KEY_ACT.get() : ReportProperties.API_KEY.get();
      if ((token != null && !token.trim().isEmpty()) || !reportApi.needsKey()) {
        PluginState.setSubmittingChangeset(true);
        ReportUtils.updateHelpText();
        List<AbstractReport> newReports = ReportLayer.getInstance().getData().getNewReports();
        newReports.forEach((tmpReport) -> {
          ReportNewBAG newReport = ((ReportNewBAG) tmpReport);

          String json = JsonNewReportEncoder.encodeNewReport(newReport).build().toString();
          try
          {
            MultipartUtility multipart = new MultipartUtility(ReportURL.submitReport(), "UTF-8", ReportProperties.USE_FIDDLER.get());
            multipart.setRequestProperty("Accept", "application/json");
            multipart.setRequestProperty("Content-Crs",  ReportURL.getCrs4326());
            multipart.setRequestProperty("User-Agent", "JOSM");
            if (reportApi.needsKey()) {
              multipart.setRequestProperty("apikey", token);
            }
            //multipart.addFilePart("files", <file>);
            multipart.StartStream();
            multipart.addFormField("melding", json.toString(), "application/json");
            Logging.info("Sending JSON to " + ReportURL.submitReport() + "\n  " + json);
            MultiPartResponse response = multipart.finish();

            Logging.debug("HTTP request finished with response code " + response.ResponseCode);
            switch (response.ResponseCode)
            {
            case 200: // old v1, it's 201 with v2
              final JsonObject jsonObject = Json.createReader(new StringReader(response.Message.toString())).readObject();
              final String reportNumberFull = jsonObject.getString("meldingsNummerVolledig");
              I18n.marktr("rejected");
              I18n.marktr("pending");
              I18n.marktr("approved");
              final String message = I18n
                .tr("Report submitted. Registration number: {0}", reportNumberFull);
              Logging.debug(message);
              ReportLayer.getInstance().getData().remove(newReport);
              break;
            case 201:
            	final String reportLocation = response.ResponseReference;
                final String messageLocation = I18n
                .tr("Report submitted. Registration number: {0}", reportLocation);
              Logging.info(messageLocation);
              ReportLayer.getInstance().getData().remove(newReport);
            	break;
            case 400:
            case 401: 
              final JsonObject jsonObjectFail = Json.createReader(new StringReader(response.Message.toString())).readObject();
              final JsonArray reasonFails = jsonObjectFail.get("reden").asJsonArray();
              final StringBuilder reasonFailMessage = new StringBuilder();
              reasonFails.forEach((reasonFail) -> {
                reasonFailMessage.append(System.lineSeparator() + reasonFail);
              });
              new Notification(
                I18n.tr(
                  "Report upload failed with {0} error ''{1}''{2}!", 
                  response.ResponseCode, response.ResponseMessage, reasonFailMessage.toString()
                )
              ).setIcon(JOptionPane.ERROR_MESSAGE).setDuration(Notification.TIME_LONG).show();
              Logging.error("Failed reason: " + reasonFailMessage.toString());
              break;
            default:
              Logging.debug("HTTP request finished with unexpected response code " + response.ResponseCode + " " + response.ResponseMessage);
              Logging.error("Failed reason: " + response.Message.toString());
            }
          }
          catch (Exception e)
          {
            Logging.log(Logging.LEVEL_ERROR, "Exception while trying to submit report to kadaster.nl", e);
            new Notification(
              I18n.tr(
                "An exception occured while trying to submit a report. If this happens repeatedly, consider reporting a bug via the Help menu. If this message appears for the first time, simply try it again. This might have been an issue with the internet connection."
              )
            ).setDuration(Notification.TIME_LONG).setIcon(JOptionPane.ERROR_MESSAGE).show();
          }
          finally {
            PluginState.setSubmittingChangeset(false);
          }
        });
      } else {
        PluginState.notLoggedInToReportDialog();
      }
      changesetDialog.setUploadPending(false);
    }, "Report submit").start();
  }
}

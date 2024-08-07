// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.utils;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

/**
 * This utility class provides an abstraction layer for sending multipart HTTP POST requests to a web server.
 * 
 * @author www.codejava.net
 * @see {@link https://www.codejava.net/java-se/networking/upload-files-by-sending-multipart-request-programmatically}
 */
public class MultipartUtility {
  private final String boundary;
  private static final String LINE_FEED = "\r\n";
  private HttpURLConnection httpConn;
  private String charset;
  private OutputStream outputStream;
  private PrintWriter writer;

  /**
   * This constructor initializes a new HTTP POST request with content type is set to multipart/form-data
   * 
   * @param requestURL
   * @param charset
   * @param useFiddlerProxy use local Fiddler proxy for debugging
   * @throws IOException
   */
  public MultipartUtility(URL requestURL, String charset, boolean useFiddlerProxy) throws IOException {
    this.charset = charset;

    // creates a unique boundary based on time stamp
    boundary = "+++" + System.currentTimeMillis() + "+++";

    if (useFiddlerProxy)
    {
      Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8888));
      httpConn = (HttpURLConnection) requestURL.openConnection(proxy);
      TrustFiddlerSSL();
    }
    else
    {
      httpConn = (HttpURLConnection) requestURL.openConnection();
    }
    httpConn.setUseCaches(false);
    httpConn.setDoOutput(true); // indicates POST method
    httpConn.setDoInput(true);
    httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
  }

  /**
   * After setting additional Request Properties use StartStream
   * 
   * @throws IOException
   */
  public void StartStream() throws IOException {
    outputStream = httpConn.getOutputStream();
    writer = new PrintWriter(new OutputStreamWriter(outputStream, this.charset), true);
  }

  /**
   * Adds a form field to the request
   * 
   * @param name
   *          field name
   * @param value
   *          field value
   */
  public void addFormField(String name, String value, String contenttype) {
    writer.append("--" + boundary).append(LINE_FEED);
    writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(LINE_FEED);
    writer.append("Content-Type: " + contenttype + "; charset=" + charset).append(LINE_FEED);
    writer.append(LINE_FEED);
    writer.append(value).append(LINE_FEED);
    writer.flush();
  }

  /**
   * Adds a upload file section to the request
   * 
   * @param fieldName
   *          name attribute in <input type="file" name="..." />
   * @param uploadFile
   *          a File to be uploaded
   * @throws IOException
   */
  public void addFilePart(String fieldName, File uploadFile) throws IOException {
    String fileName = uploadFile.getName();
    writer.append("--" + boundary).append(LINE_FEED);
    writer.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"")
      .append(LINE_FEED);
    writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
    writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
    writer.append(LINE_FEED);
    writer.flush();

    FileInputStream inputStream = new FileInputStream(uploadFile);
    byte[] buffer = new byte[4096];
    int bytesRead = -1;
    while ((bytesRead = inputStream.read(buffer)) != -1) {
      outputStream.write(buffer, 0, bytesRead);
    }
    outputStream.flush();
    inputStream.close();

    writer.append(LINE_FEED);
    writer.flush();
  }

  /**
   * Adds a header field to the request.
   * 
   * @param name
   *          - name of the header field
   * @param value
   *          - value of the header field
   */
  public void addHeaderField(String name, String value) {
    writer.append(name + ": " + value).append(LINE_FEED);
    writer.flush();
  }

  public void setRequestProperty(String name, String value) {
    httpConn.setRequestProperty(name, value);
  }

  /**
   * Completes the request and receives response from the server.
   * 
   * @return a list of Strings as response in case the server returned status OK, otherwise an exception is thrown.
   * @throws IOException
   */
  public MultiPartResponse finish() throws IOException {
    MultiPartResponse response = new MultiPartResponse();
    response.Message = new StringBuilder();

    writer.append(LINE_FEED).flush();
    writer.append("--" + boundary + "--").append(LINE_FEED);
    writer.close();

    // checks server's status code first
    response.ResponseCode = httpConn.getResponseCode();
    response.ResponseMessage = httpConn.getResponseMessage();
    if (response.ResponseCode == HttpURLConnection.HTTP_OK ||
    	response.ResponseCode == HttpURLConnection.HTTP_CREATED)
    {
      BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
      String line = null;
      while ((line = reader.readLine()) != null) {
        response.Message.append(line);
      }
      reader.close();
      response.ResponseReference = httpConn.getHeaderField("Location");
      httpConn.disconnect();
    } else if (response.ResponseCode == HttpURLConnection.HTTP_BAD_REQUEST || 
               response.ResponseCode == HttpURLConnection.HTTP_UNAUTHORIZED)
    {
      BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()));
      String line = null;
      while ((line = reader.readLine()) != null) {
        response.Message.append(line);
      }
      reader.close();
      httpConn.disconnect();
    } else {
      throw new IOException(
        MessageFormat
          .format("Server returned non-OK status: ''{0}'' ''{1}''", response.ResponseCode, response.ResponseMessage)
      );
    }

    return response;
  }

  /**
   * Only to be called when using Fiddler proxy
   */
  public static void TrustFiddlerSSL() {
    try {
      // Create a trust manager that does not validate certificate chains
      TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
      } };

      // Install the all-trusting trust manager
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

      // Create all-trusting host name verifier
      HostnameVerifier allHostsValid = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      };

      // Install the all-trusting host verifier
      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    } catch (Exception e) {
      //
    }
  }

  public class MultiPartResponse {
    public int ResponseCode;
    public String ResponseMessage;
    public String ResponseReference;
    public StringBuilder Message;
  }

}
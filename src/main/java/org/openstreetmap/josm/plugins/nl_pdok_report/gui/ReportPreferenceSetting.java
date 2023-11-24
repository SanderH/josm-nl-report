// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.net.HttpURLConnection;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openstreetmap.josm.actions.ExpertToggleAction;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.gui.preferences.SubPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.TabPreferenceSetting;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportPlugin;
import org.openstreetmap.josm.plugins.nl_pdok_report.gui.boilerplate.ReportButton;
import org.openstreetmap.josm.plugins.nl_pdok_report.gui.reportinfo.WebLinkAction;
import org.openstreetmap.josm.plugins.nl_pdok_report.oauth.ReportLoginListener;
import org.openstreetmap.josm.plugins.nl_pdok_report.oauth.ReportUser;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportColorScheme;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportProperties;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportProperties.REPORT_API;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportURL;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;

/**
 * Creates the preferences panel for the plugin.
 *
 * @author SanderH
 *
 */
public class ReportPreferenceSetting implements SubPreferenceSetting, ReportLoginListener {
  private static final int BRAND_LOGO_SIZE = 48;
  private static final int TEXT_COLUMNS = 40;
  private static final int INDENT = 20;

  private final JComboBox<String> reportApiComboBox = new JComboBox<>(
    new String[] { REPORT_API.PDOK_PRODUCTION.getLabel(), REPORT_API.PROXY_PRODUCTION.getLabel() }
  );

  private final JLabel apiUrlLabel = new JLabel(I18n.tr("URL of the PDOK report API"));
  protected final JTextField apiUrl = new JTextField(ReportProperties.API_URL.get(), TEXT_COLUMNS);

  private final JLabel apiProxyUrlLabel = new JLabel(I18n.tr("URL of the PDOK report API (proxy)"));
  protected final JTextField apiProxyUrl = new JTextField(ReportProperties.API_PROXY_URL.get(), TEXT_COLUMNS);
  
  private final JLabel apiKeyLabel = new JLabel(I18n.tr("API key for accessing the PDOK report API"));
  protected final JTextField apiKey = new JTextField(ReportProperties.API_KEY.get(), TEXT_COLUMNS);
  private final WebLinkAction requestApiKeyLink = new WebLinkAction(I18n.tr("Request API key"), null);
  private final ReportButton apiKeyRequest = new ReportButton(requestApiKeyLink);
  
  private final JLabel emailLabel = new JLabel(I18n.tr("Email address for getting notifications"));
  private final JTextField email = new JTextField(ReportProperties.USER_EMAIL.get(), TEXT_COLUMNS);
  
  private final JLabel orgLabel = new JLabel(I18n.tr("Optional organisation for the report"));
  private final JTextField org = new JTextField(ReportProperties.USER_ORGANISATION.get(), TEXT_COLUMNS);
  
  private final JCheckBox selectFromOtherLayer = new JCheckBox(I18n.tr("Select report from other layer"), ReportProperties.SELECT_FROM_OTHER_LAYER.get());
  
  private final JCheckBox developer = new JCheckBox(I18n.tr("Enable experimental beta-features (might be unstable)"), ReportProperties.DEVELOPER.get());
  private final JLabel apiUrlActLabel = new JLabel(I18n.tr("URL of the PDOK Report API (acceptance environment)"));
  protected final JTextField apiUrlAct = new JTextField(ReportProperties.API_URL_ACT.get(), TEXT_COLUMNS);
  private final JLabel apiKeyActLabel = new JLabel(I18n.tr("API key for accessing the PDOK Report API (acceptance environment)"));
  protected final JTextField apiKeyAct = new JTextField(ReportProperties.API_KEY_ACT.get(), TEXT_COLUMNS);
  private final ReportButton apiKeyActRequest = new ReportButton(requestApiKeyLink);

  private final JLabel apiProxyUrlActLabel = new JLabel(I18n.tr("URL of the PDOK Report API (proxy acceptance environment)"));
  protected final JTextField apiProxyUrlAct = new JTextField(ReportProperties.API_PROXY_URL_ACT.get(), TEXT_COLUMNS);

  private final JCheckBox fiddler = new JCheckBox(I18n.tr("Use Fiddler proxy (on local default port)"), ReportProperties.USE_FIDDLER.get());

  private final JButton validateButton = new ReportButton(new ValidateAction());
  private final JButton validateActButton = new ReportButton(new ValidateActAction());
  private final JLabel infoLabel = new JLabel(I18n.tr("Unofficial implementation of the PDOK Report API"));
  private final JPanel headerPanel = new JPanel();

  @Override
  public TabPreferenceSetting getTabPreferenceSetting(PreferenceTabbedPane gui) {
    return gui.getDisplayPreference();
  }

  @Override
  public void addGui(PreferenceTabbedPane gui) {
    JPanel container = new JPanel(new BorderLayout());

    headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.LINE_AXIS));
    headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    headerPanel.setBackground(ReportColorScheme.TOOLBAR_DARK_GREEN);
    JLabel brandImage = new JLabel();
    ImageProvider ip;
    try{
      ip = new ImageProvider("report-logo");
      if (ip != null) {
        brandImage.setIcon(ip.setMaxSize(BRAND_LOGO_SIZE).get());
        headerPanel.add(brandImage, 0);
      } else {
        Logging.warn("Could not load Report plugin brand image!");
      }
    }
    finally
    {
      ip = null;
    }
    headerPanel.add(Box.createHorizontalStrut(20), 1);
    infoLabel.setForeground(Color.WHITE);
    infoLabel.setFont(new Font(infoLabel.getFont().getName(), Font.BOLD, 20));
    headerPanel.add(infoLabel, 2);
    headerPanel.add(Box.createHorizontalGlue(), 3);
    //loginLabel.setForeground(Color.WHITE);
    //loginLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
    //headerPanel.add(loginLabel, 4);
    //headerPanel.add(loginButton, 5);
    onLogout();
    onLogoutAct();
    container.add(headerPanel, BorderLayout.NORTH);

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new GridBagLayout());
    mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

//    downloadModeComboBox.setSelectedItem(DOWNLOAD_MODE.fromPrefId(ReportProperties.DOWNLOAD_MODE.get()).getLabel());
    if (ExpertToggleAction.isExpert() || developer.isSelected()) {
      reportApiComboBox.insertItemAt(REPORT_API.PDOK_ACCEPTANCE.getLabel(), 1);
      reportApiComboBox.addItem(REPORT_API.PROXY_ACCEPTANCE.getLabel());
    }
    reportApiComboBox.setSelectedItem(REPORT_API.fromPrefId(ReportProperties.API_REPORT_USE.get()).getLabel());
    enableControls();
    reportApiComboBox.addItemListener(new ItemListener() {
      // Listening if a new items of the combo box has been selected.
      @Override
      public void itemStateChanged(ItemEvent event) {
        enableControls();
      }
    });
    
    apiKey.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void changedUpdate(DocumentEvent e) {
        onLogout();
      }
      @Override
      public void removeUpdate(DocumentEvent e) {
        //
      }
      @Override
      public void insertUpdate(DocumentEvent e) {
       //
      }
    });
    apiKeyAct.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void changedUpdate(DocumentEvent e) {
        onLogoutAct();
      }
      @Override
      public void removeUpdate(DocumentEvent e) {
        //
      }
      @Override
      public void insertUpdate(DocumentEvent e) {
       //
      }
    });
    
    requestApiKeyLink.setURL(ReportURL.requestApiKeyURL());
    
    //JPanel downloadModePanel = new JPanel();
    //downloadModePanel.add(new JLabel(I18n.tr("Download mode")));
    //downloadModePanel.add(downloadModeComboBox);
//    mainPanel.add(downloadModePanel, GBC.eol());

    JPanel reportApiPanel = new JPanel();
    reportApiPanel.add(new JLabel(I18n.tr("Use Reports API")));
    reportApiPanel.add(reportApiComboBox);
    reportApiPanel.add(new JLabel(I18n.tr("Use the official PDOK API with your own API key or a reverse proxy to the official PDOK API with someone else''s key")));
    mainPanel.add(reportApiPanel, GBC.eol());
    
    mainPanel.add(apiUrlLabel, GBC.std());
    mainPanel.add(apiUrl, GBC.eol().fill(GridBagConstraints.HORIZONTAL).insets(0, 0, 0, 0));
    
    mainPanel.add(apiKeyLabel, GBC.std().insets(0, 0, 0, 0));
    mainPanel.add(apiKey, GBC.std().fill(GridBagConstraints.HORIZONTAL));
    mainPanel.add(apiKeyRequest, GBC.std().insets(5, 0, 0, 0));
    mainPanel.add(validateButton, GBC.eol().insets(5, 0, 0, 0));
    
    mainPanel.add(apiProxyUrlLabel, GBC.std());
    mainPanel.add(apiProxyUrl, GBC.eol().fill(GridBagConstraints.HORIZONTAL).insets(0, 0, 0, 0));
    
    mainPanel.add(emailLabel, GBC.std().insets(0, 0, 0, 0));
    mainPanel.add(email, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
    
    mainPanel.add(orgLabel, GBC.std().insets(0, 0, 0, 0));
    mainPanel.add(org, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
    
    mainPanel.add(selectFromOtherLayer, GBC.eol().fill(GridBagConstraints.HORIZONTAL).insets(0, 0, 0, 0));
    
    if (ExpertToggleAction.isExpert() || 
        developer.isSelected() || 
        !REPORT_API.fromLabel(reportApiComboBox.getSelectedItem().toString()).isProduction()) {
      mainPanel.add(new JLabel(I18n.tr("Advanced settings")), GBC.eol().fill(GridBagConstraints.HORIZONTAL).insets(0, 20, 0, 0));
      
      mainPanel.add(developer, GBC.eol().fill(GridBagConstraints.HORIZONTAL).insets(INDENT, 0, 0, 0));
      
      mainPanel.add(fiddler, GBC.eol().fill(GridBagConstraints.HORIZONTAL).insets(INDENT, 0, 0, 0));
      
//      mainPanel.add(useActApi, GBC.eol().fill(GridBagConstraints.HORIZONTAL).insets(INDENT, 0, 0, 0));
      
      mainPanel.add(apiUrlActLabel, GBC.std().insets(2*INDENT, 0, 0, 0));
      mainPanel.add(apiUrlAct, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
      
      mainPanel.add(apiKeyActLabel, GBC.std().insets(2*INDENT, 0, 0, 0));
      mainPanel.add(apiKeyAct, GBC.std().fill(GridBagConstraints.HORIZONTAL));
      mainPanel.add(apiKeyActRequest, GBC.std().insets(5, 0, 0, 0));
      mainPanel.add(validateActButton, GBC.eol().insets(5, 0, 0, 0));

      mainPanel.add(apiProxyUrlActLabel, GBC.std().insets(2*INDENT, 0, 0, 0));
      mainPanel.add(apiProxyUrlAct, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
    }
//    ReportColorScheme.styleAsDefaultPanel(mainPanel, downloadModePanel, developer); // makes background white
    mainPanel.add(Box.createVerticalGlue(), GBC.eol().fill(GridBagConstraints.VERTICAL));

    container.add(mainPanel, BorderLayout.CENTER);

    synchronized (gui.getDisplayPreference().getTabPane()) {
      gui.getDisplayPreference().addSubTab(this, I18n.tr("PDOK Reports"), new JScrollPane(container));
      gui.getDisplayPreference().getTabPane()
        .setIconAt(gui.getDisplayPreference().getTabPane().getTabCount() - 1, ReportPlugin.LOGO.setSize(12, 12).get());
    }

    new Thread(() -> {
      String username = ReportUser.getApiKey();
      if (username != null) {
        SwingUtilities.invokeLater(() -> onLogin(ReportUser.getApiKey()));
      }
    }).start();
  }
  
  public void enableControls()
  {
    REPORT_API reportApi = REPORT_API.fromLabel(reportApiComboBox.getSelectedItem().toString());
    apiUrl.setEnabled(reportApi == REPORT_API.PDOK_PRODUCTION);
    apiKey.setEnabled(reportApi == REPORT_API.PDOK_PRODUCTION);
    validateButton.setEnabled(reportApi == REPORT_API.PDOK_PRODUCTION);

    apiUrlAct.setEnabled(reportApi == REPORT_API.PDOK_ACCEPTANCE);
    apiKeyAct.setEnabled(reportApi == REPORT_API.PDOK_ACCEPTANCE);
    validateActButton.setEnabled(reportApi == REPORT_API.PDOK_ACCEPTANCE);
   
    apiProxyUrl.setEnabled(reportApi == REPORT_API.PROXY_PRODUCTION);
    
    apiProxyUrlAct.setEnabled(reportApi == REPORT_API.PROXY_ACCEPTANCE);
  }

  @Override
  public void onLogin(final String apikey) {
    validateButton.setEnabled(false);
//    loginLabel.setText(I18n.tr("You are logged in using API KEY ''{0}''.", apikey));
    new Notification(I18n.tr("You are logged in using API KEY ''{0}''.", apikey)).setDuration(Notification.TIME_DEFAULT).show();
    headerPanel.revalidate();
    headerPanel.repaint();
  }

  @Override
  public void onLoginAct(final String apikey) {
    validateActButton.setEnabled(false);
//    headerPanel.add(logoutButton, 5);
//    loginLabel.setText(I18n.tr("You are logged in using API KEY ''{0}''.", apikey));
    new Notification(I18n.tr("You are logged in using API KEY ''{0}''.", apikey)).setDuration(Notification.TIME_DEFAULT).show();
    headerPanel.revalidate();
    headerPanel.repaint();
  }

  @Override
  public void onLogout() {
    validateButton.setEnabled(!apiKey.getText().isEmpty());
    apiKeyRequest.setVisible(apiKey.getText().isEmpty());
//    loginLabel.setText(I18n.tr("Enter an API key"));
    headerPanel.revalidate();
    headerPanel.repaint();
  }

  @Override
  public void onLogoutAct() {
    validateActButton.setEnabled(!apiKeyAct.getText().isEmpty());
    apiKeyActRequest.setVisible(apiKeyAct.getText().isEmpty());
//    loginLabel.setText(I18n.tr("Enter an API key"));
    headerPanel.revalidate();
    headerPanel.repaint();
  }

  @Override
  public boolean ok() {
//    ReportProperties.DOWNLOAD_MODE.put(DOWNLOAD_MODE.fromLabel(downloadModeComboBox.getSelectedItem().toString()).getPrefId());
    ReportProperties.API_REPORT_USE.put(REPORT_API.fromLabel(reportApiComboBox.getSelectedItem().toString()).getPrefId());

    ReportProperties.API_KEY.put(apiKey.getText());
    ReportProperties.API_URL.put(apiUrl.getText());
    ReportProperties.API_PROXY_URL.put(apiProxyUrl.getText());
    ReportProperties.USER_EMAIL.put(email.getText());
    ReportProperties.USER_ORGANISATION.put(org.getText());
    ReportProperties.DEVELOPER.put(developer.isSelected());
    ReportProperties.USE_FIDDLER.put(fiddler.isSelected());
    ReportProperties.API_KEY_ACT.put(apiKeyAct.getText());
    ReportProperties.API_URL_ACT.put(apiUrlAct.getText());
    ReportProperties.API_PROXY_URL_ACT.put(apiProxyUrlAct.getText());
    ReportProperties.SELECT_FROM_OTHER_LAYER.put(selectFromOtherLayer.isSelected());

    // Restart is never required
    return false;
  }

  @Override
  public boolean isExpert() {
    return false;
  }

  /**
   * Request empty dataset from PDOK to test API key.
   *
   * @author SanderH
   *
   */
  private final class ValidateAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    ValidateAction() {
      super(I18n.tr("Validate API key"));
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
      
      if (apiKey.getText().isEmpty())
      {
        new Notification(I18n.tr("Please enter an API key first in the field below")).setDuration(Notification.TIME_DEFAULT).show();
        return;
      }
      try
      {
        HttpURLConnection connection = (HttpURLConnection)ReportURL.validateApiURL(REPORT_API.PDOK_PRODUCTION).openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "JOSM");
        connection.setRequestProperty("apikey", apiKey.getText());
        connection.connect();

        switch (connection.getResponseCode())
        {
        case 204:
          onLogin(apiKey.getText());
          break;
        case 400:
        case 401: 
          new Notification(I18n.tr("An error occurred during validation: ''{0}''", connection.getResponseMessage())).setDuration(Notification.TIME_DEFAULT).show();
          break;
        }
      } 
      catch (IOException e) {
        Logging.error(e); 
      }
    }
  }

  /**
   * Request empty dataset from PDOK to test Acceptance API key.
   *
   * @author SanderH
   *
   */
  private final class ValidateActAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    ValidateActAction() {
      super(I18n.tr("Validate acceptance API key"));
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
      
      if (apiKeyAct.getText().isEmpty())
      {
        new Notification(I18n.tr("Please enter an acceptance API key first in the field below")).setDuration(Notification.TIME_DEFAULT).show();
        return;
      }
      try
      {
        HttpURLConnection connection = (HttpURLConnection)ReportURL.validateApiURL(REPORT_API.PDOK_ACCEPTANCE).openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "JOSM");
        connection.setRequestProperty("apikey", apiKeyAct.getText());
        connection.connect();

        switch (connection.getResponseCode())
        {
        case 204:
          onLoginAct(apiKeyAct.getText());
          break;
        case 400:
        case 401: 
          new Notification(I18n.tr("An error occurred during validation: ''{0}''", connection.getResponseMessage())).setDuration(Notification.TIME_DEFAULT).show();
          break;
        }
      } 
      catch (IOException e) {
        Logging.error(e); 
      }
    }
  }
}

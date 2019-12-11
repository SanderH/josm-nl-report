// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListenerAdapter;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.NavigatableComponent;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.AbstractModifiableLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.plugins.nl_pdok_report.gui.ReportFilterDialog;
import org.openstreetmap.josm.plugins.nl_pdok_report.gui.reportinfo.ReportInfoPanel;
import org.openstreetmap.josm.plugins.nl_pdok_report.history.ReportRecord;
import org.openstreetmap.josm.plugins.nl_pdok_report.history.commands.CommandDelete;
import org.openstreetmap.josm.plugins.nl_pdok_report.io.download.ReportDownloader;
import org.openstreetmap.josm.plugins.nl_pdok_report.io.download.ReportDownloader.DOWNLOAD_MODE;
import org.openstreetmap.josm.plugins.nl_pdok_report.mode.AbstractMode;
import org.openstreetmap.josm.plugins.nl_pdok_report.mode.JoinMode;
import org.openstreetmap.josm.plugins.nl_pdok_report.mode.SelectMode;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportProperties;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportUtils;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.plugins.nl_pdok_report.utils.MapViewGeometryUtil;
import org.openstreetmap.josm.tools.ColorHelper;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.ImageProvider.ImageSizes;
import org.openstreetmap.josm.tools.Logging;

/**
 * This class represents the layer shown in JOSM. There can only exist one instance of this object.
 *
 * @author nokutu
 */
public final class ReportLayer extends AbstractModifiableLayer
  implements ActiveLayerChangeListener, ReportDataListener {

  private final String ICON_MARKERS = "markers";
  private final String REPORT_NEW = "report-new";
  private final String REPORT_APPROVED = "report-approved";
  private final String REPORT_COMPLETED = "report-completed";
  private final String REPORT_FORWARDED = "report-forwarded";
  private final String REPORT_INVESTIGATION = "report-investigation";
  private final String REPORT_PARKED = "report-parked";
  private final String REPORT_REJECTED = "report-rejected";
  private final String REPORT_ADD = "report-add";
  private final String REPORT_UNKNOWN = "report-unknown";
  private static final int IMG_MARKER_SIZE = 16;
  private static final int IMG_MARKER_SIZE_SELECTED = 32;
  private final Map<String, ImageIcon> scaledIcons = new HashMap<>();
  private final Map<String, ImageIcon> scaledIconsSelected = new HashMap<>();

  private static final DataSetListenerAdapter DATASET_LISTENER = new DataSetListenerAdapter(e -> {
    if (e instanceof DataChangedEvent) {
      // When more data is downloaded, a delayed update is thrown, in order to
      // wait for the data bounds to be set.
      MainApplication.worker.execute(ReportDownloader::downloadOSMArea);
    }
  });

  /** Unique instance of the class. */
  private static ReportLayer instance;
  /** {@link ReportData} object that stores the database. */
  private final ReportData data;
  
  private AbstractReport displayedReport;
  private HtmlPanel displayedPanel;
  private JWindow displayedWindow;

  /** Mode of the layer. */
  public AbstractMode mode;

  private volatile TexturePaint hatched;

  private ReportLayer() {
    super(I18n.tr("PDOK Reports"));
    this.data = new ReportData();
    data.addListener(this);
  }

  /**
   * Initializes the Layer.
   */
  private void init() {
    final DataSet ds = MainApplication.getLayerManager().getEditDataSet();
    if (ds != null) {
      ds.addDataSetListener(DATASET_LISTENER);
    }
    MainApplication.getLayerManager().addActiveLayerChangeListener(this);
    if (!GraphicsEnvironment.isHeadless()) {
      setMode(new SelectMode());
      if (ReportDownloader.getMode() == DOWNLOAD_MODE.OSM_AREA) {
        MainApplication.worker.execute(ReportDownloader::downloadOSMArea);
      }
      if (ReportDownloader.getMode() == DOWNLOAD_MODE.VISIBLE_AREA) {
        this.mode.zoomChanged();
      }
    }
    // Does not execute when in headless mode
    /*
     * sho if (MainApplication.getMainFrame() != null && !ReportMainDialog.getInstance().isShowing()) {
     * ReportMainDialog.getInstance().showDialog(); }
     */
    if (ReportPlugin.getMapView() != null) {
      /*
       * sho ReportMainDialog.getInstance().mapillaryImageDisplay.repaint(); ReportMainDialog.getInstance()
       * .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW) .put(KeyStroke.getKeyStroke("DELETE"), "FeedbackDel");
       * ReportMainDialog.getInstance().getActionMap() .put("FeedbackDel", new DeleteReportAction());
       */
      // getLocationChangeset().addChangesetListener(ReportNewDialog.getInstance());
    }
    
    createHatchTexture();
    invalidate();
  }

  public static void invalidateInstance() {
    if (hasInstance()) {
      getInstance().invalidate();
    }
  }

  /**
   * Changes the mode the the given one.
   *
   * @param mode
   *          The mode that is going to be activated.
   */
  public void setMode(AbstractMode mode) {
    final MapView mv = ReportPlugin.getMapView();
    if (this.mode != null && mv != null) {
      mv.removeMouseListener(this.mode);
      mv.removeMouseMotionListener(this.mode);
      NavigatableComponent.removeZoomChangeListener(this.mode);
    }
    this.mode = mode;
    if (mode != null && mv != null) {
      mv.setNewCursor(mode.cursor, this);
      mv.addMouseListener(mode);
      mv.addMouseMotionListener(mode);
      NavigatableComponent.addZoomChangeListener(mode);
      ReportUtils.updateHelpText();
    }
  }

  private static synchronized void clearInstance() {
    instance = null;
  }

  /**
   * Returns the unique instance of this class.
   *
   * @return The unique instance of this class.
   */
  public static synchronized ReportLayer getInstance() {
    if (instance != null) {
      return instance;
    }
    final ReportLayer layer = new ReportLayer();
    layer.init();
    instance = layer; // Only set instance field after initialization is complete
    return instance;
  }

  /**
   * @return if the unique instance of this layer is currently instantiated
   */
  public static boolean hasInstance() {
    return instance != null;
  }

  /**
   * Returns the {@link ReportData} object, which acts as the database of the Layer.
   *
   * @return The {@link ReportData} object that stores the database.
   */
  public ReportData getData() {
    return this.data;
  }

  @Override
  public synchronized void destroy() {
    clearInstance();
    setMode(null);
    ReportRecord.getInstance().reset();
    AbstractMode.resetThread();
    ReportDownloader.stopAll();
    ReportInfoPanel.getInstance().selectedReportChanged(null, null);
    /*
     * if (ReportMainDialog.hasInstance()) { ReportMainDialog.getInstance().setReport(null);
     * ReportMainDialog.getInstance().updateReport(); }
     */
    final MapView mv = ReportPlugin.getMapView();
    if (mv != null) {
      mv.removeMouseListener(this.mode);
      mv.removeMouseMotionListener(this.mode);
    }
    try {
      MainApplication.getLayerManager().removeActiveLayerChangeListener(this);
      if (MainApplication.getLayerManager().getEditDataSet() != null) {
        MainApplication.getLayerManager().getEditDataSet().removeDataSetListener(DATASET_LISTENER);
      }
    } catch (IllegalArgumentException e) {
      // TODO: It would be ideal, to fix this properly. But for the moment let's catch this, for when a listener has
      // already been removed.
    }
    hideReportWindow();
    super.destroy();
  }

  @Override
  public boolean isModified() {
    return this.data.getReports().parallelStream().anyMatch(AbstractReport::isModified);
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    getData().getReports().parallelStream().forEach(img -> img.setVisible(visible));
    if (MainApplication.getMap() != null) {
      ReportFilterDialog.getInstance().refresh();
    }
  }

  /**
   * Initialize the hatch pattern used to paint the non-downloaded area.
   */
  private void createHatchTexture() {
    BufferedImage bi = new BufferedImage(15, 15, BufferedImage.TYPE_INT_ARGB);
    Graphics2D big = bi.createGraphics();
    big.setColor(ReportProperties.BACKGROUND.get());
    Composite comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
    big.setComposite(comp);
    // Until the report API cannot fetch results within bounds, don't draw the hatched line and background, 
    //   but we do want the transparent background, so keep the other parts of this function.
    //big.fillRect(0, 0, 15, 15);
    //big.setColor(ReportProperties.OUTSIDE_DOWNLOADED_AREA.get());
    //big.drawLine(0, 15, 15, 0);
    Rectangle r = new Rectangle(0, 0, 15, 15);
    this.hatched = new TexturePaint(bi, r);
  }

  @Override
  public synchronized void paint(final Graphics2D g, final MapView mv, final Bounds box) {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    if (MainApplication.getLayerManager().getActiveLayer() == this) {
      // paint remainder
      g.setPaint(this.hatched);
      g.fill(MapViewGeometryUtil.getNonDownloadedArea(mv, this.data.getBounds()));
    }

    for (AbstractReport reportAbs : this.data.getReports()) {
      if (reportAbs.isVisible() && mv != null && mv.contains(mv.getPoint(reportAbs.getMovingLatLon()))) {
        drawReportMarker(g, reportAbs);
      }
    }
    if (this.mode instanceof JoinMode) {
      this.mode.paint(g, mv, box);
    }
    
    AbstractReport selectedReport = data.getSelectedReport();
    if (selectedReport != null) {
        paintSelectedReport(g, mv, IMG_MARKER_SIZE_SELECTED, IMG_MARKER_SIZE_SELECTED, selectedReport);
    } else {
        hideReportWindow();
    }
  }

  /**
   * Draws an image marker onto the given Graphics context.
   * 
   * @param g
   *          the Graphics context
   * @param img
   *          the image to be drawn onto the Graphics context
   */
  private void drawReportMarker(final Graphics2D g, final AbstractReport report) {
    if (report == null || report.getLatLon() == null) {
      Logging.warn("An report is not painted, because it is null or has no LatLon!");
      return;
    }
    //final AbstractReport selectedReport = getData().getSelectedReport();
    final Point p = MainApplication.getMap().mapView.getPoint(report.getMovingLatLon());

    final ImageIcon icon = getReportMarker(report);
    g.drawImage(icon.getImage(), p.x - icon.getIconWidth() / 2, p.y - icon.getIconHeight() / 2, null);
  }

  private ImageIcon getReportMarker(final AbstractReport report) {
    String icon = REPORT_UNKNOWN;
    if (scaledIcons.isEmpty()) {
      scaledIcons.put(REPORT_NEW, new ImageProvider(ICON_MARKERS, REPORT_NEW).setMaxSize(IMG_MARKER_SIZE).get());
      scaledIcons.put(REPORT_APPROVED, new ImageProvider(ICON_MARKERS, REPORT_APPROVED).setMaxSize(IMG_MARKER_SIZE).get());
      scaledIcons.put(REPORT_COMPLETED, new ImageProvider(ICON_MARKERS, REPORT_COMPLETED).setMaxSize(IMG_MARKER_SIZE).get());
      scaledIcons.put(REPORT_FORWARDED, new ImageProvider(ICON_MARKERS, REPORT_FORWARDED).setMaxSize(IMG_MARKER_SIZE).get());
      scaledIcons.put(REPORT_INVESTIGATION, new ImageProvider(ICON_MARKERS, REPORT_INVESTIGATION).setMaxSize(IMG_MARKER_SIZE).get());
      scaledIcons.put(REPORT_PARKED, new ImageProvider(ICON_MARKERS, REPORT_PARKED).setMaxSize(IMG_MARKER_SIZE).get());
      scaledIcons.put(REPORT_REJECTED, new ImageProvider(ICON_MARKERS, REPORT_REJECTED).setMaxSize(IMG_MARKER_SIZE).get());
      scaledIcons.put(REPORT_ADD, new ImageProvider(ICON_MARKERS, REPORT_ADD).setMaxSize(IMG_MARKER_SIZE).get());
      scaledIcons.put(REPORT_UNKNOWN, new ImageProvider(ICON_MARKERS, REPORT_UNKNOWN).setMaxSize(IMG_MARKER_SIZE).get());

      scaledIconsSelected.put(REPORT_NEW, new ImageProvider(ICON_MARKERS, REPORT_NEW).setMaxSize(IMG_MARKER_SIZE_SELECTED).get());
      scaledIconsSelected.put(REPORT_APPROVED, new ImageProvider(ICON_MARKERS, REPORT_APPROVED).setMaxSize(IMG_MARKER_SIZE_SELECTED).get());
      scaledIconsSelected.put(REPORT_COMPLETED, new ImageProvider(ICON_MARKERS, REPORT_COMPLETED).setMaxSize(IMG_MARKER_SIZE_SELECTED).get());
      scaledIconsSelected.put(REPORT_FORWARDED, new ImageProvider(ICON_MARKERS, REPORT_FORWARDED).setMaxSize(IMG_MARKER_SIZE_SELECTED).get());
      scaledIconsSelected.put(REPORT_INVESTIGATION, new ImageProvider(ICON_MARKERS, REPORT_INVESTIGATION).setMaxSize(IMG_MARKER_SIZE_SELECTED).get());
      scaledIconsSelected.put(REPORT_PARKED, new ImageProvider(ICON_MARKERS, REPORT_PARKED).setMaxSize(IMG_MARKER_SIZE_SELECTED).get());
      scaledIconsSelected.put(REPORT_REJECTED, new ImageProvider(ICON_MARKERS, REPORT_REJECTED).setMaxSize(IMG_MARKER_SIZE_SELECTED).get());
      scaledIconsSelected.put(REPORT_ADD, new ImageProvider(ICON_MARKERS, REPORT_ADD).setMaxSize(IMG_MARKER_SIZE_SELECTED).get());
      scaledIconsSelected.put(REPORT_UNKNOWN, new ImageProvider(ICON_MARKERS, REPORT_UNKNOWN).setMaxSize(IMG_MARKER_SIZE_SELECTED).get());
    }
    if (report instanceof ReportNewBAG) {
      icon = REPORT_ADD;
    } else if (report instanceof ReportBAG) {
      switch (((ReportBAG) report).getStatusCode()) {
      case "NIEUW":
        icon = REPORT_NEW;
        break;
      case "GOEDGEKEURD":
        icon = REPORT_APPROVED;
        break;
      case "AFGEROND":
        icon = REPORT_COMPLETED;
        break;
      case "DOORGESTUURD":
        icon = REPORT_FORWARDED;
        break;
      case "IN_ONDERZOEK":
        icon = REPORT_INVESTIGATION;
        break;
      case "GEPARKEERD":
        icon = REPORT_PARKED;
        break;
      case "AFGEWEZEN":
        icon = REPORT_REJECTED;
        break;
      }
    }
    return report.equals(data.getSelectedReport()) ?  scaledIconsSelected.get(icon) : scaledIcons.get(icon);
  }

  @Override
  public Icon getIcon() {
    return ReportPlugin.LOGO.setSize(ImageSizes.LAYER).get();
  }

  @Override
  public boolean isMergable(Layer other) {
    return false;
  }

  @Override
  public void mergeFrom(Layer from) {
    throw new UnsupportedOperationException("This layer does not support merging yet");
  }

  @Override
  public Action[] getMenuEntries() {
    return new Action[] { LayerListDialog.getInstance().createShowHideLayerAction(),
        LayerListDialog.getInstance().createDeleteLayerAction(), new LayerListPopup.InfoAction(this) };
  }

  @Override
  public Object getInfoComponent() {
    final long numNew = getData().getReports().stream().filter(i -> i instanceof ReportNewBAG).count();
    final long numDownloaded = getData().getReports().stream().filter(i -> i instanceof ReportBAG).count();
    final int numTotal = getData().getReports().size();
    return new StringBuilder(I18n.tr("Reports layer")).append("\n\n").append(
      I18n.trn("{0} new report", "{0} new reports", numNew, numNew)
    ).append("\n+ ").append(I18n.trn("{0} downloaded report", "{0} downloaded reports", numDownloaded, numDownloaded))
      .append("\n= ").append(I18n.trn("{0} report in total", "{0} reports in total", numTotal, numTotal)).toString();
  }

  @Override
  public String getToolTipText() {
    return I18n.tr("{0} reports", getData().getReports().size());
  }

  @Override
  public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
    if (MainApplication.getLayerManager().getActiveLayer() == this) {
      ReportUtils.updateHelpText();
    }

    if (MainApplication.getLayerManager().getEditLayer() != e.getPreviousDataLayer()) {
      if (MainApplication.getLayerManager().getEditLayer() != null) {
        MainApplication.getLayerManager().getEditLayer().getDataSet().addDataSetListener(DATASET_LISTENER);
      }
      if (e.getPreviousDataLayer() != null) {
        e.getPreviousDataLayer().getDataSet().removeDataSetListener(DATASET_LISTENER);
      }
    }
  }

  @Override
  public void visitBoundingBox(BoundingXYVisitor v) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openstreetmap.josm.plugins.nl_pdok_feedback.ReportDataListener#reportssAdded()
   */
  @Override
  public void reportsAdded() {
    // sho updateNearestReports();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openstreetmap.josm.plugins.nl_pdok_feedback.ReportDataListener#reportssAdded()
   */
  @Override
  public void reportsRemoved() {
    // Method is not needed, but enforced by the interface ReportDataListener
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.openstreetmap.josm.plugins.nl_pdok_feedback.ReportDataListener#selectedImageChanged(org.openstreetmap.josm.
   * plugins.nl_pdok_feedback.AbstractReport, org.openstreetmap.josm.plugins.nl_pdok_feedback.AbstractReport)
   */
  @Override
  public void selectedReportChanged(AbstractReport oldReport, AbstractReport newReport) {
    // sho updateNearestReports();
  }

  @Override
  public boolean isUploadable() {
    return true;
  }

  @Override
  public boolean requiresUploadToServer() {
    return !this.getData().getNewReports().isEmpty();
  }

  /**
   * Action used to delete images.
   *
   * @author nokutu
   */
  private class DeleteReportAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
      if (instance != null)
        ReportRecord.getInstance().addCommand(new CommandDelete(getData().getMultiSelectedReports()));
    }
  }
  
  private void hideReportWindow() {
    if (displayedWindow != null) {
        displayedWindow.setVisible(false);
        for (MouseWheelListener listener : displayedWindow.getMouseWheelListeners()) {
            displayedWindow.removeMouseWheelListener(listener);
        }
        displayedWindow.dispose();
        displayedWindow = null;
        displayedPanel = null;
        displayedReport = null;
    }
  }

  private void paintSelectedReport(Graphics2D g, MapView mv, final int iconHeight, final int iconWidth, AbstractReport selectedReport) {
    Point p = mv.getPoint(selectedReport.getLatLon());

    g.setColor(ColorHelper.html2color(Config.getPref().get("color.selected")));
    g.drawRect(p.x - (iconWidth / 2), p.y - (iconHeight / 2), iconWidth - 1, iconHeight - 1);

    if (selectedReport != null && !selectedReport.equals(selectedReport)) {
        hideReportWindow();
    }

    int xl = p.x - (iconWidth / 2) - 5;
    int xr = p.x + (iconWidth / 2) + 5;
    int yb = p.y - iconHeight - 1;
    int yt = p.y + (iconHeight / 2) + 2;
    Point pTooltip;

    String text = getReportToolTip(selectedReport);

    if (displayedWindow == null) {
        displayedPanel = new HtmlPanel(text);
        displayedPanel.setBackground(UIManager.getColor("ToolTip.background"));
        displayedPanel.setForeground(UIManager.getColor("ToolTip.foreground"));
        displayedPanel.setFont(UIManager.getFont("ToolTip.font"));
        displayedPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        displayedPanel.enableClickableHyperlinks();
        pTooltip = fixPanelSizeAndLocation(mv, text, xl, xr, yt, yb);
        displayedWindow = new JWindow(MainApplication.getMainFrame());
        displayedWindow.setAutoRequestFocus(false);
        displayedWindow.add(displayedPanel);
        // Forward mouse wheel scroll event to MapMover
        displayedWindow.addMouseWheelListener(e -> mv.getMapMover().mouseWheelMoved(
                (MouseWheelEvent) SwingUtilities.convertMouseEvent(displayedWindow, e, mv)));
    } else {
        displayedPanel.setText(text);
        pTooltip = fixPanelSizeAndLocation(mv, text, xl, xr, yt, yb);
    }

    displayedWindow.pack();
    displayedWindow.setLocation(pTooltip);
    displayedWindow.setVisible(mv.contains(p));
    displayedReport = selectedReport;
  }

  private Point fixPanelSizeAndLocation(MapView mv, String text, int xl, int xr, int yt, int yb) {
    int leftMaxWidth = (int) (0.95 * xl);
    int rightMaxWidth = (int) (0.95 * mv.getWidth() - xr);
    int topMaxHeight = (int) (0.95 * yt);
    int bottomMaxHeight = (int) (0.95 * mv.getHeight() - yb);
    int maxWidth = Math.max(leftMaxWidth, rightMaxWidth);
    int maxHeight = Math.max(topMaxHeight, bottomMaxHeight);
    JEditorPane pane = displayedPanel.getEditorPane();
    Dimension d = pane.getPreferredSize();

    // If still too large, enforce maximum size
    d = pane.getPreferredSize();
    if (d.width > maxWidth || d.height > maxHeight) {
        View v = (View) pane.getClientProperty(BasicHTML.propertyKey);
        if (v == null) {
            BasicHTML.updateRenderer(pane, text);
            v = (View) pane.getClientProperty(BasicHTML.propertyKey);
        }
        if (v != null) {
            v.setSize(maxWidth, 0);
            int w = (int) Math.ceil(v.getPreferredSpan(View.X_AXIS));
            int h = (int) Math.ceil(v.getPreferredSpan(View.Y_AXIS)) + 10;
            pane.setPreferredSize(new Dimension(w, h));
        }
    }
    d = pane.getPreferredSize();
    // place tooltip on left or right side of icon, based on its width
    Point screenloc = mv.getLocationOnScreen();
    return new Point(
            screenloc.x + (d.width > rightMaxWidth && d.width <= leftMaxWidth ? xl - d.width : xr),
            screenloc.y + (d.height > bottomMaxHeight && d.height <= topMaxHeight ? yt - d.height - 10 : yb));
  }

  /**
   * Returns the HTML-formatted tooltip text for the given report.
   * @param report report to display
   * @return the HTML-formatted tooltip text for the given report
   */
  public static String getReportToolTip(AbstractReport report) {
      StringBuilder sb = new StringBuilder("<html>");
      sb.append(tr("Report"))
        .append(" ").append(report instanceof ReportBAG ? ((ReportBAG)report).getReportNumberFull() : "")
        .append("<hr />").append(report.getDescription());
      sb.append("</html>");
      String result = sb.toString();
//      Logging.debug(result);
      return result;
  }

}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.gui.PopupMenuHandler;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.widgets.PopupMenuLauncher;
import org.openstreetmap.josm.plugins.nl_pdok_report.AbstractReport;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportDataListener;
import org.openstreetmap.josm.plugins.nl_pdok_report.ReportLayer;
import org.openstreetmap.josm.plugins.nl_pdok_report.actions.ReportNewAddAction;
import org.openstreetmap.josm.plugins.nl_pdok_report.actions.ReportNewEditAction;
import org.openstreetmap.josm.plugins.nl_pdok_report.actions.ReportRemoveNewAction;
import org.openstreetmap.josm.plugins.nl_pdok_report.actions.ReportUploadNewAction;
import org.openstreetmap.josm.plugins.nl_pdok_report.actions.ReportZoomAction;
import org.openstreetmap.josm.plugins.nl_pdok_report.history.ReportRecord;
import org.openstreetmap.josm.plugins.nl_pdok_report.history.commands.ReportCommand;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Toggle dialog that shows you the latest {@link ReportCommand} done and allows the user to revert them.
 *
 * @see ReportRecord
 * @see ReportCommand
 */
public final class ReportNewDialog extends ToggleDialog implements ReportDataListener {
  private static final long serialVersionUID = -7762181685042342527L;

  private static ReportNewDialog instance;

  private final DefaultTreeModel changesetTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
  protected DefaultMutableTreeNode selectedNode;

  private final Component spacer = Box.createRigidArea(new Dimension(0, 3));

  private final Container rootComponent = new JPanel(new BorderLayout());
  private final SideButton addButton = new SideButton(new NewReportAction());
  private final SideButton uploadButton = new SideButton(new ReportUploadNewAction(this));
  protected final SideButton removeButton = new SideButton(new ReportRemoveNewAction(this));
  private final JProgressBar uploadPendingProgress = new JProgressBar();

  protected final JPopupMenu popupMenu = new JPopupMenu();
  private final transient PopupMenuHandler popupMenuHandler = new PopupMenuHandler(popupMenu);
  protected final ReportZoomAction zoomAction = new ReportZoomAction();
  protected final ReportNewEditAction newEditAction = new ReportNewEditAction();

  /**
   * Destroys the unique instance of the class.
   */
  public static void destroyInstance() {
    ReportNewDialog.instance = null;
  }

  private ReportNewDialog() {
    super(
      tr("New reports to upload"), "report-upload", tr("Open new reports dialog"), Shortcut.registerShortcut(tr("New reports upload"), tr("Open new reports dialog"), KeyEvent.VK_9, Shortcut.NONE), 150
    );
    // NewReportAction action = new NewReportAction();
    // addButton = new SideButton(action);

    popupMenuHandler.addAction(zoomAction);
    popupMenuHandler.addAction(newEditAction);

    createLayout(rootComponent, false, Arrays.asList(addButton, uploadButton, removeButton));

    final JTree changesetTree = new JTree(this.changesetTreeModel);
    changesetTree.addMouseListener(new MouseEventHandler());
    changesetTree.expandRow(0);
    changesetTree.setShowsRootHandles(true);
    changesetTree.setRootVisible(false);
    changesetTree.setCellRenderer(new ReportNewTreeCellRenderer());
    changesetTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    changesetTree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        selectedNode = (DefaultMutableTreeNode)changesetTree.getLastSelectedPathComponent(); 
        removeButton.setEnabled(selectedNode != null);
        if (selectedNode != null)
        {
          ReportLayer.getInstance().getData().setSelectedReport((AbstractReport)selectedNode.getUserObject());
        }
        else
        {
          ReportLayer.getInstance().getData().setSelectedReport(null);
        }
      }
    });

    final JPanel treesPanel = new JPanel(new GridBagLayout());
    treesPanel.add(this.spacer, GBC.eol());
    treesPanel.add(changesetTree, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
    treesPanel.add(new JSeparator(), GBC.eol().fill(GridBagConstraints.HORIZONTAL));
    treesPanel.add(Box.createRigidArea(new Dimension(0, 0)), GBC.std().weight(0, 1));
    rootComponent.add(new JScrollPane(treesPanel), BorderLayout.CENTER);

    uploadPendingProgress.setIndeterminate(true);
    uploadPendingProgress.setString(tr("Uploading report to server…"));
    uploadPendingProgress.setStringPainted(true);

    setUploadPending(false);
  }

  /**
   * Returns the unique instance of the class.
   *
   * @return The unique instance of the class.
   */
  public static synchronized ReportNewDialog getInstance() {
    if (instance == null) {
      instance = new ReportNewDialog();
    }
    return instance;
  }

  private void buildTree() {
    ReportLayer.getInstance().getData().addListener(newEditAction);
    ReportLayer.getInstance().getData().addListener(zoomAction);

    final List<AbstractReport> reports = ReportLayer.getInstance().getData().getNewReports();
    uploadButton.setEnabled(!reports.isEmpty());
    DefaultMutableTreeNode reportsRoot = new DefaultMutableTreeNode();

    reports.parallelStream().filter(Objects::nonNull).forEach(report -> {
      final DefaultMutableTreeNode node = new DefaultMutableTreeNode(report);
      reportsRoot.add(node);
    });

    this.spacer.setVisible(reports.isEmpty());

    this.changesetTreeModel.setRoot(reportsRoot);
  }

  public void setUploadPending(final boolean isUploadPending) {
    if (isUploadPending) {
      rootComponent.add(uploadPendingProgress, BorderLayout.SOUTH);
    } else {
      rootComponent.remove(uploadPendingProgress);
    }
    uploadButton.setEnabled(
      ReportLayer.hasInstance() && !ReportLayer.getInstance().getData().getNewReports().isEmpty()
    );
    rootComponent.revalidate();
    rootComponent.repaint();
  }
  
  public AbstractReport getSelectedReport()
  {
    return ((AbstractReport)selectedNode.getUserObject());
  }
  
  @Override
  public void reportsAdded() {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(this::buildTree);
    } else {
      buildTree();
    }
  }

  @Override
  public void reportsRemoved() {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(this::buildTree);
    } else {
      buildTree();
    }
  }

  public PopupMenuHandler getPopupMenuHandler() {
    return popupMenuHandler;
  }

  public static void editReport()
  {
    ReportPropertiesDialog dialog = new ReportPropertiesDialog(
      MainApplication.getMainFrame(), tr("Update a new report"), tr("Update report"),
      ReportLayer.getInstance().getData().getSelectedReport().getDescription()
    );
    dialog.showReportPropertiesDialog(
      tr("Enter a detailed description to create a report"), new ImageProvider("dialogs", "report-new").setSize(24, 24).get()
    );
    if (dialog.getValue() != 1) {
      Logging.debug("User aborted report creation");
      return;
    }
    String input = dialog.getInputText();
    if (input != null && !input.isEmpty()) {
      ReportLayer.getInstance().getData().getSelectedReport().setDescription(input);
    } else {
      new Notification(tr("You must enter a description to create a new report")).setIcon(JOptionPane.WARNING_MESSAGE)
        .show();
    }
    ReportNewDialog.getInstance().buildTree();
  }
  
  /**
   * Watches for double clicks and launches the popup menu.
   */
  class MouseEventHandler extends PopupMenuLauncher {

      MouseEventHandler() {
          super(popupMenu);
      }
      
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            if (selectedNode == null) return;
            
            ReportNewDialog.editReport();
        }
    }
  }
  
  class NewReportAction extends AbstractAction {
    private static final long serialVersionUID = -4839536718553295231L;

    /**
     * Constructs a new {@code NewAction}.
     */
    NewReportAction() {
      putValue(SHORT_DESCRIPTION, tr("Create a new report"));
      putValue(NAME, tr("Create report"));
      new ImageProvider("markers", "report-add").getResource().attachImageIcon(this, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (!ReportLayer.hasInstance() || !MainApplication.getLayerManager().containsLayer(ReportLayer.getInstance())) {
        MainApplication.getLayerManager().addLayer(ReportLayer.getInstance());
        return;
      }
      MainApplication.getMap().selectMapMode(new ReportNewAddAction(ReportLayer.getInstance().getData()));
    }
  }

  @Override
  public void selectedReportChanged(AbstractReport oldReport, AbstractReport newReport) {
    // not needed
  }
}

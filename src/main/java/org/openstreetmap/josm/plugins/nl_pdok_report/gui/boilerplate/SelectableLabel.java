// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.gui.boilerplate;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JTextPane;
import javax.swing.UIManager;

public class SelectableLabel extends JTextPane {
  private static final long serialVersionUID = 2167053113803999216L;
  public static final Font DEFAULT_FONT = UIManager.getFont("Label.font").deriveFont(Font.PLAIN);
  public static final Color DEFAULT_BACKGROUND = UIManager.getColor("Panel.background");

  public SelectableLabel() {
    super();
    init();
  }

  public SelectableLabel(String text) {
    this();
    setText(text);
  }

  private void init() {
    setEditable(false);
    setFont(DEFAULT_FONT);
    setContentType("text/html");
    setBackground(DEFAULT_BACKGROUND);
    setBorder(null);
  }
}

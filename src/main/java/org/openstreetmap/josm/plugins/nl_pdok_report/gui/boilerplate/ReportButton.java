// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.gui.boilerplate;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;

import org.openstreetmap.josm.plugins.nl_pdok_report.utils.ReportColorScheme;

public class ReportButton extends JButton {
  private static final long serialVersionUID = -2082935371181586227L;

  public ReportButton(final Action action) {
    this(action, false);
  }

  public ReportButton(final Action action, boolean slim) {
    super(action);
    setForeground(Color.WHITE);
    setBorder(slim ? BorderFactory.createEmptyBorder(3, 4, 3, 4) : BorderFactory.createEmptyBorder(7, 10, 7, 10));
  }

  @Override
  protected void paintComponent(final Graphics g) {
    if (!isEnabled()) {
      g.setColor(ReportColorScheme.TOOLBAR_DARK_GREY);
    } else if (getModel().isPressed()) {
      g.setColor(ReportColorScheme.REPORT_BLUE.darker().darker());
    } else if (getModel().isRollover()) {
      g.setColor(ReportColorScheme.REPORT_BLUE.darker());
    } else {
      g.setColor(ReportColorScheme.REPORT_BLUE);
    }
    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.fillRoundRect(0, 0, getWidth(), getHeight(), 3, 3);
    super.paintComponent(g);
  }

  @Override
  public boolean isContentAreaFilled() {
    return false;
  }
}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.nl_pdok_report.model;

import java.io.Serializable;

import javax.swing.ImageIcon;

public class UserProfile extends KeyIndexedObject implements Serializable {
  private static final long serialVersionUID = 4808420837839489132L;
  private final String username;
  private final ImageIcon avatar;

  public UserProfile(String key, String username, ImageIcon avatar) {
    super(key);
    this.avatar = avatar;
    this.username = username;
  }

  public String getUsername() {
    return username;
  }

  public ImageIcon getAvatar() {
    return avatar;
  }
}

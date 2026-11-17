package edu.calpoly.csc3100.fantasy;

/**
 * undefined
 * @author CSC 3100 Instructional Team
 * @version 1.0
 */
public final class Sword implements Character {
  private final Character wrapped;
  public Sword(Character wrapped) { this.wrapped = java.util.Objects.requireNonNull(wrapped); }
  @Override public void draw(java.awt.Graphics2D g, int x, int y) {
    wrapped.draw(g, x, y);
    g.setColor(java.awt.Color.DARK_GRAY);
    g.fillPolygon(new int[]{x + 32, x + 34, x + 36, x + 36, x + 32},
        new int[]{y + 9, y + 5, y + 9, y + 22, y + 22}, 5);
    g.setColor(java.awt.Color.LIGHT_GRAY);
    g.fillRect(x + 33, y + 9, 2, 12);
    g.setColor(new java.awt.Color(110, 65, 30));
    g.fillRect(x + 33, y + 23, 2, 5);
    g.setColor(new java.awt.Color(210, 160, 30));
    g.fillRect(x + 30, y + 21, 8, 3);
  }

  public int attack() { return wrapped.attack() + 5; }
  public int defense() { return wrapped.defense(); }
  public String type() { return wrapped.type(); }
}

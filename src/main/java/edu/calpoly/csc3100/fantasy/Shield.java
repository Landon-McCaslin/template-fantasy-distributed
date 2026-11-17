package edu.calpoly.csc3100.fantasy;

/**
 * undefined
 * @author CSC 3100 Instructional Team
 * @version 1.0
 */
public final class Shield implements Character {
  private final Character wrapped;
  public Shield(Character wrapped) { this.wrapped = java.util.Objects.requireNonNull(wrapped); }
  @Override public void draw(java.awt.Graphics2D g, int x, int y) {
    wrapped.draw(g, x, y);
    g.setColor(java.awt.Color.DARK_GRAY);
    g.fillPolygon(new int[]{x + 2, x + 12, x + 11, x + 7, x + 3},
        new int[]{y + 11, y + 11, y + 21, y + 26, y + 21}, 5);
    g.setColor(new java.awt.Color(210, 160, 30));
    g.fillPolygon(new int[]{x + 4, x + 10, x + 9, x + 7, x + 5},
        new int[]{y + 13, y + 13, y + 20, y + 23, y + 20}, 5);
  }

  public int attack() { return wrapped.attack(); }
  public int defense() { return wrapped.defense() + 5; }
  public String type() { return wrapped.type(); }
}

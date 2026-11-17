package edu.calpoly.csc3100.fantasy;

/**
 * Visual footwear that preserves the wrapped character’s stats.
 * @author CSC 3100 Instructional Team
 * @version 1.0
 */
public final class Shoes implements Character {
  private final Character wrapped;
  public Shoes(Character wrapped) { this.wrapped = java.util.Objects.requireNonNull(wrapped); }
  @Override public void draw(java.awt.Graphics2D g, int x, int y) {
    wrapped.draw(g, x, y);
    g.setColor(new java.awt.Color(110, 65, 30));
    g.fillRoundRect(x + 11, y + 28, 8, 5, 2, 2);
    g.fillRoundRect(x + 21, y + 28, 8, 5, 2, 2);
    g.setColor(java.awt.Color.DARK_GRAY);
    g.fillRect(x + 11, y + 32, 8, 1);
    g.fillRect(x + 21, y + 32, 8, 1);
  }

  public int attack() { return wrapped.attack(); }
  public int defense() { return wrapped.defense(); }
  public String type() { return wrapped.type(); }
}

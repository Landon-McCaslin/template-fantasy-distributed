package edu.calpoly.csc3100.fantasy;

import java.beans.PropertyChangeSupport;
import java.util.*;

/**
 * Player state and change notifications. The application confines access to the Swing EDT.
 * @author CSC 3100 Instructional Team
 * @version 1.0
 */
public final class Blackboard extends PropertyChangeSupport {
  public static final String PLAYERS = "players";
  public static final int COLUMNS = 16;
  public static final int ROWS = 11;
  private static final Set<String> EQUIPMENT = Set.of("sword", "shield", "shoes");
  private final Map<String,PlayerState> players = new LinkedHashMap<>();
  private final GamePublisher publisher;
  private String localUsername;

  public Blackboard(GamePublisher publisher) {
    super(Blackboard.class);
    this.publisher = Objects.requireNonNull(publisher);
  }

  private boolean store(PlayerState state) {
    PlayerState previous = players.get(state.player());
    if (state.equals(previous)) return false;
    players.put(state.player(), state);
    firePropertyChange(new java.beans.PropertyChangeEvent(this, PLAYERS, previous, state));
    return true;
  }
  private void storeLocal(PlayerState state) {
    if (store(state)) publisher.publish(state);
  }
  private PlayerState localPlayer(String username) {
    return localUsername != null && localUsername.equals(username) ? players.get(username) : null;
  }
  public PlayerState getPlayer(String username) { return players.get(username); }
  public Collection<PlayerState> players() { return List.copyOf(players.values()); }
  public void addLocalPlayer(PlayerState state) {
    localUsername = state.player();
    storeLocal(state);
  }
  public void moveLocal(String username, int dx, int dy) {
    if (!((dx == 1 || dx == -1) && dy == 0
        || dx == 0 && (dy == 1 || dy == -1))) return;
    PlayerState state = localPlayer(username);
    if (state == null) return;
    int x = state.x() + dx;
    int y = state.y() + dy;
    if (x < 0 || x >= COLUMNS || y < 0 || y >= ROWS) return;
    storeLocal(new PlayerState(state.player(), state.type(), x, y, state.equipment()));
  }
  public void applyRemote(PlayerState state) {
    if (!state.player().equals(localUsername)) store(state);
  }
  public void equipLocal(String username, String item) {
    if (item == null || !EQUIPMENT.contains(item)) return;
    PlayerState state = localPlayer(username);
    if (state == null || state.equipment().contains(item)) return;
    List<String> equipment = new ArrayList<>(state.equipment());
    equipment.add(item);
    storeLocal(new PlayerState(state.player(), state.type(), state.x(), state.y(), equipment));
  }
  public void unequipLocal(String username, String item) {
    if (item == null || !EQUIPMENT.contains(item)) return;
    PlayerState state = localPlayer(username);
    if (state == null) return;
    List<String> equipment = new ArrayList<>(state.equipment());
    if (!equipment.remove(item)) return;
    storeLocal(new PlayerState(state.player(), state.type(), state.x(), state.y(), equipment));
  }
}

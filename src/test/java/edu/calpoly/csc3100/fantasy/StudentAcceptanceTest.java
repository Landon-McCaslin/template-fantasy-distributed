package edu.calpoly.csc3100.fantasy;

import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeEvent;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StudentAcceptanceTest {
  @Test void blackboardSnapshotsCannotBeModifiedAndKeepTheirOriginalState() {
    Blackboard board = new Blackboard(state -> {});
    PlayerState alice = new PlayerState("alice", "warrior", 2, 3, List.of("shield"));
    board.addLocalPlayer(alice);
    Collection<PlayerState> snapshot = board.players();

    assertThrows(UnsupportedOperationException.class, () -> snapshot.remove(alice));
    assertThrows(UnsupportedOperationException.class,
        () -> snapshot.iterator().next().equipment().add("sword"));
    assertEquals(alice, board.getPlayer("alice"));

    board.moveLocal("alice", 1, 0);
    board.equipLocal("alice", "shoes");

    assertEquals(List.of(alice), List.copyOf(snapshot));
    PlayerState updated = new PlayerState("alice", "warrior", 3, 3, List.of("shield", "shoes"));
    assertEquals(updated, board.getPlayer("alice"));
    assertEquals(List.of(updated), List.copyOf(board.players()));
  }

  @Test void incomingUpdatesNotifyBlackboardOncePerChangeWithoutRepublishing() {
    List<PlayerState> published = new ArrayList<>();
    List<PropertyChangeEvent> events = new ArrayList<>();
    Blackboard board = new Blackboard(published::add);
    PlayerState alice = new PlayerState("alice", "warrior", 0, 0, List.of());
    board.addLocalPlayer(alice);
    board.addPropertyChangeListener(Blackboard.PLAYERS, events::add);
    IncomingPlayerHandler handler = new IncomingPlayerHandler("student-team", "alice", board::applyRemote);
    PlayerState bob = new PlayerState("bob", "warrior", 2, 3, List.of("shield"));
    PlayerState updated = new PlayerState("bob", "warrior", 3, 3, List.of("shield", "shoes"));

    deliver(handler, bob);
    deliver(handler, bob); // MQTT may deliver the same snapshot more than once.
    assertEquals(bob, board.getPlayer("bob"));
    assertEquals(1, events.size());
    assertNull(events.getFirst().getOldValue());
    assertEquals(bob, events.getFirst().getNewValue());

    deliver(handler, updated);
    deliver(handler, updated);
    assertEquals(updated, board.getPlayer("bob"));
    assertEquals(2, events.size());
    assertSame(board, events.getLast().getSource());
    assertEquals(bob, events.getLast().getOldValue());
    assertEquals(updated, events.getLast().getNewValue());
    assertEquals(alice, board.getPlayer("alice"));
    assertEquals(List.of(alice), published); // Only the local join was published.
  }

  @Test void invalidCoordinateTypesAreIgnoredAndNextValidSnapshotIsDelivered() {
    List<PlayerState> received = new ArrayList<>();
    IncomingPlayerHandler handler = new IncomingPlayerHandler("student-team", "alice", received::add);
    String topic = "csc3100/a04/student-team/players/bob";
    for (String coordinate : List.of("1.5", "\"1\"", "true", "4294967296")) {
      String json = "{\"player\":\"bob\",\"type\":\"warrior\",\"x\":" + coordinate
          + ",\"y\":0,\"equipment\":[]}";
      assertDoesNotThrow(() -> handler.accept(topic, json.getBytes(StandardCharsets.UTF_8)));
    }
    assertTrue(received.isEmpty());
    PlayerState valid = new PlayerState("bob", "warrior", 15, 10, List.of("shoes"));
    deliver(handler, valid);
    assertEquals(List.of(valid), received);
  }

  private static void deliver(IncomingPlayerHandler handler, PlayerState state) {
    handler.accept("csc3100/a04/student-team/players/" + state.player(),
        PlayerCodec.encode(state).getBytes(StandardCharsets.UTF_8));
  }

}

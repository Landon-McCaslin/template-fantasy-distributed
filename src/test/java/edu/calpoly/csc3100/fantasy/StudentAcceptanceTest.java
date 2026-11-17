package edu.calpoly.csc3100.fantasy;

import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StudentAcceptanceTest {
  @Test void equipmentAndMovementSynchronizeInBothDirectionsWithoutEchoes() {
    List<PlayerState> alicePublications = new ArrayList<>();
    List<PlayerState> bobPublications = new ArrayList<>();
    List<PropertyChangeEvent> bobEvents = new ArrayList<>();
    Blackboard aliceBoard = new Blackboard(alicePublications::add);
    Blackboard bobBoard = new Blackboard(bobPublications::add);
    bobBoard.addPropertyChangeListener(Blackboard.PLAYERS, bobEvents::add);
    PlayerState bob = new PlayerState("bob", "warrior", 5, 6, List.of());
    aliceBoard.addLocalPlayer(new PlayerState("alice", "warrior", 2, 3, List.of()));
    bobBoard.addLocalPlayer(bob);
    IncomingPlayerHandler toBob = new IncomingPlayerHandler("student-team", "bob", bobBoard::applyRemote);
    IncomingPlayerHandler toAlice = new IncomingPlayerHandler("student-team", "alice", aliceBoard::applyRemote);

    aliceBoard.equipLocal("alice", "shoes");
    aliceBoard.equipLocal("alice", "sword");
    aliceBoard.equipLocal("alice", "shield");
    aliceBoard.moveLocal("alice", 1, 0);
    aliceBoard.unequipLocal("alice", "sword");
    assertEquals(6, alicePublications.size());
    for (PlayerState state : alicePublications) {
      deliver(toBob, state);
      deliver(toBob, state); // QoS 1 may deliver the same snapshot again.
    }

    PlayerState alice = new PlayerState("alice", "warrior", 3, 3, List.of("shoes", "shield"));
    assertEquals(alice, bobBoard.getPlayer("alice"));
    assertEquals(7, bobEvents.size());
    assertEquals(List.of(bob), bobPublications);
    Character remoteCharacter = CharacterFactory.create(bobBoard.getPlayer("alice"));
    assertEquals(10, remoteCharacter.attack());
    assertEquals(10, remoteCharacter.defense());

    bobBoard.equipLocal("bob", "sword");
    bobBoard.moveLocal("bob", 0, -1);
    bobPublications.forEach(state -> deliver(toAlice, state));
    assertEquals(new PlayerState("bob", "warrior", 5, 5, List.of("sword")), aliceBoard.getPlayer("bob"));
    assertEquals(3, bobPublications.size());
    assertEquals(6, alicePublications.size());
    assertEquals(alice, aliceBoard.getPlayer("alice"));
  }

  @Test void allThreeOverlaysRemainVisibleInEveryEquipmentOrder() {
    List<List<String>> orders = List.of(
        List.of("sword", "shield", "shoes"), List.of("sword", "shoes", "shield"),
        List.of("shield", "sword", "shoes"), List.of("shield", "shoes", "sword"),
        List.of("shoes", "sword", "shield"), List.of("shoes", "shield", "sword"));
    BufferedImage bare = draw(List.of());
    for (String item : List.of("sword", "shield", "shoes")) {
      BufferedImage single = draw(List.of(item));
      int visiblePixels = 0;
      for (List<String> order : orders) {
        BufferedImage combined = draw(order);
        List<String> remaining = new ArrayList<>(order);
        remaining.remove(item);
        BufferedImage removed = draw(remaining);
        for (int y = 0; y < 40; y++) {
          for (int x = 0; x < 40; x++) {
            if (single.getRGB(x, y) != bare.getRGB(x, y)) {
              visiblePixels++;
              assertEquals(single.getRGB(x, y), combined.getRGB(x, y), item + " must remain visible");
              assertEquals(bare.getRGB(x, y), removed.getRGB(x, y), item + " must disappear on removal");
            }
          }
        }
      }
      assertTrue(visiblePixels > 0, item + " must have a visible overlay");
    }
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

  private static BufferedImage draw(List<String> equipment) {
    BufferedImage image = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = image.createGraphics();
    try {
      CharacterFactory.create(new PlayerState("alice", "warrior", 0, 0, equipment))
          .draw(graphics, 0, 0);
    } finally {
      graphics.dispose();
    }
    return image;
  }
}

package edu.calpoly.csc3100.fantasy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * undefined
 * @author CSC 3100 Instructional Team
 * @version 1.0
 */
public final class PlayerCodec {
  private static final ObjectMapper MAPPER = new ObjectMapper()
      .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
      .enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
  private PlayerCodec() { }
  public static String encode(PlayerState state) {
    try { return MAPPER.writeValueAsString(state); }
    catch (Exception e) { throw new IllegalArgumentException("Cannot encode player", e); }
  }
  public static PlayerState decode(String json) {
    JsonNode snapshot;
    try {
      snapshot = MAPPER.readTree(json);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Cannot decode player", e);
    }
    if (snapshot == null || !snapshot.isObject()) {
      throw new IllegalArgumentException("Player snapshot must be an object");
    }

    JsonNode player = snapshot.path("player");
    JsonNode type = snapshot.path("type");
    JsonNode x = snapshot.path("x");
    JsonNode y = snapshot.path("y");
    JsonNode equipment = snapshot.path("equipment");
    if (!player.isTextual() || player.textValue().isBlank()) {
      throw new IllegalArgumentException("Player name is required");
    }
    if (!type.isTextual() || !"warrior".equalsIgnoreCase(type.textValue())) {
      throw new IllegalArgumentException("Unsupported character type");
    }
    if (!x.isIntegralNumber() || !x.canConvertToInt()
        || !y.isIntegralNumber() || !y.canConvertToInt()
        || x.intValue() < 0 || x.intValue() >= Blackboard.COLUMNS
        || y.intValue() < 0 || y.intValue() >= Blackboard.ROWS) {
      throw new IllegalArgumentException("Player coordinates must be within the board");
    }
    if (!equipment.isArray()) {
      throw new IllegalArgumentException("Equipment must be an array");
    }
    List<String> items = new ArrayList<>();
    Set<String> seen = new HashSet<>();
    for (JsonNode item : equipment) {
      if (!item.isTextual()
          || !("sword".equals(item.textValue()) || "shield".equals(item.textValue())
              || "shoes".equals(item.textValue()))
          || !seen.add(item.textValue())) {
        throw new IllegalArgumentException("Equipment must contain unique supported items");
      }
      items.add(item.textValue());
    }
    return new PlayerState(player.textValue(), type.textValue(), x.intValue(), y.intValue(), items);
  }
}

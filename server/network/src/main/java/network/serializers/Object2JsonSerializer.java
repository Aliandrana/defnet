package network.serializers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import network.Serializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author k.usachev
 */
public enum Object2JsonSerializer implements Serializer<Object, JsonObject> {
  Instance;

  @NotNull
  private final Logger log = LoggerFactory.getLogger(Object2JsonSerializer.class);

  @NotNull
  private static final Gson gson = new Gson();
  @NotNull
  private static final JsonParser parser = new JsonParser();

  @NotNull
  private final Map<String, Class<?>> name2type = new HashMap<>();

  public void register(@NotNull String typeName, @NotNull Class<?> type) {
    name2type.put(typeName, type);
  }

  @NotNull
  @Override
  public Class<Object> getBaseClass() {
    return Object.class;
  }

  @NotNull
  @Override
  public JsonObject serialize(@NotNull Object msg) throws IOException {
    if (msg instanceof JsonObject)
      return (JsonObject) msg;
    return (JsonObject) gson.toJsonTree(msg);
  }

  @NotNull
  @Override
  public Object deserialize(@NotNull JsonObject msg) throws IOException {
    final JsonElement typeElmt = msg.get("type");
    final String type = typeElmt != null && typeElmt.isJsonPrimitive() && typeElmt.getAsJsonPrimitive().isString() ? typeElmt.getAsString() : null;
    if (type == null)
      return msg;
    final Class<?> objType = name2type.get(type);
    if (objType == null) {
      log.error("Unknown type {} at {}", type, msg);
      return msg;
    }
    return gson.fromJson(msg, objType);
  }

}

package utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * @author k.usachev
 */
public enum JsonUtils {
  ;

  @NotNull
  static final Logger log = LoggerFactory.getLogger(JsonUtils.class);

  @Nullable
  public static String tryGetString(@NotNull JsonObject obj, @NotNull String name) {
    final JsonPrimitive primitive = tryGetPrimitive(obj, name);
    return primitive != null && primitive.isString() ? primitive.getAsString() : null;
  }

  @Nullable
  public static String getString(@NotNull JsonObject obj, @NotNull String name) {
    final JsonPrimitive primitive = getPrimitive(obj, name);
    if (primitive == null)
      return null;
    if (!primitive.isString()) {
      log.warn("Elmt '{}' from msg {} isn't a String", primitive, obj);
      return null;
    }
    return primitive.getAsString();
  }

  @Nullable
  public static JsonPrimitive tryGetPrimitive(@NotNull JsonObject obj, @NotNull String name) {
    final JsonElement elmt = tryGetElement(obj, name);
    return elmt instanceof JsonPrimitive ? (JsonPrimitive) elmt : null;
  }

  @Nullable
  public static JsonPrimitive getPrimitive(@NotNull JsonObject obj, @NotNull String name) {
    final JsonElement elmt = getElement(obj, name);
    if (elmt == null)
      return null;
    if (!(elmt instanceof JsonPrimitive)) {
      log.warn("Elmt '{}' from msg {} isn't a JsonPrimitive", elmt, obj);
      return null;
    }
    return (JsonPrimitive) elmt;
  }

  @Nullable
  public static JsonElement tryGetElement(@NotNull JsonObject obj, @NotNull String name) {
    return obj.get(name);
  }

  @Nullable
  public static JsonElement getElement(@NotNull JsonObject obj, @NotNull String name) {
    final JsonElement result = obj.get(name);
    if (result == null)
      log.warn("No '{}' field at obj {}", name, obj);
    return result;
  }

  @NotNull
  public static JsonObject createFrom(@NotNull Map<String, Object> map) {
    final JsonObject result = new JsonObject();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      final Object value = entry.getValue();
      if (value instanceof Number)
        result.addProperty(entry.getKey(), (Number) value);
      else if (value instanceof String)
        result.addProperty(entry.getKey(), (String) value);
      else if (value instanceof Boolean)
        result.addProperty(entry.getKey(), (Boolean) value);
      else if (value instanceof JsonElement)
        result.add(entry.getKey(), (JsonElement) value);
      else
        log.error("Unknown object {} from {}", value, map);
    }
    return result;
  }

  @NotNull
  public static JsonObject createFrom(@NotNull String key, @NotNull Object value) {
    return createFrom(Collections.singletonMap(key, value));
  }
}

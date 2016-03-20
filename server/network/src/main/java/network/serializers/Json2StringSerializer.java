package network.serializers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import network.Serializer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author k.usachev
 */
public enum Json2StringSerializer implements Serializer<JsonObject, String> {
  Instance;

  @NotNull
  private static final Gson gson = new Gson();
  @NotNull
  private static final JsonParser parser = new JsonParser();

  @NotNull
  @Override
  public Class<JsonObject> getBaseClass() {
    return JsonObject.class;
  }

  @NotNull
  @Override
  public String serialize(@NotNull JsonObject msg) throws IOException {
    return gson.toJson(msg);
  }

  @NotNull
  @Override
  public JsonObject deserialize(@NotNull String msg) throws IOException {
    return parser.parse(msg).getAsJsonObject();
  }
}

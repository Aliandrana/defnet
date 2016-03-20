package frontend;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.JsonUtils;

import java.net.InetSocketAddress;

/**
 * @author k.usachev
 */
public enum SystemMessages {
  ;

  public static JsonObject redirect(@NotNull InetSocketAddress address, @NotNull String type, @Nullable JsonObject handshake) {
    return JsonUtils.createFrom(ImmutableMap.of("type", "redirect", "room_type", type, "host", address.getHostName()
        , "port", address.getPort(), "handshake", handshake == null ? new JsonObject() : handshake));
  }
}

package dispatcher;

import com.google.gson.JsonObject;
import framework.actors.Actor;
import frontend.Client;
import frontend.Frontend;
import frontend.HandshakeFunction;
import frontend.SystemMessages;
import network.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import room.Room;
import utils.JsonUtils;
import utils.RandomUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Dispatcher accepts new connections from clients, waits for handshake and
 * routes them to other frontend, or, in case of absence, starts them at first.
 *
 * @author k.usachev
 */
public class Dispatcher implements HandshakeFunction<JsonObject> {
  @NotNull
  private static final Logger log = LoggerFactory.getLogger(Dispatcher.class);
  @NotNull
  private final Function<String, Room> roomCreator;
  @NotNull
  private final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();

  public Dispatcher(@NotNull Function<String, Room> roomCreator) {
    this.roomCreator = roomCreator;
  }

  @Override
  @Nullable
  public Actor handshake(@NotNull Session<JsonObject> session, @NotNull JsonObject handshake) {
    final String appId = JsonUtils.getString(handshake, "appId");
    if (appId == null) {
      log.info("No app id at {} from {}", handshake, session);
      return null;
    }

    final Room room = rooms.computeIfAbsent(appId, roomCreator::apply);
    final Frontend frontend = RandomUtils.pick(room.getFrontends());
    if (frontend == null) {
      log.warn("Can't get frontend for {} from {}", session, room);
      return null;
    }
    session.sendMessage(SystemMessages.redirect(frontend.getPublicAddress(), "lobby", null));
    return Client.create(session);
  }
}

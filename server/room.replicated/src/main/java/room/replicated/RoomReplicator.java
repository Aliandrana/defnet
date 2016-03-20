package room.replicated;

import com.google.gson.JsonObject;
import framework.actors.Actor;
import framework.actors.ActorComponent;
import framework.actors.ActorRef;
import framework.actors.MessagesHandler;
import frontend.Client;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.JsonUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author k.usachev
 */
final class RoomReplicator implements ActorComponent {
  @NotNull
  private static final Logger log = LoggerFactory.getLogger(RoomReplicator.class);

  @NotNull
  private final Map<String, JsonObject> netObjects = new HashMap<>();
  @NotNull
  private final Set<ActorRef> allClients = new HashSet<>();
  @NotNull
  private final MessagesHandler<NewClientConnectedMsg> clientsHandler = (actor, msg) -> {
    allClients.add(msg.getClient());
    actor.watch(msg.getClient(), allClients::remove);
    // replicating current state
    netObjects.values().forEach(cmd -> Client.send(actor, msg.getClient(), cmd));
  };

  @NotNull
  private final MessagesHandler<NewCommandFromClientMsg> commandsHandler = (actor, msg) -> {
    final JsonObject cmd = msg.getCommand();

    final String type = JsonUtils.getString(cmd, "type");
    if ("post".equals(type)) {
      final String from = JsonUtils.tryGetString(cmd, "from");
      final String to = JsonUtils.getString(cmd, "to");
      final String name = JsonUtils.getString(cmd, "name");
      if (to == null || name == null || to.startsWith("@")) // hack for system objects (
        return;
      if (!netObjects.containsKey(to)) {
        log.warn("Trying to send cmd {} to {}", cmd, to);
        return;
      }
      if (from == null || !netObjects.containsKey(from))
        cmd.remove("from");
    } else if ("spawn".equals(type)) {
      final String obj = JsonUtils.getString(cmd, "obj");
      if (obj == null) {
        log.warn("Trying to spawn object without id {}", cmd);
        return;
      }
      if (netObjects.putIfAbsent(obj, cmd) != null) {
        log.warn("Trying to spawn object with the same id {}", cmd);
        return;
      }
    } else if ("delete".equals(type)) {
      final String obj = JsonUtils.getString(cmd, "obj");
      if (obj == null) {
        log.warn("Trying to delete object without id {}", cmd);
        return;
      }
      if (netObjects.remove(obj) == null) {
        log.warn("Trying to delete already deleted object {}", cmd);
        return;
      }
    }
    // replicating command to all clients except sender
    allClients.stream().filter(client -> !msg.getClient().equals(client)).forEach(actorRef -> Client.send(actor, actorRef, cmd));
  };

  public static void create(@NotNull Actor holder) {
    RoomReplicator replicator = new RoomReplicator();
    holder.addHandler(NewClientConnectedMsg.class, replicator.clientsHandler);
    holder.addHandler(NewCommandFromClientMsg.class, replicator.commandsHandler);
  }
}

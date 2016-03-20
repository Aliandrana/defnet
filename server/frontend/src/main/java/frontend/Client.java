package frontend;

import com.google.gson.JsonObject;
import framework.actors.*;
import network.Session;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.JsonUtils;

import java.util.UUID;

/**
 * @author k.usachev
 */
public final class Client implements ActorComponent, ActorLifecycleObserver {
  @NotNull
  private static final Logger log = LoggerFactory.getLogger(Client.class);

  @NotNull
  private final Session<JsonObject> session;

  public Client(@NotNull Session<JsonObject> session) {
    this.session = session;
  }

  @NotNull
  public static Actor create(@NotNull Session<JsonObject> session) {
    final Actor actor = new Actor(UrlRef.createAbsolute("defnet", UUID.randomUUID().toString()));
    final Client client = new Client(session);
    actor.addComponent(Client.class, client);
    actor.addLifecycleObserver(client);
    return actor;
  }

  public static void send(@NotNull World world, @NotNull ActorRef client, @NotNull JsonObject msg) {
    world.send(client, new SendMessageMsg(msg));
  }

  public static void send(@NotNull Actor someActor, @NotNull ActorRef client, @NotNull JsonObject msg) {
    someActor.send(client, new SendMessageMsg(msg));
  }

  public static void send(@NotNull Actor clientActor, @NotNull JsonObject msg) {
    clientActor.get(Client.class).session.sendMessage(msg);
  }

  public static void kick(@NotNull Actor clientActor) {
    clientActor.get(Client.class).session.close();
  }

  @Override
  public void onCreated(@NotNull Actor holder) {
    holder.addHandler(SendMessageMsg.class, new OnSendMessageMsgHandler());
    holder.addHandler(NewCommand.class, new OnMessageMsgHandler());
  }

  @Override
  public void onDeleted(@NotNull Actor holder) {

  }

  private final class OnSendMessageMsgHandler implements MessagesHandler<SendMessageMsg> {

    @Override
    public void handle(@NotNull Actor actor, @NotNull SendMessageMsg msg) {
      send(actor, msg.getMsg());
    }
  }

  private final class OnMessageMsgHandler implements MessagesHandler<NewCommand> {

    @Override
    public void handle(@NotNull Actor actor, @NotNull NewCommand msg) {
      log.debug("{} received message {}", actor, msg);
      final JsonObject cmd = msg.getMessage();
      final String type = JsonUtils.getString(cmd, "type");
      if (!"post".equals(type))
        return;
      final String to = JsonUtils.getString(cmd, "to");
      final String name = JsonUtils.getString(cmd, "name");
      if (to == null || name == null)
        return;
      if (to.startsWith("@")) { //deliver system related message only here
        final ActorRef fromRef = actor.getRef();
        actor.send(UrlRef.createSystem(to.substring(1)), new Command(fromRef, name, JsonUtils.tryGetElement(cmd, "params")));
      }
    }
  }
}

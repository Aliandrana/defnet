package room;

import com.google.gson.JsonObject;
import framework.actors.Actor;
import framework.actors.Mailbox;
import framework.actors.World;
import frontend.Frontend;
import frontend.HandshakeFunction;
import network.NetEngineInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Just an world with frontend and handlers to populate this world with actors.
 *
 * @author k.usachev
 */
public final class Room extends World {
  @Nullable
  private final HandshakeFunction<JsonObject> handshake;
  @NotNull
  private final List<Consumer<Actor>> newClientHandlers = new CopyOnWriteArrayList<>();
  @NotNull
  private final CopyOnWriteArrayList<Frontend> frontends = new CopyOnWriteArrayList<>();

  public Room(@NotNull Mailbox mailbox, @Nullable HandshakeFunction<JsonObject> handshake) {
    super(mailbox);
    this.handshake = handshake;
  }

  public void addNewClientHandler(@NotNull Consumer<Actor> newClient) {
    newClientHandlers.add(newClient);
  }

  @NotNull
  public Room addFrontend(@NotNull NetEngineInstance<JsonObject> network) {
    frontends.add(Frontend.launch(this, network, handshake, actor -> newClientHandlers.forEach(actorConsumer -> actorConsumer.accept(actor))));
    return this;
  }

  @NotNull
  public Frontend[] getFrontends() {
    return frontends.stream().toArray(Frontend[]::new);
  }
}

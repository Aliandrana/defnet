package frontend;

import com.google.gson.JsonObject;
import framework.actors.*;
import network.NetEngineInstance;
import network.NewSessionHandler;
import network.Session;
import network.SessionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Service which handles sessions.
 *
 * @author k.usachev
 */
public final class Frontend implements AutoCloseable, ActorComponent, ActorLifecycleObserver {

  @NotNull
  private static final Logger log = LoggerFactory.getLogger(Frontend.class);

  @NotNull
  private final NetEngineInstance<JsonObject> network;

  @NotNull
  public static Frontend launch(@NotNull World world, @NotNull NetEngineInstance<JsonObject> network,
                                @Nullable HandshakeFunction<JsonObject> handshake) {
    return launch(world, network, handshake, actor -> {
    });
  }

  @NotNull
  public static Frontend launch(@NotNull World world, @NotNull NetEngineInstance<JsonObject> network,
                                @Nullable HandshakeFunction<JsonObject> handshake, @NotNull Consumer<Actor> newClient) {
    final Actor actor = new Actor(UniqueRef.create());
    network.setHandler(new SessionHandlerCreator(actor, handshake, newClient));
    final Frontend frontend = new Frontend(network);
    actor.addComponentUnique(Frontend.class, frontend);
    actor.addLifecycleObserver(frontend);
    world.register(actor);
    return frontend;
  }

  private Frontend(@NotNull NetEngineInstance<JsonObject> network) {
    this.network = network;
  }

  @Override
  public void close() throws Exception {
    network.close();
  }

  @NotNull
  public InetSocketAddress getPublicAddress() {
    return network.getAddress();
  }

  @Override
  public void onCreated(@NotNull Actor holder) {

  }

  @Override
  public void onDeleted(@NotNull Actor holder) {
    try {
      close();
    } catch (Exception e) {
      log.error("Can't close frontend", e);
    }
  }

  private static final class SessionHandlerCreator implements NewSessionHandler<JsonObject> {
    @NotNull
    private final Actor holder;
    @Nullable
    private final HandshakeFunction<JsonObject> handshake;
    @NotNull
    private final Consumer<Actor> newClient;

    public SessionHandlerCreator(@NotNull Actor holder, @Nullable HandshakeFunction<JsonObject> handshake, @NotNull Consumer<Actor> newClient) {
      this.holder = holder;
      this.handshake = handshake;
      this.newClient = newClient;
    }

    @Override
    @NotNull
    public SessionHandler<JsonObject> createHandler(@NotNull Session<JsonObject> session) {
      log.debug("New session {}", session);
      return new Handler(session);
    }

    private final class Handler implements SessionHandler<JsonObject> {
      @NotNull
      private final Session<JsonObject> session;
      @Nullable
      private volatile ActorRef client;

      public Handler(@NotNull Session<JsonObject> session) {
        this.session = session;
        if (handshake == null)
          createClient(null);
      }

      @Override
      public void onMessage(@NotNull JsonObject message) {
        if (client != null)
          sendMessage(message);
        else
          createClient(message);
      }

      private void sendMessage(@NotNull JsonObject message) {
        final ActorRef clientSafe = Objects.requireNonNull(client);
        holder.send(clientSafe, NewCommand.create(message));
      }

      private void createClient(@Nullable JsonObject handshakeMsg) {
        final Actor client = handshake != null && handshakeMsg != null
            ? handshake.handshake(session, handshakeMsg)
            : Client.create(session);
        if (client == null) {
          session.close();
          return;
        }
        holder.register(client);
        this.client = client.getRef();
        holder.watch(holder.getRef(), actorRef -> this.client = null);
        newClient.accept(client);
      }

      @Override
      public void onClosed() {
        log.debug("Session closed {}", session);
        session.close();
      }
    }
  }
}

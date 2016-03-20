package launcher;

import com.google.gson.JsonObject;
import dispatcher.Dispatcher;
import framework.actors.Mailbox;
import framework.actors.World;
import framework.actors.mailbox.MultiThreadingMailbox;
import frontend.Frontend;
import lobby.matchmaking.Matchmaking;
import network.NetEngineInstance;
import network.netty.NettyEngine;
import network.serializers.Json2StringSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import room.Room;
import room.replicated.ReplicatedRoom;

import java.util.function.Function;

/**
 * Server launcher.
 *
 * @author k.usachev
 */
public class EntryPoint {

  public static void main(@NotNull String... args) throws InterruptedException {
    final Mailbox mailbox = MultiThreadingMailbox.create(4);
    final String host = args.length > 0 ? args[0] : "localhost";
    final int port = args.length > 1 ? Integer.parseInt(args[1]) : 8282;
    final World world = new World(mailbox);
    final NetEngineInstance<JsonObject> net = NettyEngine.Instance.launch(host, port, Json2StringSerializer.Instance);
    Frontend.launch(world, net, new Dispatcher(new LobbyCreator(mailbox, host)));
  }

  private static final class LobbyCreator implements Function<String, Room> {
    @NotNull
    private final Mailbox mailbox;
    @NotNull
    private final String host;

    public LobbyCreator(@NotNull Mailbox mailbox, @NotNull String host) {
      this.mailbox = mailbox;
      this.host = host;
    }

    @Override
    @Nullable
    public Room apply(@NotNull String appId) {
      final Room lobby = new Room(mailbox, null);
      Matchmaking.create(lobby, mailbox, () -> NettyEngine.Instance.launch(host, 0, Json2StringSerializer.Instance), ReplicatedRoom::make);
      lobby.addFrontend(NettyEngine.Instance.launch(host, 0, Json2StringSerializer.Instance));
      return lobby;
    }
  }
}

package lobby.matchmaking;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import framework.actors.*;
import frontend.Client;
import frontend.Command;
import frontend.Frontend;
import frontend.SystemMessages;
import network.NetEngineInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import room.Room;
import utils.JsonUtils;
import utils.RandomUtils;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Primitive matchmaking logic.
 *
 * @author k.usachev
 */
public final class Matchmaking implements ActorComponent, MessagesHandler<Command> {

  @NotNull
  private static final Logger log = LoggerFactory.getLogger(Matchmaking.class);

  @NotNull
  private final Mailbox matchesMailbox;
  @NotNull
  private final Supplier<NetEngineInstance<JsonObject>> network;
  @NotNull
  private final ConcurrentLinkedQueue<ActorRef> matchmakingQueue = new ConcurrentLinkedQueue<>();
  @NotNull
  private final List<Room> matches = Collections.synchronizedList(new ArrayList<>());
  @NotNull
  private final Consumer<Room> matchCreated;

  public static void create(@NotNull World world, @NotNull Mailbox matchesMailbox, @NotNull Supplier<NetEngineInstance<JsonObject>> network, @NotNull Consumer<Room> matchCreated) {
    final Actor actor = new Actor(UrlRef.createSystem("matchmaking"));
    final Matchmaking matchmaking = new Matchmaking(matchesMailbox, network, matchCreated);
    actor.addComponent(Matchmaking.class, matchmaking);
    actor.addHandler(Command.class, matchmaking);
    world.register(actor);
  }

  private Matchmaking(@NotNull Mailbox matchesMailbox, @NotNull Supplier<NetEngineInstance<JsonObject>> network, @NotNull Consumer<Room> matchCreated) {
    this.matchesMailbox = matchesMailbox;
    this.network = network;
    this.matchCreated = matchCreated;
  }

  @Override
  public void handle(@NotNull Actor actor, @NotNull Command cmd) {
    if (cmd.getName().equals("find")) {
      findGame(actor, cmd.getFrom());
    } else if (cmd.getName().equals("cancel")) {
      cancelFindFGame(cmd.getFrom());
    }
  }

  private void findGame(@NotNull Actor mm, @NotNull ActorRef client) {
    if (matchmakingQueue.contains(client))
      return;
    final ActorRef opponent = matchmakingQueue.poll();
    if (opponent != null) {
      startGame(mm, client, opponent);
    } else {
      log.debug("Searching for a game for {}", client);
      matchmakingQueue.add(client);
    }
  }

  private void cancelFindFGame(@NotNull ActorRef client) {
    matchmakingQueue.remove(client);
  }

  private void startGame(@NotNull Actor actor, @NotNull ActorRef... participants) {
    if (log.isDebugEnabled())
      log.debug("Starting game for {}", Arrays.stream(participants).map(Object::toString).collect(Collectors.joining(",")));
    final String[] tickets = Arrays.stream(participants).map(a -> UUID.randomUUID().toString()).toArray(String[]::new);
    final Room room = createRoom(tickets);
    if (room == null)
      return;
    final Frontend frontend = RandomUtils.pick(room.getFrontends());
    if (frontend == null) {
      log.error("Can't obtain frontend for {}", Arrays.stream(participants).map(Object::toString).collect(Collectors.joining(",")));
      return;
    }
    matches.add(room);
    matchCreated.accept(room);
    for (int i = 0; i < participants.length; i++) {
      final JsonObject msg = SystemMessages.redirect(frontend.getPublicAddress(), "match",
          JsonUtils.createFrom(ImmutableMap.of("ticket", tickets[i], "idx", i + 1)));
      Client.send(actor, participants[i], msg);
    }
  }

  @Nullable
  private Room createRoom(@NotNull String[] tickets) {
    final Set<String> ticketsLeft = new ConcurrentSkipListSet<>();
    Collections.addAll(ticketsLeft, tickets);
    final Room match = new Room(matchesMailbox, (session, handshake) -> {
      if (!ticketsLeft.remove(JsonUtils.getString(handshake, "ticket")))
        return null;
      return Client.create(session);
    });
    match.addFrontend(network.get());
    return match;
  }
}

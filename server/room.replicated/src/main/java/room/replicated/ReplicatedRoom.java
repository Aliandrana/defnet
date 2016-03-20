package room.replicated;

import framework.actors.Actor;
import framework.actors.ActorRef;
import framework.actors.MessagesHandler;
import framework.actors.UniqueRef;
import frontend.NewCommand;
import org.jetbrains.annotations.NotNull;
import room.Room;

/**
 * @author k.usachev
 */
public enum ReplicatedRoom {
  ;

  public static void make(@NotNull Room room) {
    final ActorRef replicatorRef = UniqueRef.create();
    final Actor replicator = new Actor(replicatorRef);
    RoomReplicator.create(replicator);
    room.register(replicator);
    room.addNewClientHandler(client -> {
      client.addHandler(NewCommand.class, new CommandsHandler(replicatorRef));
      client.send(replicatorRef, new NewClientConnectedMsg(client.getRef()));
    });
  }

  private static final class CommandsHandler implements MessagesHandler<NewCommand> {
    @NotNull
    private final ActorRef replicator;

    public CommandsHandler(@NotNull ActorRef replicator) {
      this.replicator = replicator;
    }

    @Override
    public void handle(@NotNull Actor actor, @NotNull NewCommand msg) {
      actor.send(replicator, new NewCommandFromClientMsg(actor.getRef(), msg.getMessage()));
    }
  }
}

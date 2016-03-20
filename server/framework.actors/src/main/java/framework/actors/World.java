package framework.actors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Bunch of actors with references to each other.
 *
 * @author k.usachev
 */
public class World implements ActorHolder, ActorsHolder {
  @NotNull
  private static final Logger log = LoggerFactory.getLogger(World.class);
  @NotNull
  private final Mailbox mailbox;
  @NotNull
  private final Map<ActorRef, Actor> actors = new ConcurrentHashMap<>();

  public World(@NotNull Mailbox mailbox) {
    this.mailbox = mailbox;
  }

  @NotNull
  private final DeleteTask deleteTask = new DeleteTask();

  public void register(@NotNull Actor... newActors) {
    Arrays.stream(newActors).forEach(actor -> {
      if (actors.putIfAbsent(actor.getRef(), actor) != null)
        log.error("Trying to register duplicate actor {}", actor);
      else {
        actor.onCreated(this);
      }
    });
  }

  @Override
  public void delete(@NotNull ActorRef actorRef) {
    mailbox.send(this, actorRef, deleteTask);
  }

  @Override
  public void watch(@NotNull ActorRef watcher, @NotNull ActorRef actorRef, @NotNull Consumer<ActorRef> onDeleted) {
    ActorWatchers.watch(this, watcher, actorRef, onDeleted);
  }

  @Override
  @Nullable
  public Actor find(@NotNull ActorRef ref) {
    return actors.get(ref);
  }

  public void send(@NotNull ActorRef ref, @NotNull ActorTask task) {
    mailbox.send(this, ref, task);
  }

  public void send(@NotNull ActorRef ref, @NotNull Message msg) {
    send(ref, new HandleMessageTask(msg));
  }

  @NotNull
  Mailbox getMailbox() {
    return mailbox;
  }

  private static final class HandleMessageTask implements ActorTask {
    @NotNull
    private final Message msg;

    public HandleMessageTask(@NotNull Message msg) {
      this.msg = msg;
    }

    @Override
    public void execute(@NotNull Actor actor) {
      actor.handle(msg);
    }

    @Override
    public void onFail(@NotNull ActorRef actor) {
      log.warn("Couldn't deliver message {} to {}", msg, actor);
    }
  }

  private final class DeleteTask implements ActorTask {
    @Override
    public void execute(@NotNull Actor actor) {
      if (actors.remove(actor.getRef()) == null)
        return;
      actor.onDeleted();
    }

    @Override
    public void onFail(@NotNull ActorRef actor) {
      //do nothing
    }
  }
}

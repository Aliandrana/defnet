package framework.actors;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * @author k.usachev
 */
final class ActorWatchers implements ActorComponent, ActorLifecycleObserver {
  @NotNull
  private final Multimap<ActorRef, Consumer<ActorRef>> watchers = LinkedListMultimap.create();
  @NotNull
  private final ActorHolder holder;

  public ActorWatchers(@NotNull ActorHolder holder) {
    this.holder = holder;
  }

  public static void watch(@NotNull ActorHolder holder, @NotNull ActorRef watcher, @NotNull ActorRef target, @NotNull Consumer<ActorRef> action) {
    holder.send(target, new WatchActorTask(holder, watcher, action));
  }

  @NotNull
  public static ActorWatchers retrive(@NotNull ActorHolder holder, @NotNull Actor actor) {
    ActorWatchers watchers = actor.tryGet(ActorWatchers.class);
    if (watchers == null) {
      watchers = new ActorWatchers(holder);
      actor.addComponent(ActorWatchers.class, watchers);
      actor.addLifecycleObserver(watchers);
    }
    return watchers;
  }

  private void addWatcher(@NotNull ActorRef actor, @NotNull Consumer<ActorRef> action) {
    watchers.put(actor, action);
  }

  @Override
  public void onCreated(@NotNull Actor holder) {

  }

  @Override
  public void onDeleted(@NotNull Actor holder) {
    watchers.entries().forEach(e -> this.holder.send(e.getKey(), new NotifyDeletedTask(holder.getRef(), e.getValue())));
  }

  private static final class WatchActorTask implements ActorTask {
    @NotNull
    private final ActorHolder holder;
    @NotNull
    private final ActorRef watcher;
    @NotNull
    private final Consumer<ActorRef> action;

    public WatchActorTask(@NotNull ActorHolder holder, @NotNull ActorRef watcher, @NotNull Consumer<ActorRef> action) {
      this.holder = holder;
      this.watcher = watcher;
      this.action = action;
    }

    @Override
    public void execute(@NotNull Actor actor) {
      ActorWatchers.retrive(holder, actor).addWatcher(watcher, action);
    }

    @Override
    public void onFail(@NotNull ActorRef actor) {
      holder.send(watcher, new NotifyDeletedTask(actor, action));
    }
  }

  private static final class NotifyDeletedTask implements ActorTask {
    @NotNull
    private final ActorRef deleted;
    @NotNull
    private final Consumer<ActorRef> action;

    public NotifyDeletedTask(@NotNull ActorRef deleted, @NotNull Consumer<ActorRef> action) {
      this.deleted = deleted;
      this.action = action;
    }

    @Override
    public void execute(@NotNull Actor actor) {
      action.accept(deleted);
    }

    @Override
    public void onFail(@NotNull ActorRef actor) {
      // do nothing
    }
  }
}
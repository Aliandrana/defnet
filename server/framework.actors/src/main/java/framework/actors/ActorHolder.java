package framework.actors;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * @author k.usachev
 */
public interface ActorHolder {
  void send(@NotNull ActorRef ref, @NotNull ActorTask task);

  void send(@NotNull ActorRef ref, @NotNull Message msg);

  void delete(@NotNull ActorRef actorRef);

  void watch(@NotNull ActorRef watcher, @NotNull ActorRef actorRef, @NotNull Consumer<ActorRef> onDeleted);

  void register(@NotNull Actor... newActors);
}

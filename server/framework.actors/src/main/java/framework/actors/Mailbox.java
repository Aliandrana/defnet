package framework.actors;

import org.jetbrains.annotations.NotNull;

/**
 * Particular implementation of message delivery system.
 *
 * @author k.usachev
 */
public interface Mailbox {
  void send(@NotNull ActorsHolder holder, @NotNull ActorRef ref, @NotNull ActorTask task);
}

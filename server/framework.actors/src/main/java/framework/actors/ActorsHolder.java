package framework.actors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author k.usachev
 */
public interface ActorsHolder {
  @Nullable
  Actor find(@NotNull ActorRef ref);
}

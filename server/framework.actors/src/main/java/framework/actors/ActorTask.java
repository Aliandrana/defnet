package framework.actors;

import org.jetbrains.annotations.NotNull;

/**
 * @author k.usachev
 */
public interface ActorTask {
  void execute(@NotNull Actor actor);

  void onFail(@NotNull ActorRef actor);
}

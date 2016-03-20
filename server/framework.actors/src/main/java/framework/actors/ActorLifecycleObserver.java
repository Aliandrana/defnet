package framework.actors;

import org.jetbrains.annotations.NotNull;

/**
 * Observer for actor's lifecycle.
 *
 * @author k.usachev
 */
public interface ActorLifecycleObserver {
  void onCreated(@NotNull Actor holder);

  void onDeleted(@NotNull Actor holder);
}

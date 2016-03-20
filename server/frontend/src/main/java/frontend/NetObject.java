package frontend;

import framework.actors.Actor;
import framework.actors.ActorComponent;
import framework.actors.ActorLifecycleObserver;
import org.jetbrains.annotations.NotNull;

/**
 * todo: javadoc
 *
 * @author k.usachev
 */
public final class NetObject implements ActorComponent, ActorLifecycleObserver {
  public NetObject(@NotNull Actor actor) {
    actor.addLifecycleObserver(this);
  }

  public static void attach(@NotNull Actor actor) {
    actor.addComponentUnique(NetObject.class, new NetObject(actor));
  }

  @Override
  public void onCreated(@NotNull Actor holder) {
    holder.addLifecycleObserver(this);
  }

  @Override
  public void onDeleted(@NotNull Actor holder) {

  }
}

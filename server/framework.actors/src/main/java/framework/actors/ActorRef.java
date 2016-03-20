package framework.actors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author k.usachev
 */
public abstract class ActorRef {
  @Override
  public final boolean equals(@Nullable Object obj) {
    return obj instanceof ActorRef && equals((ActorRef) obj);
  }

  @Override
  public final int hashCode() {
    return getHashCode();
  }

  protected abstract boolean equals(@NotNull ActorRef other);

  protected abstract int getHashCode();
}

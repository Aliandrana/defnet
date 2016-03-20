package framework.actors;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * @author k.usachev
 */
public final class UniqueRef extends ActorRef {
  @NotNull
  private final UUID uuid = UUID.randomUUID();

  @NotNull
  public static UniqueRef create() {
    return new UniqueRef();
  }

  private UniqueRef() {
  }

  @Override
  protected boolean equals(@NotNull ActorRef other) {
    return other instanceof UniqueRef && uuid.equals(((UniqueRef) other).uuid);
  }

  @Override
  protected int getHashCode() {
    return uuid.hashCode();
  }
}

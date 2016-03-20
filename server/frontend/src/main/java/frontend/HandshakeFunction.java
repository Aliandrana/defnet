package frontend;

import framework.actors.Actor;
import network.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author k.usachev
 */
@FunctionalInterface
public interface HandshakeFunction<T> {
  @Nullable
  Actor handshake(@NotNull Session<T> session, @NotNull T handshake);
}

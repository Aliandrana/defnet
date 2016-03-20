package network;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;

/**
 * @author k.usachev
 */
@ThreadSafe
public interface NewSessionHandler<T> {
  @NotNull
  SessionHandler<T> createHandler(@NotNull Session<T> session);
}

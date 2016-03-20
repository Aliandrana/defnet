package network;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;

/**
 * @author k.usachev
 */
@ThreadSafe
public interface SessionHandler<T> {
  void onMessage(@NotNull T message);

  void onClosed();
}

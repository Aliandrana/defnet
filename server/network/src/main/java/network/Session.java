package network;

import org.jetbrains.annotations.NotNull;

/**
 * @author k.usachev
 */
public interface Session<T> {
  void sendMessage(@NotNull T message);

  void close();
}

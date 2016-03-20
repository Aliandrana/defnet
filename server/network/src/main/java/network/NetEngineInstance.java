package network;

import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

/**
 * @author k.usachev
 */
public interface NetEngineInstance<T> extends AutoCloseable {
  @NotNull
  InetSocketAddress getAddress();

  void setHandler(@NotNull NewSessionHandler<T> handler);
}

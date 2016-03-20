package network.netty;

import network.NetEngine;
import network.NetEngineInstance;
import network.Serializer;
import org.jetbrains.annotations.NotNull;

/**
 * @author k.usachev
 */
public enum NettyEngine implements NetEngine<String> {
  Instance;

  @Override
  @NotNull
  public <T> NetEngineInstance<T> launch(@NotNull String host, int port, @NotNull Serializer<T, String> serializer) {
    return Server.launch(host, port, serializer);
  }
}

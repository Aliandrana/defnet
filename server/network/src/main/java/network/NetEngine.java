package network;

import org.jetbrains.annotations.NotNull;

/**
 * @author k.usachev
 */
public interface NetEngine<K> {
  @NotNull
  <T> NetEngineInstance<T> launch(@NotNull String host, int port, @NotNull Serializer<T, K> serializer) throws InterruptedException;
}

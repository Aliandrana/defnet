package network.test;

import network.NetEngine;
import network.Serializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link NetEngine} implementation to use inside tests.
 *
 * @author k.usachev
 */
public enum TestEngine implements NetEngine<String> {
  Instance;

  @NotNull
  public <T> TestServer<T> launch() {
    try {
      return launch("localhost", 0, null);
    } catch (InterruptedException e) {
      throw new AssertionError(e);
    }
  }

  @NotNull
  @Override
  public <T> TestServer<T> launch(@NotNull String host, int port, @Nullable Serializer<T, String> serializer) throws InterruptedException {
    return new TestServer<>(host, port);
  }
}

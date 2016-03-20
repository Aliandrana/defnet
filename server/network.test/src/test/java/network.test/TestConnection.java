package network.test;

import network.SessionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author k.usachev
 */
public final class TestConnection<T> {
  @NotNull
  private final TestSession<T> session;
  @NotNull
  private final SessionHandler<T> handler;

  public TestConnection(@NotNull TestSession<T> session, @NotNull SessionHandler<T> handler) {
    this.session = session;
    this.handler = handler;
  }

  public void send(@NotNull T message) {
    handler.onMessage(message);
  }

  @Nullable
  public T receive() {
    return session.getSentMessages().poll();
  }
}

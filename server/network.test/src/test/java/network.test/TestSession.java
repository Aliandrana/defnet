package network.test;

import network.Session;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author k.usachev
 */
public final class TestSession<T> implements Session<T> {
  private boolean closed;
  @NotNull
  private final Logger log = LoggerFactory.getLogger(TestSession.class);

  @NotNull
  private final Queue<T> sentMessages = new LinkedList<>();

  @Override
  public void sendMessage(@NotNull T message) {
    if (closed) {
      log.error("Trying to send message {} to closed session", message);
      return;
    }
    sentMessages.add(message);
  }

  @NotNull
  public Queue<T> getSentMessages() {
    return sentMessages;
  }

  @Override
  public void close() {
    closed = true;
  }
}

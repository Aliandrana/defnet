package network.test;

import network.NetEngineInstance;
import network.NewSessionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author k.usachev
 */
public class TestServer<T> implements NetEngineInstance<T> {

  @NotNull
  private static final AtomicInteger SessionIdx = new AtomicInteger(0);

  @NotNull
  private final InetSocketAddress address;

  public TestServer(@NotNull String host, int port) {
    this.address = InetSocketAddress.createUnresolved(host, port == 0 ? SessionIdx.incrementAndGet() : port);
  }

  @Nullable
  private NewSessionHandler<T> handler;

  @NotNull
  public TestConnection<T> connect() {
    Assert.assertNotNull(handler);
    final TestSession<T> session = new TestSession<>();
    return new TestConnection<>(session, handler.createHandler(session));
  }

  @Override
  public void close() throws Exception {

  }

  @NotNull
  @Override
  public InetSocketAddress getAddress() {
    return address;
  }

  @Override
  public void setHandler(@NotNull NewSessionHandler<T> handler) {
    this.handler = handler;
  }
}

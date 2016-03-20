package network.netty;

import com.google.gson.JsonObject;
import network.NetEngineInstance;
import network.NewSessionHandler;
import network.Session;
import network.SessionHandler;
import network.serializers.Json2StringSerializer;
import network.serializers.Object2StringSerializer;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * @author k.usachev
 */
public class NettyServerTests {

  @SuppressWarnings("EmptyTryBlock")
  @Test(timeOut = 5000)
  public void testStartAndShutdown() throws Exception {
    try (NetEngineInstance<JsonObject> ignored = NettyEngine.Instance.launch(InetAddress.getLoopbackAddress().getCanonicalHostName(), 0, Json2StringSerializer.Instance)) {
      //do nothing
    }
  }

  @Test(timeOut = 500000)
  public void testMessages() throws Exception {
    try (NetEngineInstance<Serializable> server = NettyEngine.Instance.launch(InetAddress.getLoopbackAddress().getCanonicalHostName(), 0, Object2StringSerializer.Instance);
         ServerClient<Serializable> client = ServerClient.connect(server.getAddress(), Object2StringSerializer.Instance)) {
      server.setHandler(new EchoSessionCreator());
      final String msg = "Some string";
      client.send(msg);
      Assert.assertEquals(client.receive(), msg);
    }
  }

  private static final class EchoSessionCreator implements NewSessionHandler<Serializable> {
    @Override
    public
    @NotNull
    SessionHandler<Serializable> createHandler(@NotNull Session<Serializable> session) {
      return new EchoSessionHandler(session);
    }
  }

  private static final class EchoSessionHandler implements SessionHandler<Serializable> {
    @NotNull
    private final Session<Serializable> session;

    public EchoSessionHandler(@NotNull Session<Serializable> session) {
      this.session = session;
    }

    @Override
    public void onMessage(@NotNull Serializable message) {
      session.sendMessage(message);
    }

    @Override
    public void onClosed() {

    }
  }
}

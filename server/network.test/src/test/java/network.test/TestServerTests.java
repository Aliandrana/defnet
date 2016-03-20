package network.test;

import com.google.gson.JsonObject;
import network.NewSessionHandler;
import network.Session;
import network.SessionHandler;
import network.serializers.Json2StringSerializer;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.JsonUtils;

import java.net.InetAddress;

/**
 * Tests for {@link TestServer}
 *
 * @author k.usachev
 */
public class TestServerTests {

  @Test
  public void testSendMessage() throws Exception {
    try (TestServer<JsonObject> server = TestEngine.Instance.launch(InetAddress.getLoopbackAddress().getCanonicalHostName(), 0, Json2StringSerializer.Instance)) {
      server.setHandler(new EchoSessionCreator());
      final TestConnection<JsonObject> c = server.connect();
      Assert.assertNull(c.receive());
      final JsonObject obj = JsonUtils.createFrom("key", "value");
      c.send(obj);
      Assert.assertEquals(c.receive(), obj);
    }
  }

  private static final class EchoSessionCreator implements NewSessionHandler<JsonObject> {
    @Override
    public
    @NotNull
    SessionHandler<JsonObject> createHandler(@NotNull Session<JsonObject> session) {
      return new EchoSessionHandler(session);
    }
  }

  private static final class EchoSessionHandler implements SessionHandler<JsonObject> {
    @NotNull
    private final Session<JsonObject> session;

    public EchoSessionHandler(@NotNull Session<JsonObject> session) {
      this.session = session;
    }

    @Override
    public void onMessage(@NotNull JsonObject message) {
      session.sendMessage(message);
    }

    @Override
    public void onClosed() {

    }
  }
}

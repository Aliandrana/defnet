package frontend;

import com.google.gson.JsonObject;
import framework.actors.Actor;
import framework.actors.World;
import framework.actors.mailbox.TestMailbox;
import network.test.TestConnection;
import network.test.TestEngine;
import network.test.TestServer;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.JsonUtils;

/**
 * Tests for {@link Frontend}
 *
 * @author k.usachev
 */
public final class FrontendTests {
  @Test
  public void testConnectAndDeliverCommands() {
    final TestMailbox mailbox = new TestMailbox();
    final World world = new World(mailbox);
    final TestServer<JsonObject> testServer = TestEngine.Instance.launch();
    final NewCommand[] commands = new NewCommand[1];
    Frontend.launch(world, testServer, (session, handshake) -> {
      Assert.assertEquals(JsonUtils.getString(handshake, "key"), "value");
      final Actor client = Client.create(session);
      client.addHandler(NewCommand.class, (actor, msg) -> commands[0] = msg);
      return client;
    });
    final TestConnection<JsonObject> c = testServer.connect();

    c.send(JsonUtils.createFrom("key", "value"));
    mailbox.deliverAllMessages();
    Assert.assertNull(commands[0]);
    final JsonObject obj = JsonUtils.createFrom("otherKey", "otherValue");
    c.send(obj);
    mailbox.deliverAllMessages();
    Assert.assertNotNull(commands[0]);
    Assert.assertEquals(commands[0].getMessage(), obj);
  }
}

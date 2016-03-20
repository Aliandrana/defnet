package framework.actors;

import framework.actors.mailbox.TestMailbox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Objects;

/**
 * @author k.usachev
 */
public class ActorTests {
  @Nullable
  private World world;
  @NotNull
  private TestMailbox mailbox = new TestMailbox();

  @BeforeMethod
  public void setup() {
    mailbox.clear();
    world = new World(mailbox);
  }

  @Test
  public void testWatchAlive() {
    final Actor actor1 = new Actor(UniqueRef.create());
    final Actor actor2 = new Actor(UniqueRef.create());
    Objects.requireNonNull(world).register(actor1, actor2);
    mailbox.deliverAllMessages();
    boolean[] received = {false};
    actor1.watch(actor2.getRef(), actorRef -> received[0] = true);
    mailbox.deliverAllMessages();
    Assert.assertFalse(received[0]);
    actor1.delete(actor2.getRef());
    mailbox.deliverAllMessages();
    Assert.assertTrue(received[0]);
  }

  @Test
  public void testWatchDead() {
    final Actor actor = new Actor(UniqueRef.create());
    Objects.requireNonNull(world).register(actor);
    mailbox.deliverAllMessages();
    boolean[] received = {false};
    actor.watch(UniqueRef.create(), actorRef -> received[0] = true);
    mailbox.deliverAllMessages();
    Assert.assertTrue(received[0]);
  }
}

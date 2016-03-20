package framework.actors.mailbox;

import framework.actors.Actor;
import framework.actors.ActorRef;
import framework.actors.World;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

/**
 * @author k.usachev
 */
public class TestMailboxTests extends MailboxTester {
  @Test
  public void testSendMessages() throws Exception {
    final TestMailbox mailbox = new TestMailbox();
    final World world = new World(mailbox);
    final int chainLength = 100;
    final CountDownLatch initialized = new CountDownLatch(chainLength + 1);
    final CountDownLatch deleted = new CountDownLatch(chainLength);
    final CountDownLatch finished = new CountDownLatch(1);
    final ActorRef[] chain = IntStream.range(0, chainLength).mapToObj(i -> new StringRef("Actor" + i)).toArray(ActorRef[]::new);
    final Actor[] chainActors = Arrays.stream(chain).map(actorRef -> ChainActorComponent.attach(new Actor(actorRef), initialized, deleted, chain)).toArray(Actor[]::new);
    final Actor finalActor = FinalActorComponent.attach(new Actor(new StringRef("Final")), 100, initialized, finished, chain);
    Arrays.stream(chainActors).forEach(world::register);
    world.register(finalActor);
    mailbox.deliverAllMessages();
    Assert.assertEquals(initialized.getCount(), 0);
    Assert.assertEquals(finished.getCount(), 0);
    Assert.assertEquals(deleted.getCount(), 0);
  }
}

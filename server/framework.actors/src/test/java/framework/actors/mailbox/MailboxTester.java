package framework.actors.mailbox;

import framework.actors.*;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

/**
 * @author k.usachev
 */
public abstract class MailboxTester {

  protected static final class TtlMessage extends Message {
    private final int ttl;

    public TtlMessage(int ttl) {
      this.ttl = ttl;
    }
  }

  protected static final class ChainActorComponent implements ActorComponent, ActorLifecycleObserver, MessagesHandler<TtlMessage> {
    @NotNull
    private final CountDownLatch initialized;
    @NotNull
    private final CountDownLatch deleted;
    @NotNull
    private final List<ActorRef> allActors;

    @NotNull
    public static Actor attach(@NotNull Actor actor, @NotNull CountDownLatch initialized, @NotNull CountDownLatch deleted, @NotNull ActorRef... allActors) {
      final ChainActorComponent component = new ChainActorComponent(initialized, deleted, allActors);
      actor.addComponent(ChainActorComponent.class, component);
      actor.addHandler(TtlMessage.class, component);
      actor.addLifecycleObserver(component);
      return actor;
    }

    private ChainActorComponent(@NotNull CountDownLatch initialized, @NotNull CountDownLatch deleted, @NotNull ActorRef... allActors) {
      this.initialized = initialized;
      this.deleted = deleted;
      this.allActors = Arrays.asList(allActors);
    }

    @Override
    public void onCreated(@NotNull Actor holder) {
      initialized.countDown();
    }

    @Override
    public void onDeleted(@NotNull Actor holder) {
      deleted.countDown();
    }

    @Override
    public void handle(@NotNull Actor actor, @NotNull TtlMessage msg) {
      if (msg.ttl == 0) {
        actor.send(new StringRef("Final"), new TtlMessage(0));
      } else {
        final ActorRef nextActor = allActors.get((allActors.indexOf(actor.getRef()) + 1) % allActors.size());
        actor.send(nextActor, new TtlMessage(msg.ttl - 1));
      }
    }
  }

  protected static final class FinalActorComponent implements ActorComponent, ActorLifecycleObserver, MessagesHandler<TtlMessage> {
    @NotNull
    private final CountDownLatch initialized;
    @NotNull
    private final CountDownLatch finished;
    private final int count;
    @NotNull
    private final ActorRef[] allActors;
    private int handled = 0;

    @NotNull
    public static Actor attach(@NotNull Actor actor, int count, @NotNull CountDownLatch initialized, @NotNull CountDownLatch deleted, @NotNull ActorRef... allActors) {
      final FinalActorComponent component = new FinalActorComponent(count, initialized, deleted, allActors);
      actor.addComponent(FinalActorComponent.class, component);
      actor.addHandler(TtlMessage.class, component);
      actor.addLifecycleObserver(component);
      return actor;
    }

    private FinalActorComponent(int count, @NotNull CountDownLatch initialized, @NotNull CountDownLatch finished, @NotNull ActorRef... allActors) {
      this.count = count;
      this.initialized = initialized;
      this.finished = finished;
      this.allActors = allActors;
    }

    @Override
    public void onCreated(@NotNull Actor holder) {
      initialized.countDown();
      Stream.of(allActors).forEach(actorRef -> holder.send(actorRef, new TtlMessage(count)));
    }

    @Override
    public void onDeleted(@NotNull Actor holder) {

    }

    @Override
    public void handle(@NotNull Actor actor, @NotNull TtlMessage msg) {
      Assert.assertEquals(msg.ttl, 0);
      if (++handled < allActors.length)
        return;
      Arrays.stream(allActors).forEach(actor::delete);
      finished.countDown();
    }
  }

  protected static final class StringRef extends ActorRef {
    @NotNull
    private final String ref;

    public StringRef(@NotNull String ref) {
      this.ref = ref;
    }

    @Override
    public int getHashCode() {
      return ref.hashCode();
    }

    @Override
    protected boolean equals(@NotNull ActorRef other) {
      return this == other || other instanceof StringRef && ref.equals(((StringRef) other).ref);
    }
  }
}

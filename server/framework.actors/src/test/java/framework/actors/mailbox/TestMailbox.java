package framework.actors.mailbox;

import framework.actors.*;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Mailbox for tests. Can tick messages.
 *
 * @author k.usachev
 */
public final class TestMailbox implements Mailbox {
  @NotNull
  private final Queue<RefAndTask> tasks = new LinkedList<>();

  @Override
  public void send(@NotNull ActorsHolder holder, @NotNull ActorRef ref, @NotNull ActorTask task) {
    tasks.add(new RefAndTask(holder, ref, task));
  }

  public void deliverAllMessages() {
    int protector = 50000;
    while (--protector > 0 && !tasks.isEmpty())
      tasks.poll().deliver();
    Assert.assertEquals(tasks.size(), 0);
  }

  public void clear() {
    tasks.clear();
  }

  private final class RefAndTask {
    @NotNull
    private final ActorsHolder holder;
    @NotNull
    private final ActorRef ref;
    @NotNull
    private final ActorTask task;

    public RefAndTask(@NotNull ActorsHolder holder, @NotNull ActorRef ref, @NotNull ActorTask task) {
      this.holder = holder;
      this.ref = ref;
      this.task = task;
    }

    public void deliver() {
      final Actor actor = holder.find(ref);
      if (actor != null)
        task.execute(actor);
      else
        task.onFail(ref);
    }
  }
}

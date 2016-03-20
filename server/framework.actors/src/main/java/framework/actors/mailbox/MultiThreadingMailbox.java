package framework.actors.mailbox;

import framework.actors.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple job stealing multithreaded mailbox implementation.
 *
 * @author k.usachev
 */
public final class MultiThreadingMailbox implements Mailbox, AutoCloseable {
  @NotNull
  private static final Logger log = LoggerFactory.getLogger(MultiThreadingMailbox.class);
  @NotNull
  private final ExecutorService executor;
  @NotNull
  private final Map<ActorRef, Queue<ActorTask>> tasks = new ConcurrentHashMap<>();
  @NotNull
  private final ReentrantLock emptyQueueLock = new ReentrantLock();

  private MultiThreadingMailbox(@NotNull ExecutorService executor) {
    this.executor = executor;
  }

  @NotNull
  public static MultiThreadingMailbox create(int threads) {
    return new MultiThreadingMailbox(Executors.newFixedThreadPool(threads));
  }

  @SuppressWarnings("unchecked")
  @Override
  public void send(@NotNull ActorsHolder holder, @NotNull ActorRef ref, @NotNull ActorTask task) {
    final ConcurrentLinkedQueue<ActorTask>[] newQueue = new ConcurrentLinkedQueue[1];
    emptyQueueLock.lock();
    try {
      tasks.computeIfAbsent(ref, actorRef -> {
        newQueue[0] = new ConcurrentLinkedQueue<>();
        return newQueue[0];
      }).add(task);
    } finally {
      emptyQueueLock.unlock();
    }
    if (newQueue[0] != null)
      executor.submit(new Executor(holder, ref, newQueue[0]));
  }

  @Override
  public void close() throws Exception {
    executor.shutdownNow();
  }

  private class Executor implements Runnable {
    @NotNull
    private final ActorsHolder holder;
    @NotNull
    private final ActorRef ref;
    @NotNull
    private final Queue<ActorTask> queue;

    public Executor(@NotNull ActorsHolder holder, @NotNull ActorRef ref, @NotNull Queue<ActorTask> queue) {
      this.holder = holder;
      this.ref = ref;
      this.queue = queue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
      try {
        final Actor actor = holder.find(ref);
        if (actor == null) {
          final Queue<ActorTask> dropped = tasks.remove(ref);
          dropped.forEach(actorTask -> actorTask.onFail(ref));
          return;
        }
        ActorTask task = queue.poll();
        if (task == null) {
          emptyQueueLock.lock();
          try {
            task = queue.poll();
            if (task == null) {
              tasks.remove(ref);
              return;
            }
          } finally {
            emptyQueueLock.unlock();
          }
        }
        task.execute(actor);
        executor.execute(this);

      } catch (Exception e) {
        log.error("During execution", e);
      }
    }
  }
}

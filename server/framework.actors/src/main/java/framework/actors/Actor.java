package framework.actors;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Main execution unit.
 *
 * @author k.usachev
 */
public final class Actor {
  @NotNull
  private final Logger log = LoggerFactory.getLogger(Actor.class);

  @NotNull
  private final ActorRef ref;
  @NotNull
  private final CopyOnWriteArrayList<ActorLifecycleObserver> lifecycleObservers = new CopyOnWriteArrayList<>();
  @NotNull
  private State state = State.Clear;
  @Nullable
  private ActorHolder post;

  @NotNull
  private final Multimap<Class<? extends Message>, MessagesHandler> handlers = LinkedListMultimap.create();

  public Actor(@NotNull ActorRef ref) {
    this.ref = ref;
  }

  @NotNull
  public ActorRef getRef() {
    return ref;
  }

  public void addLifecycleObserver(@NotNull ActorLifecycleObserver observer) {
    lifecycleObservers.add(observer);
    state.onObserverAdded(this, observer);
  }

  public void removeLifecycleObserver(@NotNull ActorLifecycleObserver observer) {
    if (!lifecycleObservers.remove(observer)) {
      log.warn("Can't find observer {} at {}", observer, this);
      return;
    }
    state.onObserverRemoved(this, observer);
  }

  @NotNull
  private final Map<Class<?>, ActorComponent> components = new HashMap<>();

  @SuppressWarnings("unchecked")
  @Nullable
  public <T extends ActorComponent> T tryGet(@NotNull Class<T> component) {
    return (T) components.get(component);
  }

  @SuppressWarnings("unchecked")
  @NotNull
  public <T extends ActorComponent> T get(@NotNull Class<T> component) {
    final T result = (T) components.get(component);
    if (result == null)
      throw new IllegalArgumentException("No " + component + " at " + this);
    return result;
  }

  @SuppressWarnings("unchecked")
  @Nullable
  public <T extends ActorComponent, K extends T> T addComponent(@NotNull Class<T> type, @NotNull K component) {
    return (T) components.put(type, component);
  }

  public <T extends ActorComponent, K extends T> void addComponentUnique(@NotNull Class<T> type, @NotNull K component) {
    final T oldComponent = addComponent(type, component);
    if (oldComponent != null)
      throw new IllegalStateException("Already have component " + oldComponent + " at " + this);
  }

  void onCreated(@NotNull ActorHolder holder) {
    state = State.Created;
    this.post = holder;
    lifecycleObservers.forEach(o -> o.onCreated(this));
  }

  void onDeleted() {
    state = State.Deleted;
    lifecycleObservers.forEach(o -> o.onDeleted(this));
  }

  public <T extends Message> void addHandler(@NotNull Class<? extends T> msgType, @NotNull MessagesHandler<T> handler) {
    handlers.put(msgType, handler);
  }

  void handle(@NotNull Message msg) {
    if (state == State.Deleted) {
      log.warn("Shouldn't handle any message {} at {}", msg, this);
      return;
    }
    // todo: optimize it
    final Collection<MessagesHandler> typeHandlers = handlers.entries().stream()
        .filter(entry -> entry.getKey().isAssignableFrom(msg.getClass()))
        .map(Map.Entry::getValue).collect(Collectors.toList());
    if (typeHandlers.isEmpty())
      log.warn("Nobody can handle msg {} at {}", msg, this);
    else
      //noinspection unchecked
      typeHandlers.forEach(handler -> handler.handle(this, msg));
  }

  public void send(@NotNull ActorRef addressee, @NotNull Message message) {
    Objects.requireNonNull(post).send(addressee, message);
  }

  public void delete(@NotNull ActorRef actor) {
    Objects.requireNonNull(post).delete(actor);
  }

  public void watch(@NotNull ActorRef target, @NotNull Consumer<ActorRef> action) {
    Objects.requireNonNull(post).watch(getRef(), target, action);
  }

  public void register(@NotNull Actor... actors) {
    Objects.requireNonNull(post).register(actors);
  }

  public <T extends Message> void removeHandler(@NotNull Class<? extends T> msgType, @NotNull MessagesHandler<T> handler) {
    if (!handlers.remove(msgType, handler))
      log.warn("Couldn't find handler {} for {} at {}", handler, msgType, this);
  }

  private enum State {
    Clear {
      @Override
      public void onObserverAdded(@NotNull Actor actor, @NotNull ActorLifecycleObserver observer) {
        // do nothing
      }

      @Override
      public void onObserverRemoved(@NotNull Actor actor, @NotNull ActorLifecycleObserver observer) {
        // do nothing
      }
    },
    Created {
      @Override
      public void onObserverAdded(@NotNull Actor actor, @NotNull ActorLifecycleObserver observer) {
        observer.onCreated(actor);
      }

      @Override
      public void onObserverRemoved(@NotNull Actor actor, @NotNull ActorLifecycleObserver observer) {
        observer.onDeleted(actor); // not sure about this
      }
    },
    Deleted {
      @Override
      public void onObserverAdded(@NotNull Actor actor, @NotNull ActorLifecycleObserver observer) {
        observer.onCreated(actor);
        observer.onDeleted(actor);
      }

      @Override
      public void onObserverRemoved(@NotNull Actor actor, @NotNull ActorLifecycleObserver observer) {
        // do nothing
      }
    };

    public abstract void onObserverAdded(@NotNull Actor actor, @NotNull ActorLifecycleObserver observer);

    public abstract void onObserverRemoved(@NotNull Actor actor, @NotNull ActorLifecycleObserver observer);
  }

  @Override
  public String toString() {
    return "Actor{" +
        ref +
        "-" + state +
        " " + components.values() +
        " from " + post +
        '}';
  }
}

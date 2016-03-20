package framework.actors;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

/**
 * ThreadSafe class->object storage for system services.
 *
 * @author k.usachev
 */
public class Context {
  @NotNull
  private final Map<Class<?>, ?> services;

  @NotNull
  public static Context create(@NotNull Map<Class<?>, ?> services) {
    return new Context(services);
  }

  private Context(@NotNull Map<Class<?>, ?> services) {
    this.services = Collections.unmodifiableMap(services);
  }

  @SuppressWarnings("unchecked")
  @NotNull
  public <T> T get(@NotNull Class<T> type) {
    final T result = (T) services.get(type);
    if (result == null)
      throw new AssertionError("Can't obtain " + type);
    return result;
  }
}

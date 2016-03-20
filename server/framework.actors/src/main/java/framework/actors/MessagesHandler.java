package framework.actors;

import org.jetbrains.annotations.NotNull;

/**
 * @author k.usachev
 */
public interface MessagesHandler<T extends Message> {
  void handle(@NotNull Actor actor, @NotNull T msg);
}

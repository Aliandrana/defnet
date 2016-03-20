package frontend;

import com.google.gson.JsonElement;
import framework.actors.ActorRef;
import framework.actors.Message;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * @author k.usachev
 */
public final class Command extends Message {
  @NotNull
  private final ActorRef from;
  @NotNull
  private final String name;
  @Nullable
  private final JsonElement params;

  public Command(@NotNull ActorRef from, @NotNull String name, JsonElement params) {
    this.from = from;
    this.name = name;
    this.params = params;
  }

  @NotNull
  public ActorRef getFrom() {
    return from;
  }

  @NotNull
  public String getName() {
    return name;
  }

  @Nullable
  public JsonElement getParams() {
    return params;
  }
}

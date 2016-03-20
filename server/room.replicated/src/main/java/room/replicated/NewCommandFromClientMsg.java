package room.replicated;

import com.google.gson.JsonObject;
import framework.actors.ActorRef;
import framework.actors.Message;
import org.jetbrains.annotations.NotNull;

/**
 * @author k.usachev
 */
class NewCommandFromClientMsg extends Message{
  @NotNull
  private final ActorRef client;
  @NotNull
  private final JsonObject command;

  public NewCommandFromClientMsg(@NotNull ActorRef client, @NotNull JsonObject command) {
    this.client = client;
    this.command = command;
  }

  @NotNull
  public ActorRef getClient() {
    return client;
  }

  @NotNull
  public JsonObject getCommand() {
    return command;
  }
}

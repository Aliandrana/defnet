package room.replicated;

import framework.actors.ActorRef;
import framework.actors.Message;
import org.jetbrains.annotations.NotNull;

/**
 * @author k.usachev
 */
final class NewClientConnectedMsg extends Message {
  @NotNull
  private final ActorRef client;

  public NewClientConnectedMsg(@NotNull ActorRef client) {
    this.client = client;
  }

  @NotNull
  public ActorRef getClient() {
    return client;
  }
}

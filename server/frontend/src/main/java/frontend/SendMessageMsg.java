package frontend;

import com.google.gson.JsonObject;
import framework.actors.Message;
import org.jetbrains.annotations.NotNull;

/**
 * @author k.usachev
 */
public final class SendMessageMsg extends Message {
  @NotNull
  private final JsonObject msg;

  public SendMessageMsg(@NotNull JsonObject msg) {
    this.msg = msg;
  }

  @NotNull
  public JsonObject getMsg() {
    return msg;
  }
}

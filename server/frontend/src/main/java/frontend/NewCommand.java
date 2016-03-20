package frontend;

import com.google.gson.JsonObject;
import framework.actors.Message;
import org.jetbrains.annotations.NotNull;

/**
 * @author k.usachev
 */
public class NewCommand extends Message {

  @NotNull
  private final JsonObject message;

  @NotNull
  public static NewCommand create(@NotNull JsonObject msg) {
    return new NewCommand(msg);
  }

  public NewCommand(@NotNull JsonObject message) {
    this.message = message;
  }

  @NotNull
  public JsonObject getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return "NewMessageMsg{" +
        "message=" + message +
        "}";
  }
}

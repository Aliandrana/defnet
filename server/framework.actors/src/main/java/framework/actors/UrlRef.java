package framework.actors;

import org.jetbrains.annotations.NotNull;

/**
 * @author k.usachev
 */
public class UrlRef extends ActorRef {
  @NotNull
  private static final UrlRef EmptyRef = new UrlRef("", "") {
    @Override
    public String toString() {
      return "EmptyUrlRef";
    }
  };
  @NotNull
  private final String socket;
  @NotNull
  private final String path;

  public static UrlRef createSystem(@NotNull String system) {
    return new UrlRef("@" + system, "");
  }

  public static UrlRef createAbsolute(@NotNull String socket, @NotNull String path) {
    return new UrlRef(socket, path);
  }

  @NotNull
  public static UrlRef parse(@NotNull String url) {
    final int socketIdx = url.indexOf(':');
    if (socketIdx < 0)
      return EmptyRef;
    final String socket = url.substring(0, socketIdx + 1);
    int fragmentIdx = url.indexOf('#');
    fragmentIdx = fragmentIdx >= 0 ? fragmentIdx : url.length();
    final String path = url.substring(socketIdx + 1, fragmentIdx - 1);
    return createAbsolute(socket, path);
  }

  private UrlRef(@NotNull String socket, @NotNull String path) {
    this.socket = socket;
    this.path = path;
  }

  @Override
  protected boolean equals(@NotNull ActorRef other) {
    if (!(other instanceof UrlRef))
      return false;
    final UrlRef o = (UrlRef) other;
    return socket.equals(o.socket) && path.equals(o.path);
  }

  @Override
  protected int getHashCode() {
    int result = socket.hashCode();
    result = 31 * result + path.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return socket + ':' + path;
  }
}

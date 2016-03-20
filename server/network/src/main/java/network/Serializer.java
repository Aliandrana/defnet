package network;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author k.usachev
 */
public interface Serializer<T, K> {

  @NotNull
  Class<T> getBaseClass();

  @NotNull
  K serialize(@NotNull T msg) throws IOException;

  @NotNull
  T deserialize(@NotNull K msg) throws IOException;
}

package network.serializers;

import network.Serializer;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Base64;

/**
 * Simple java serializer. For test use only.
 *
 * @author k.usachev
 */
public enum Object2StringSerializer implements Serializer<Serializable, String> {
  Instance;

  @NotNull
  @Override
  public Class<Serializable> getBaseClass() {
    return Serializable.class;
  }

  @Override
  @NotNull
  public String serialize(@NotNull Serializable msg) throws IOException {
    final ByteArrayOutputStream bytes = new ByteArrayOutputStream(1024);
    final ObjectOutputStream stream = new ObjectOutputStream(bytes);
    stream.writeObject(msg);
    stream.flush();
    return Base64.getEncoder().encodeToString(bytes.toByteArray());
  }

  @Override
  @NotNull
  public Serializable deserialize(@NotNull String msg) throws IOException {
    final byte[] bytes = Base64.getDecoder().decode(msg);
    final ByteArrayInputStream bytesStream = new ByteArrayInputStream(bytes, 0, bytes.length);
    final ObjectInputStream stream = new ObjectInputStream(bytesStream);
    try {
      return (Serializable) stream.readObject();
    } catch (ClassNotFoundException e) {
      throw new IOException(e);
    }
  }
}

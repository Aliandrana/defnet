package utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * @author k.usachev
 */
public enum RandomUtils {
  ;
  @NotNull
  private static final Random random = new Random(System.currentTimeMillis());

  @Nullable
  public static <T> T pick(@NotNull T... elements) {
    return elements[random.nextInt(elements.length)];
  }
}

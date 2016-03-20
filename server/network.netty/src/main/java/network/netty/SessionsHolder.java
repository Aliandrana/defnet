package network.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import network.NewSessionHandler;
import network.Session;
import network.SessionHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author k.usachev
 */
public class SessionsHolder<T> extends SimpleChannelInboundHandler<T> {

  @NotNull
  private static final Logger log = LoggerFactory.getLogger(SessionsHolder.class);
  @NotNull
  private final Supplier<NewSessionHandler<T>> handlerProvider;
  @NotNull
  private final Map<SocketAddress, SessionHandler<T>> sessions = new ConcurrentHashMap<>();

  public SessionsHolder(@NotNull Supplier<NewSessionHandler<T>> handlerProvider, @NotNull Class<T> messageType) {
    super(messageType);
    this.handlerProvider = handlerProvider;
  }

  @Override
  public void channelReadComplete(@NotNull ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  public void exceptionCaught(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause) {
    log.warn("exceptionCaught", cause);
    ctx.close();
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void channelRead0(@NotNull ChannelHandlerContext ctx, @NotNull T message) throws Exception {
    final SessionHandler<T> session = sessions.get(ctx.channel().remoteAddress());
    if (session == null) {
      log.error("No session for {}", ctx.channel().remoteAddress());
      return;
    }
    log.debug(ctx.channel().remoteAddress() + " => " + message);
    session.onMessage(message);
  }

  @Override
  public void channelRegistered(@NotNull ChannelHandlerContext ctx) throws Exception {
    super.channelRegistered(ctx);
    final NewSessionHandler<T> handler = handlerProvider.get();
    if (handler != null)
      sessions.put(ctx.channel().remoteAddress(), handler.createHandler(new NettySession(ctx)));
    else
      ctx.disconnect();
  }

  @Override
  public void channelUnregistered(@NotNull ChannelHandlerContext ctx) throws Exception {
    super.channelUnregistered(ctx);
    final SessionHandler handler = sessions.remove(ctx.channel().remoteAddress());
    if (handler != null)
      handler.onClosed();
  }

  private class NettySession implements Session<T> {

    @NotNull
    private final ChannelHandlerContext ctx;

    public NettySession(@NotNull ChannelHandlerContext ctx) {
      this.ctx = ctx;
    }

    @Override
    public void sendMessage(@NotNull T message) {
      log.debug(ctx.channel().remoteAddress() + " <= " + message);
      ctx.writeAndFlush(message);
    }

    @Override
    public void close() {
      ctx.close();
    }

    @Override
    public String toString() {
      return "NettySession{" + ctx.channel().remoteAddress() + '}';
    }
  }
}

package network.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import network.Serializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

/**
 * @author k.usachev
 */
public class ServerClient<T> implements AutoCloseable {
  @NotNull
  private static final Logger log = LoggerFactory.getLogger(ServerClient.class);
  @NotNull
  private final ClientMessagesHandler<T> handler;
  @NotNull
  private final Channel channel;

  private ServerClient(@NotNull ClientMessagesHandler<T> handler, @NotNull Channel channel) {
    this.handler = handler;
    this.channel = channel;
  }

  @NotNull
  public static <T> ServerClient<T> connect(@NotNull InetSocketAddress address, @NotNull Serializer<T, String> serializer) throws InterruptedException, ExecutionException {
    final EventLoopGroup group = new NioEventLoopGroup();

    final Bootstrap b = new Bootstrap();
    final ClientMessagesHandler<T> handler = new ClientMessagesHandler<>(serializer.getBaseClass());
    b.group(group)
        .channel(NioSocketChannel.class)
        .option(ChannelOption.TCP_NODELAY, true)
        .handler(new ChannelInitializer<SocketChannel>() {
          @Override
          public void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline p = ch.pipeline();
            p.addLast(
                new LineBasedFrameDecoder(512),
                new StringDecoder(CharsetUtil.UTF_8),
                new Server.MessagesDeserializer(serializer),

                new StringEncoder(CharsetUtil.UTF_8),
                new Server.MessagesSerializer<>(serializer),
                handler);
          }
        });

    return new ServerClient<>(handler, b.connect(address.getAddress(), address.getPort()).sync().channel());

  }

  public void send(@NotNull T message) {
    final ChannelHandlerContext ctx = handler.ctx;
    Assert.assertNotNull(ctx);
    ctx.writeAndFlush(message);
  }

  @Nullable
  public T receive() throws InterruptedException {
    T msg = handler.messages.poll();
    while (msg == null) {
      msg = handler.messages.poll();
      Thread.sleep(100);
    }
    return msg;
  }

  @Override
  public void close() throws Exception {
    channel.close().get();
  }

  private static final class ClientMessagesHandler<T> extends SimpleChannelInboundHandler<T> {

    public ClientMessagesHandler(@NotNull Class<? extends T> inboundMessageType) {
      super(inboundMessageType);
    }

    @Nullable
    private ChannelHandlerContext ctx;
    @NotNull
    private final ConcurrentLinkedQueue<T> messages = new ConcurrentLinkedQueue<>();

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
      ctx.flush();
    }

    @Override
    public void exceptionCaught(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause) {
      log.error("exceptionCaught", cause);
      ctx.close();
    }

    @Override
    protected void channelRead0(@NotNull ChannelHandlerContext ctx, @NotNull T msg) throws Exception {
      messages.add(msg);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
      super.channelRegistered(ctx);
      Assert.assertNull(this.ctx);
      this.ctx = ctx;
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
      super.channelUnregistered(ctx);
      Assert.assertNotNull(this.ctx);
      Assert.assertEquals(ctx, this.ctx);
      this.ctx = null;
    }
  }
}

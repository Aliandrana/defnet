package network.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import network.NetEngineInstance;
import network.NewSessionHandler;
import network.Serializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author k.usachev
 */
final class Server<T> implements NetEngineInstance<T> {
  @NotNull
  private static final Logger log = LoggerFactory.getLogger(Server.class);

  @NotNull
  private final Channel channel;
  @NotNull
  private final EventLoopGroup bossGroup;
  @NotNull
  private final EventLoopGroup workerGroup;
  @Nullable
  private volatile NewSessionHandler<T> handler;

  private Server(@NotNull Channel channel, @NotNull EventLoopGroup bossGroup, @NotNull EventLoopGroup workerGroup) {
    this.channel = channel;
    this.bossGroup = bossGroup;
    this.workerGroup = workerGroup;
  }

  @NotNull
  public static <T> Server<T> launch(@NotNull String host, int port, @NotNull Serializer<T, String> serializer) {
    final NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
    final NioEventLoopGroup workerGroup = new NioEventLoopGroup();
    ServerBootstrap b = new ServerBootstrap();
    final Server[] crap = new Server[1];
    b.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(new ChannelInitializer<SocketChannel>() {
          @Override
          public void initChannel(@NotNull SocketChannel ch) throws Exception {
            final ChannelPipeline p = ch.pipeline();
            p.addLast(
                new LineBasedFrameDecoder(512),
                new StringDecoder(CharsetUtil.UTF_8),
                new MessagesDeserializer(serializer),

                new StringEncoder(CharsetUtil.UTF_8),
                new MessagesSerializer<>(serializer),

                new SessionsHolder<>(() -> crap[0] != null ? crap[0].handler : null, serializer.getBaseClass()));
          }
        });

    final Server<T> server;
    try {
      // Bind and start to accept incoming connections.
      server = new Server<>(b.bind(host, port).sync().channel(), bossGroup, workerGroup);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    }
    crap[0] = server;
    log.info("Launched on {}", server.channel.localAddress());
    return server;
  }

  @Override
  public void close() throws Exception {
    if (!channel.isOpen())
      return;
    channel.close();
    bossGroup.shutdownGracefully();
    workerGroup.shutdownGracefully();
  }

  public
  @NotNull
  InetSocketAddress getAddress() {
    return (InetSocketAddress) channel.localAddress();
  }

  @Override
  public void setHandler(@NotNull NewSessionHandler<T> handler) {
    this.handler = handler;
  }

  static final class MessagesSerializer<T> extends MessageToMessageEncoder<T> {
    @NotNull
    private final Serializer<T, String> serializer;

    public MessagesSerializer(@NotNull Serializer<T, String> serializer) {
      super(serializer.getBaseClass());
      this.serializer = serializer;
    }

    @Override
    protected void encode(@NotNull ChannelHandlerContext ctx, @NotNull T msg, @NotNull List<Object> out) throws Exception {
      out.add(serializer.serialize(msg) + '\n');
    }
  }

  static final class MessagesDeserializer extends MessageToMessageDecoder<String> {
    @NotNull
    private final Serializer<?, String> serializer;

    public MessagesDeserializer(@NotNull Serializer<?, String> serializer) {
      super(String.class);
      this.serializer = serializer;
    }

    @Override
    protected void decode(@NotNull ChannelHandlerContext ctx, @NotNull String msg, @NotNull List<Object> out) throws Exception {
      out.add(serializer.deserialize(msg));
    }
  }
}

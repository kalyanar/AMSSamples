package com.adobe.ams.kalyanar.utilities.logtail.impl;

import com.adobe.ams.kalyanar.utilities.logtail.LogEventService;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.ImmediateEventExecutor;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.commons.osgi.PropertiesUtil;

import java.util.Map;

/**
 * Created by kalyanar on 8/29/2015.
 */
@Component(configurationFactory = true)
@Service(NettyServer.class)
public class NettyServer implements LogEventService {

    private ServerBootstrap bootstrap;
  //  private ChannelGroup channelGroup;
    private EventLoopGroup eventLoopGroup;
    private ChannelFuture channelFuture;

    @Property(description = "",intValue =3333)
    private static final String PORT="PORT";




    @Reference
    private LogTailManager logTailManager;

    @Activate
    protected final void activate(final Map<String, String> config) {
     //   channelGroup  = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
        bootstrap = new ServerBootstrap();
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new LogChannelGroupInitializer(logTailManager));
        ChannelFuture channelFuture = bootstrap.bind(PropertiesUtil.toInteger(config.get("PORT"),3333));
        channelFuture.syncUninterruptibly();
    }

    @Deactivate
    protected final void deactivate(final Map<String, String> config) {
        Channel channel = channelFuture.channel();
        if(channel!=null){
            channel.close();
        }
        eventLoopGroup.shutdownGracefully();
        channelFuture.channel().closeFuture().syncUninterruptibly();

    }


}

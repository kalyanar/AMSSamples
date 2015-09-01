package com.adobe.ams.utilities.logtail.impl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.felix.scr.annotations.*;

import java.util.Map;

/**
 * Created by kalyanar on 8/31/2015.
 */
@Component(immediate = true)
@Service(LogBroadcastServer.class)
public class LogBroadcastServer {

    private ServerBootstrap serverBootstrap;
    private EventLoopGroup eventLoopGroup;
    private ChannelFuture channelFuture;

    @Reference
    private LogTailManager logTailManager;

    @Activate
    protected final void activate(final Map<String, String> config) {
        serverBootstrap = new ServerBootstrap();
        eventLoopGroup = new NioEventLoopGroup();
        serverBootstrap.group(eventLoopGroup).channel(NioServerSocketChannel.class).childHandler(new LogBroadcastPipelineInitializer(logTailManager));
         channelFuture = serverBootstrap.bind(3333);
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

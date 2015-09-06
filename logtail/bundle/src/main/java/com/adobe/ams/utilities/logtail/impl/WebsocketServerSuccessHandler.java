package com.adobe.ams.utilities.logtail.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.AttributeKey;

/**
 * Created by kalyanar on 8/31/2015.
 */
public class WebsocketServerSuccessHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private LogTailManager logTailManager;

    WebsocketServerSuccessHandler(LogTailManager logTailManager){
        super();
        this.logTailManager = logTailManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            String logfilepath = (String)ctx.channel().attr(AttributeKey.valueOf("logfilepath")).getAndRemove();
           LogTail logTail = logTailManager.getLogTail(logfilepath);
            logTail.getWebsocketChannelGroup().addChannel(ctx.channel());
            logTail.start();
            ctx.pipeline().remove(HttpRequestHandler.class);
        }
    }
}
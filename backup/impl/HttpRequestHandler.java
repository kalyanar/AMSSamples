package com.adobe.ams.kalyanar.utilities.logtail.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

import java.io.File;

/**
 * Created by kalyanar on 8/29/2015.
 */
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final String wsUri;
    private LogTailManager logTailManager;
    public HttpRequestHandler(String wsUri, LogTailManager logTailManager) {
        this.wsUri = wsUri;
        this.logTailManager = logTailManager;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if(request.getUri().startsWith(wsUri)){
            LogTail logTail = logTailManager.getLogTail(request.getUri().substring(3));
            logTail.add(ctx.channel());
            ctx.fireChannelRead(request.retain());
        }
    }
}

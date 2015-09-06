package com.adobe.ams.utilities.logtail.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;

/**
 * Created by kalyanar on 8/31/2015.
 */
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest>  {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        String uri = fullHttpRequest.getUri();
        if(uri.startsWith("/ws")&&uri.length()>3){

            AttributeKey<String> attr = AttributeKey.valueOf("logfilepath");
            channelHandlerContext.channel().attr(attr).set(uri.substring(3));
            channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
        }
    }
}

package com.adobe.ams.utilities.logtail.impl;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.ImmediateEventExecutor;

/**
 * Created by kalyanar on 8/31/2015.
 */
public class WebsocketWriter implements LogWriter,WebsocketChannelGroup {

    private ChannelGroup channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);

    @Override
    public void write(String line) {
        writeAndFlush(line);
    }

    @Override
    public void writeAndFlush(String line) {
        channelGroup.writeAndFlush(line);
    }

    @Override
    public void addChannel(Channel channel) {
        channelGroup.add(channel);
    }

    @Override
    public void closeChannels() {
        channelGroup.close();
    }
}

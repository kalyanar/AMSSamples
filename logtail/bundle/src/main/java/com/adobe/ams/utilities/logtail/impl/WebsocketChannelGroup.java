package com.adobe.ams.utilities.logtail.impl;

import io.netty.channel.Channel;

/**
 * Created by kalyanar on 8/31/2015.
 */
public interface WebsocketChannelGroup {
    void addChannel(Channel channel);
    void closeChannels();
}

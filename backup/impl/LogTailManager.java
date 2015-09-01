package com.adobe.ams.kalyanar.utilities.logtail.impl;

import io.netty.channel.group.ChannelGroup;

/**
 * Created by kalyanar on 8/30/2015.
 */
public interface LogTailManager {
    LogTail getLogTail(String logFilePath);
}

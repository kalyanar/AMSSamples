package com.adobe.ams.kalyanar.utilities.logtail.impl;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.ImmediateEventExecutor;
import org.apache.felix.scr.annotations.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kalyanar on 8/30/2015.
 */
@Component
public class LogTailManagerImpl implements LogTailManager {
    Map<String,LogTail> tails = new ConcurrentHashMap<String,LogTail>();
    @Override
    public LogTail getLogTail(String logFilePath) {
        if(tails.containsKey(logFilePath)){
            return tails.get(logFilePath);
        }
        ChannelGroup channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
        LogTail tail  = new LogTail(logFilePath,channelGroup);
        tails.put(logFilePath,tail);
        return tail;
    }
}

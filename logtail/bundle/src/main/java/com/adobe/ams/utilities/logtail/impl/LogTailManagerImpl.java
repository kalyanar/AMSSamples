package com.adobe.ams.utilities.logtail.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kalyanar on 8/31/2015.
 */
@Component(immediate = true)
@Service
public class LogTailManagerImpl implements LogTailManager {
    private Map<String,LogTail> tails = new ConcurrentHashMap<String,LogTail>();
    @Override
    public LogTail getLogTail(String logfilepath) {
        if(tails.containsKey(logfilepath)){
            return tails.get(logfilepath);
        }else{
            LogTail logTail = new LogTail(logfilepath,new WebsocketWriter());
            tails.put(logfilepath,logTail);
            return logTail;
        }
    }
}

package com.adobe.ams.kalyanar.stats.userstats.impl;

import com.adobe.ams.kalyanar.stats.userstats.ConcurrentUserStats;
import com.adobe.ams.kalyanar.stats.userstats.ConcurrentUserStatsMBean;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingConstants;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by kalyanar on 8/12/2015.
 */
@Component(immediate = true, name = "User Stats", description = "User Stats" )
@Service
public class ConcurrentUserStatsImpl implements ConcurrentUserStats{
    /** Value */
    private final AtomicLong userStatsEachSecond = new AtomicLong();

    public long getConcurrentUserCount() {
        return userStatsEachSecond.get();
    }
    public void incrementByOne(){
        userStatsEachSecond.incrementAndGet();
    }
    public void decrementByOne(){
        userStatsEachSecond.decrementAndGet();
    }

    public void reset() {
        userStatsEachSecond.set(0);
    }
}

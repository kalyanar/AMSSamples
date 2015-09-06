package com.adobe.ams.kalyanar.stats.userstats.impl;

import com.adobe.ams.kalyanar.stats.userstats.ConcurrentUserStats;
import com.adobe.ams.kalyanar.stats.userstats.ConcurrentUserStatsMBean;
import com.adobe.ams.kalyanar.stats.userstats.ResetUserStats;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by kalyanar on 8/12/2015.
 */
@Component(immediate = true,name = "User Stats MBean register service", description = "User Stats Mbean register service" )
@Service(ConcurrentUserStatsRegisterService.class)
public class ConcurrentUserStatsRegisterService {

    @Reference
    private ConcurrentUserStats concurrentUserStats;

    @Reference
    private ResetUserStats resetUserStats;

    @Activate
    public void activate(ComponentContext componentContext){
        StatsRecorder recorder = new StatsRecorder(concurrentUserStats,createScheduledExecutorService());
        ConcurrentUserStatsMBean concurrentUserMBean = new ConcurrentUserStatsMBeanImpl(recorder,resetUserStats);
        Dictionary<String, Object> serviceProps = new Hashtable<String, Object>();
        serviceProps.put("jmx.objectname", "com.adobe.ams" +
                ".stats:type=ConcurrentUserCount");

        componentContext.getBundleContext().registerService(ConcurrentUserStatsMBean.class
                        .getName(),
                concurrentUserMBean, serviceProps);
    }
    private static ScheduledExecutorService createScheduledExecutorService(){
        ThreadFactory tf = new ThreadFactory() {
            private final AtomicLong counter = new AtomicLong();
            @Override
            public Thread newThread( Runnable r) {
                Thread t = new Thread(r, newName());
                t.setDaemon(true);
                return t;
            }

            private String newName() {
                return "ams-concurrent-userstats-" + counter.incrementAndGet();
            }
        };
        return new ScheduledThreadPoolExecutor(1, tf) {

            @Override
            public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay,
                                                          long period, TimeUnit unit) {
                purge();
                return super.scheduleAtFixedRate(command, initialDelay, period, unit);
            }
        };
    }
}

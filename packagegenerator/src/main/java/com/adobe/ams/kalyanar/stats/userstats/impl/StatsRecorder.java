package com.adobe.ams.kalyanar.stats.userstats.impl;

import com.adobe.ams.kalyanar.stats.userstats.ConcurrentUserStats;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by kalyanar on 8/12/2015.
 */
public class StatsRecorder {

    /** Measured value per second over the last minute. This is a circular buffer */
    private final long[] valuePerSecond = new long[60];



    private int seconds = 0;

    private final ConcurrentUserStats concurrentUserStats;


    public StatsRecorder(ConcurrentUserStats concurrentUserStats,ScheduledExecutorService executorService){
        this.concurrentUserStats = concurrentUserStats;
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                recordOneSecond();
            }
        },1,1, TimeUnit.SECONDS);
    }

    public synchronized long[] getValuePerSecond() {
        return cyclicCopyFrom(valuePerSecond, seconds);
    }

    public synchronized void recordOneSecond() {
            valuePerSecond[seconds++] = concurrentUserStats.getConcurrentUserCount();
        if (seconds == valuePerSecond.length) {
            seconds = 0;
        }
    }

    /**
     * Returns a copy of the given cyclical array, with the element at
     * the given position as the first element of the returned array.
     *
     * @param array cyclical array
     * @param pos position of the first element
     * @return copy of the array
     */
    private static long[] cyclicCopyFrom(long[] array, int pos) {
        long[] reverse = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            reverse[i] = array[(pos + i) % array.length];
        }
        return reverse;
    }
}

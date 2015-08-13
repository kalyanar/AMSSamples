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

    /** Measured value per minute over the last hour. */
    private final long[] valuePerMinute = new long[60];

    /** Measured value per hour over the last 8 hours. */
    private final long[] valuePerHour = new long[ 8];


    private int seconds = 0;
    private int minutes  = 0;
    private int hours = 0;

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
    public synchronized long[] getValuePerMinute() {
        return cyclicCopyFrom(valuePerMinute, minutes);
    }
    public synchronized long[] getValuePerHourPastEightHour() {
        return cyclicCopyFrom(valuePerHour, hours);
    }

    public synchronized void recordOneSecond() {
            valuePerSecond[seconds++] = concurrentUserStats.getConcurrentUserCount();
        if (seconds == valuePerSecond.length) {
            seconds = 0;
            valuePerMinute[minutes++]=average(valuePerSecond);
        }
        if (minutes == valuePerMinute.length) {
            minutes = 0;
            valuePerHour[hours++] = average(valuePerMinute);
        }
        if (hours == valuePerHour.length) {
            hours = 0;
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
    /**
     * Returns the average of all entries in the given array.
     *
     * @param array array to be summed
     * @return sum of entries
     */
    private long average(long[] array) {
        long sum = 0;
        for (int i = 0; i < array.length; i++) {

            sum += array[i];
        }
        return sum / array.length;
    }
}

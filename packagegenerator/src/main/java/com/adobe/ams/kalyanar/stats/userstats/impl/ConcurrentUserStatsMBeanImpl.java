package com.adobe.ams.kalyanar.stats.userstats.impl;

import com.adobe.ams.kalyanar.stats.userstats.ConcurrentUserStatsMBean;
import com.adobe.ams.kalyanar.stats.userstats.ResetUserStats;

/**
 * Created by kalyanar on 8/12/2015.
 */
public class ConcurrentUserStatsMBeanImpl implements ConcurrentUserStatsMBean {

    private StatsRecorder statsRecorder;
    private ResetUserStats resetUserStats;

    public ConcurrentUserStatsMBeanImpl(StatsRecorder statsRecorder,ResetUserStats resetUserStats){
        this.statsRecorder = statsRecorder;
        this.resetUserStats = resetUserStats;
    }

    public String getName() {
        return "concurrent user stats";
    }

    public long[] concurrentUserCountLastOneMinuteEachSecond() {
        return statsRecorder.getValuePerSecond();
    }

    public void resetStats() {
        resetUserStats.reset();
    }
}

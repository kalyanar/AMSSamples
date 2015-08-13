package com.adobe.ams.kalyanar.stats.userstats.impl;

import com.adobe.ams.kalyanar.stats.userstats.ConcurrentUserStatsMBean;
import com.adobe.ams.kalyanar.stats.userstats.ResetUserStats;

import javax.management.openmbean.*;

/**
 * Created by kalyanar on 8/12/2015.
 */
public class ConcurrentUserStatsMBeanImpl implements ConcurrentUserStatsMBean {
    public static final String[] ITEM_NAMES = new String[] {"per second for last one minute", "per minute for last one hour", "per hour for past 8 hours"};

    private StatsRecorder statsRecorder;
    private ResetUserStats resetUserStats;

    public ConcurrentUserStatsMBeanImpl(StatsRecorder statsRecorder,ResetUserStats resetUserStats){
        this.statsRecorder = statsRecorder;
        this.resetUserStats = resetUserStats;
    }

    public String getName() {
        return "concurrent user stats";
    }

    public CompositeData concurrentUserCount() {
        long[][] values =  new long[][]{statsRecorder.getValuePerSecond(),statsRecorder.getValuePerMinute(),statsRecorder.getValuePerHourPastEightHour()};
        try {
            return new CompositeDataSupport(getCompositeType(ConcurrentUserStatsMBean.TYPE), ITEM_NAMES, values);
        } catch (OpenDataException e) {
            throw new IllegalArgumentException("Error creating CompositeData instance from USER STATS", e);
        }
    }

    public void resetStats() {
        resetUserStats.reset();
    }
    private CompositeType getCompositeType(String name) throws OpenDataException {
        ArrayType<int[]> longArrayType = new ArrayType<int[]>(SimpleType.LONG, true);
        OpenType<?>[] itemTypes = new OpenType[] {longArrayType, longArrayType, longArrayType};
        return new CompositeType(name, name + " time series", ITEM_NAMES, ITEM_NAMES, itemTypes);
    }
}

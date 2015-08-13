package com.adobe.ams.kalyanar.stats.userstats  ;


import javax.management.openmbean.CompositeData;

/**
 * Created by kalyanar on 8/11/2015.
 */
public interface ConcurrentUserStatsMBean {
    String TYPE = "ConcurrentUserStats";
    String getName();
    CompositeData concurrentUserCount();
    void resetStats();
}

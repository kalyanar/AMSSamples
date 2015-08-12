package com.adobe.ams.kalyanar.stats.userstats  ;


/**
 * Created by kalyanar on 8/11/2015.
 */
public interface ConcurrentUserStatsMBean {
    String TYPE = "ConcurrentUserStats";
    String getName();
    long[] concurrentUserCountLastOneMinuteEachSecond();
    void resetStats();
}

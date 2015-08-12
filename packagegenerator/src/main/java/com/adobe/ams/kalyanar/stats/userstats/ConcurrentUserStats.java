package com.adobe.ams.kalyanar.stats.userstats;

/**
 * Created by kalyanar on 8/12/2015.
 */
public interface ConcurrentUserStats {
    long getConcurrentUserCount();
    void incrementByOne();
    void decrementByOne();
    void reset();
}

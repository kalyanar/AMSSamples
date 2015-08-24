package com.adobe.ams.jmx;

import javax.management.openmbean.CompositeData;

/**
 * Created by kalyanar on 8/16/2015.
 */
public interface ReplicationStatsMBean {
    String TYPE = "ReplicationAgentsStats";
    String getName();
    CompositeData allQueueCount();
    int queueCount(String replicationAgentName);
    void resetStats();
}

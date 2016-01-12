package com.adobe.ams.jmx.impl;

import com.adobe.ams.jmx.ReplicationStatsMBean;
import com.day.cq.replication.Agent;
import com.day.cq.replication.AgentManager;
import com.day.cq.replication.ReplicationQueue;

import javax.management.openmbean.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kalyanar on 8/16/2015.
 */
public class ReplicationStatsMBeanImpl implements ReplicationStatsMBean {
    private final AgentManager agentManager;

    public ReplicationStatsMBeanImpl(AgentManager agentManager){
        this.agentManager = agentManager;
    }

    public String getName() {
        return "Replicationstats";
    }

    public CompositeData allQueueCount() {
        Map<String,Agent> agents = agentManager.getAgents();
        Map<String,Integer> items = new HashMap<String, Integer>();
        for(Map.Entry<String,Agent> agentEntry : agents.entrySet()){
            String name = agentEntry.getKey();
            Agent agent = agentEntry.getValue();
            if(agent.isEnabled()){
                items.put(agent.getId(), agent.getQueue().entries().size());

            }
        }

        try {
            return getCompositeData("AMS",items);
        } catch (OpenDataException e) {

        }
        return null;
    }

    @Override
    public int queueCount(String replicationAgentName) {
        Map<String,Agent> agents = agentManager.getAgents();
        Agent agent = agents.get(replicationAgentName);
        return agent.getQueue().entries().size();
    }

    public void resetStats() {

    }

    @Override
    public boolean isAgentHealthy(String replicationAgentName) {
        Agent agent = agentManager.getAgents().get(replicationAgentName);
        if(agent.getQueue().entries().size()>0){
           ReplicationQueue.Entry entry = agent.getQueue().entries().get(0);

            try {
                Thread.sleep(10000);                 //1000 milliseconds is
                // one second.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            if(agent.getQueue().entries().size()>0){
                ReplicationQueue.Entry newEntry = agent.getQueue().entries()
                        .get(0);
                if((entry.getId().equals(newEntry.getId()))&&(entry
                        .getLastProcessTime().equals(newEntry.getLastProcessTime()))){
                    return false;
                }
                return true;
            }else {
                return true;
            }
        }
        return true;
    }

    private CompositeDataSupport getCompositeData(String name,Map<String,Integer> items) throws OpenDataException {
        OpenType integerType = SimpleType.INTEGER;
        int size = items.size();
        String[] itemNamesArray = new String[size];
        itemNamesArray = items.keySet().toArray(itemNamesArray);
        OpenType<?>[] itemTypes = new OpenType[size];

        Integer[] values = new Integer[size];
        for(int i=0;i<size;i++){
            itemTypes[i] = integerType;
            values[i] = items.get(itemNamesArray[i]);
        }

        CompositeType compositeType = new CompositeType(name, name + " Replication Stats", itemNamesArray, itemNamesArray, itemTypes);

        CompositeDataSupport compositeDataSupport = new CompositeDataSupport(compositeType,itemNamesArray,values);

        return compositeDataSupport;
    }
}

package com.adobe.ams.jmx.impl;

import com.adobe.ams.jmx.ReplicationStatsMBean;
import com.day.cq.replication.AgentManager;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.ComponentContext;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Created by kalyanar on 8/16/2015.
 */
@Component(immediate = true,name = "Replication Stats MBean register service", description = "Replication Stats Mbean register service" )
@Service(ReplicationStatsMBeanRegisterService.class)
public class ReplicationStatsMBeanRegisterService {

    @Reference
    private volatile AgentManager agentManager;

    @Activate
    public void activate(ComponentContext componentContext){
        ReplicationStatsMBean replicationStatsMBean = new ReplicationStatsMBeanImpl(agentManager);
        Dictionary<String, Object> serviceProps = new Hashtable<String, Object>();
        serviceProps.put("jmx.objectname", "com.adobe.ams" +
                ".stats:type=ReplicationStats");

        componentContext.getBundleContext().registerService(ReplicationStatsMBean.class
                        .getName(),
                replicationStatsMBean, serviceProps);
    }
}

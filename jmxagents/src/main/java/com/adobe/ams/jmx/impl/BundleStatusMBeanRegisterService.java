package com.adobe.ams.jmx.impl;

import com.adobe.ams.jmx.BundleStatusMBean;
import com.adobe.ams.jmx.ReplicationStatsMBean;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Created by kalyanar on 8/16/2015.
 */
@Component(immediate = true,name = "Bundle Status MBean register service",
        description = "Bundle Status Mbean register service" )
@Service(BundleStatusMBeanRegisterService.class)
public class BundleStatusMBeanRegisterService {
    @Activate
    public void activate(ComponentContext componentContext){
        BundleStatusMBean bundleStatusMBean = new BundleStatusMBeanImpl
                (componentContext.getBundleContext());
        Dictionary<String, Object> serviceProps = new Hashtable<String, Object>();
        serviceProps.put("jmx.objectname", "com.adobe.ams" +
                ".stats:type=BundleStats");

        componentContext.getBundleContext().registerService(BundleStatusMBean.class
                        .getName(),
                bundleStatusMBean, serviceProps);
    }
}

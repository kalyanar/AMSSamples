package com.adobe.ams.jmx;

import javax.management.openmbean.CompositeData;

/**
 * Created by kalyanar on 24/11/15.
 */
public interface BundleStatusMBean {

    String TYPE = "BundleStats";
    String getName();
    String[] inactiveBundles(String commaSeparatedBundleNames);
    boolean isBundleHealthy(String bundleName);
    String[] allInactiveBundles();

}

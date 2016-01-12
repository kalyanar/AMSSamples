package com.adobe.ams.jmx.impl;

import com.adobe.ams.jmx.BundleStatusMBean;
import org.apache.jackrabbit.oak.commons.StringUtils;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import javax.management.openmbean.CompositeData;
import java.util.*;

/**
 * Created by kalyanar on 24/11/15.
 */
public class BundleStatusMBeanImpl implements BundleStatusMBean {

    private BundleContext bundleContext;

    protected BundleStatusMBeanImpl(BundleContext bundleContext){
        this.bundleContext = bundleContext;
    }

    @Override
    public String getName() {
        return "Bundlestats";
    }

    @Override
    public String[] inactiveBundles(String commaSeparatedBundleNames) {
        List<String> inactivelist = new ArrayList<String>();
        Set<String> bundlesSet= new HashSet<String>
                (Arrays.asList(commaSeparatedBundleNames
                .split
                        (",")));
      Bundle[] bundles =  bundleContext.getBundles();
        for(Bundle bundle : bundles){
            if(bundlesSet.contains(bundle.getSymbolicName())){
                if(isInactive(bundle)){
                    inactivelist.add(bundle.getSymbolicName());
                }
            }
        }

        return inactivelist.toArray(new String[inactivelist.size()]);
    }


    @Override
    public boolean isBundleHealthy(String bundleName) {
        Bundle[] bundles =  bundleContext.getBundles();
        for(Bundle bundle : bundles){
            if(bundleName.equals(bundle.getSymbolicName())){
                if(!isInactive(bundle)){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String[] allInactiveBundles() {
        List<String> inactivelist = new ArrayList<String>();

        Bundle[] bundles =  bundleContext.getBundles();
        for(Bundle bundle : bundles){

                if(isInactive(bundle)){
                    inactivelist.add(bundle.getSymbolicName());
                }

        }

        return inactivelist.toArray(new String[inactivelist.size()]);
    }
    private boolean isInactive(Bundle bundle){
        if(bundle.getState()!=Bundle.ACTIVE && ! isFragmentBundle(bundle)) {
            return true;
        }
        return false;
    }
    private boolean isFragmentBundle(Bundle bundle){
        return  bundle.getHeaders().get( Constants.FRAGMENT_HOST ) != null;
    }
}

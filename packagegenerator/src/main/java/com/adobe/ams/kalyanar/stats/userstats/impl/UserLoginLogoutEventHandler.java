package com.adobe.ams.kalyanar.stats.userstats.impl;

import com.adobe.ams.kalyanar.stats.userstats.ConcurrentUserStats;
import com.adobe.ams.kalyanar.stats.userstats.ResetUserStats;
import com.google.common.collect.Sets;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.api.resource.*;
import org.osgi.framework.Constants;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kalyanar on 8/11/2015.
 */
@Component(immediate = true, name = "User login logout handler", description = "User login logout handler" )
@Service(value = {EventHandler.class,ResetUserStats.class})
@Properties({
        @Property(name = Constants.SERVICE_DESCRIPTION, value = "User login logout handler."),
        @Property(name = EventConstants.EVENT_TOPIC, value = {
                SlingConstants.TOPIC_RESOURCE_ADDED,
                SlingConstants.TOPIC_RESOURCE_REMOVED } , propertyPrivate = true),
        @Property(name = EventConstants.EVENT_FILTER, value = "(resourceType=rep:Token)")})
public class UserLoginLogoutEventHandler implements EventHandler,ResetUserStats {

    private static Logger logger = LoggerFactory.getLogger(UserLoginLogoutEventHandler.class);
    Map<String,Set<String>> users = new ConcurrentHashMap<String, Set<String>>();

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private ConcurrentUserStats concurrentUserStats;

    public void handleEvent(Event event) {
        String tokenResourcePath = (String) event.getProperty("path");
        try {
            String eventTopic  = event.getTopic();
            //must change to service user
            ResourceResolver resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
            Resource tokenResource = resourceResolver.getResource(tokenResourcePath);
            String tokenName = tokenResource.getName();
            Resource userResource = tokenResource.getParent().getParent();
            String authorizableId = userResource.adaptTo(ValueMap.class).get("rep:authorizableId","");
            if(SlingConstants.TOPIC_RESOURCE_ADDED.equals(eventTopic)){
                Set<String> tokens = getTokens(authorizableId);
                tokens.add(tokenName);
                if(!users.containsKey(authorizableId)){
                    concurrentUserStats.incrementSecByOne();
                }
                users.put(authorizableId,tokens);
            }else if(SlingConstants.TOPIC_RESOURCE_REMOVED.equals(eventTopic)){
                Set<String> tokens = getTokens(authorizableId);
                tokens.remove(tokenName);
                if(tokens.size()==0){
                    users.remove(authorizableId);
                    concurrentUserStats.decrementSecByOne();
                }
            }
        } catch (LoginException e) {

        }
    }
    private Set<String> getTokens(String authorizableid){
        if(users.containsKey(authorizableid)){
            return users.get(authorizableid);
        }
        return Sets.newConcurrentHashSet();
    }

    public synchronized void reset() {
        users.clear();
        concurrentUserStats.reset();
    }
}

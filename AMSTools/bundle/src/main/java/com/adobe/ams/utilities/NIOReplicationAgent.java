package com.adobe.ams.utilities;

import com.day.cq.replication.*;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

/**
 * Created by kalyanar on 19/12/15.
 */
@Component(metatype = true)
@Service(value = TransportHandler.class)
public class NIOReplicationAgent implements TransportHandler,PollingTransportHandler {
    EventLoopGroup workerGroup = new NioEventLoopGroup();


    @Override
    public ReverseReplication[] poll(TransportContext transportContext, ReplicationTransaction replicationTransaction, ReplicationContentFactory replicationContentFactory) throws ReplicationException {
        return new ReverseReplication[0];
    }

    @Override
    public boolean canHandle(AgentConfig agentConfig) {
        String uri = agentConfig == null ? null : agentConfig.getTransportURI();
        return uri != null && (uri.startsWith("netty://") || uri.startsWith
                ("nettys://"));
    }

    @Override
    public ReplicationResult deliver(TransportContext transportContext, ReplicationTransaction replicationTransaction) throws ReplicationException {

        return null;
    }
}

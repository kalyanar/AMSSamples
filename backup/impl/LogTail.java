package com.adobe.ams.kalyanar.utilities.logtail.impl;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by kalyanar on 8/30/2015.
 */
public class LogTail implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(LogTail.class);

    enum STATE {
        RUNNING,
        STOPPED
    }

    private volatile STATE state;
    private String logFilePath;
    private File logfile;
    private ChannelGroup channelGroup;
    private Thread thread;

    public LogTail(String logFilePath, ChannelGroup channelGroup){
        logfile = new File(logFilePath);
        this.channelGroup = channelGroup;
        state = STATE.STOPPED;
    }

    public void add(Channel channel){
        channelGroup.add(channel);
    }

    public void fetchTail(){
        if(state != STATE.RUNNING){
            thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public void run() {
        state = STATE.RUNNING;
        log.info(logFilePath +" tail started");
        long pointer = logfile.length();
        long lastmodified = System.currentTimeMillis();
        for(;;){
            if(state == STATE.STOPPED){
                break;
            }
            long len = logfile.length();
            if((len < pointer) && (logfile.lastModified()>lastmodified)){
                pointer = 0l;
                lastmodified = System.currentTimeMillis();
            }
            if(len>pointer){
                RandomAccessFile  raf = null;
                try {
                    raf = new RandomAccessFile(logfile,"r");
                    raf.seek(pointer);
                    String line = "";
                    while ((line = raf.readLine()) != null) {
                       channelGroup.writeAndFlush(new TextWebSocketFrame(line +"<BR/>"));
                    }
                    pointer = raf.getFilePointer();
                    raf.close();
                } catch (FileNotFoundException e) {
                    log.error(logFilePath +"  could not be read",e);
                } catch (IOException e) {
                    log.error(logFilePath + "  could not be read", e);
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                state = STATE.STOPPED;
                Thread.interrupted();
            }
        }
    }

    public boolean stop(){
        state = STATE.STOPPED;
        thread.interrupt();
        return false;
    }
}

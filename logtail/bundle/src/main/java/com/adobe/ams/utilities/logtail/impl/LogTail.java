package com.adobe.ams.utilities.logtail.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by kalyanar on 8/31/2015.
 */
public class LogTail implements Runnable {

    enum STATE{
        NOTRUNNING,
        RUNNING
    }
    private File logfile;
    private volatile STATE state = STATE.NOTRUNNING;
    private Thread thread;
    private LogWriter logWriter;

    public LogTail(String logFilePath,LogWriter logWriter){
        this.logfile = new File(logFilePath);
        this.logWriter = logWriter;
    }

    public void start(){
        if((state ==STATE.NOTRUNNING) || (thread==null || !thread.isAlive()||thread.isInterrupted())){
            state = STATE.RUNNING;
            thread = new Thread(this);
         //   thread.setDaemon(true);
            thread.start();
        }
    }

    public WebsocketChannelGroup getWebsocketChannelGroup(){
        return (WebsocketWriter)logWriter;
    }

    @Override
    public void run() {
        long pointer = logfile.length();
        long lastmodifiedtime = System.currentTimeMillis();
        for(;;){
            if(state == STATE.NOTRUNNING || thread==null || thread.isInterrupted()){
                break;
            }

        //    if(logfile.lastModified() > lastmodifiedtime){
              long len = logfile.length();
               if(len < pointer){
                   if(logfile.lastModified() > lastmodifiedtime){
                       pointer = 0l;
                   }else {
                       pointer = len;
                   }
               }

                  try{
                      RandomAccessFile randomAccessFile = new RandomAccessFile(logfile,"r");
                      randomAccessFile.seek(pointer);
                      String line;
                      while ((line = randomAccessFile.readLine()) != null) {
                          logWriter.write(line);
                      }
                      pointer = randomAccessFile.getFilePointer();
                      randomAccessFile.close();
                      try {
                          Thread.sleep(1000);
                      } catch (InterruptedException e) {
                          state = STATE.NOTRUNNING;
                          Thread.interrupted();
                          break;
                      }
                  } catch (FileNotFoundException e) {
                      state = STATE.NOTRUNNING;
                  } catch (IOException e) {
                      state = STATE.NOTRUNNING;
                  }
        }
    }

    public static void main(String[] args) {
        LogTail logTail = new LogTail("C:\\cq\\cq\\author-61\\crx-quickstart\\logs\\access.log",new ConsoleWriter());
        logTail.start();

    }
}

package com.adobe.ams.utilities.logtail.impl;

/**
 * Created by kalyanar on 8/31/2015.
 */
public interface LogWriter {
    void write(String line);
    void writeAndFlush(String line);
}

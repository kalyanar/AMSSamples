package com.adobe.ams.utilities.logtail.impl;

/**
 * Created by kalyanar on 8/31/2015.
 */
public class ConsoleWriter implements LogWriter {
    @Override
    public void write(String line) {
        System.out.println(line);
    }

    @Override
    public void writeAndFlush(String line) {
        System.out.println( line);
    }
}

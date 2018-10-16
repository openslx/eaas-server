package me.jtalk.socketconnector.utils;


import me.jtalk.socketconnector.ExistingTCPConnectionRequest;
import me.jtalk.socketconnector.NewTCPConnectionRequest;

import javax.resource.spi.ConnectionEventListener;

public class LazyLogger {
    public static void lazyTrace(ConnectorLogger log, String s, String className) {
        log.trace(s + " className " + className);
    }

    public static void lazyTrace(ConnectorLogger log, String s, String className, String oldName) {
        log.trace(s + " className " + className + " oldName " + oldName);
    }

    public static void lazyTrace(ConnectorLogger log, String s, ConnectionEventListener listener, String className) {
        log.trace(s + " className " + className + " listener " + listener);

    }
    public static void lazyTrace(ConnectorLogger log, String s, String className, NewTCPConnectionRequest request) {
        log.trace(s + "className" + className + " request " + request.toString());

    }
    public static void lazyTrace(ConnectorLogger log, String s, String className, ExistingTCPConnectionRequest request) {
        log.trace(s + "className" + className + " request " + request.toString());

    }
    public static void lazyTrace(ConnectorLogger log, String s) {
        log.trace(s);
    }
}

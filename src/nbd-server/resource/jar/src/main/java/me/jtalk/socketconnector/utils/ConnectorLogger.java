/*
 * Copyright (C) 2016 Roman Nazarenko <me@jtalk.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.jtalk.socketconnector.utils;



import java.util.logging.Level;
import java.util.logging.Logger;
import javax.resource.spi.ConnectionEventListener;


public class ConnectorLogger {

    private static Logger log = Logger.getLogger(ConnectorLogger.class.getName());


    public void error(String message) {
        log.log(Level.SEVERE, message);
    }


    public void info(String message, String status) {
        log.info(message + " " + status);
    }

    public ConnectorLogger(String className) {
        log = Logger.getLogger(className);

    }

    public void warn(String warn, String name) {
        log.warning(warn + " " + name);

    }


    public void info(String message) {
        log.info(message);
    }

    public void trace(String message, int id, ConnectionEventListener connectionEventListener) {
        log.log(Level.FINEST, message + " id:" + id + connectionEventListener.toString());
    }

    public void trace(String message) {
        log.log(Level.FINEST, message);
    }

    public void trace(String message, long id) {
        log.log(Level.FINEST, message + " id " + id);
    }

    public void trace(String message, String target) {
        log.log(Level.FINEST, message + " target " + target);
    }

    public void trace(String message, long id1, long id2) {
        trace(message + " id " + id1, id2);
    }

    public void trace(String message, long id, String target, int port) {
        trace(message + " id " + id + " target " + target + " port " + port);
    }

    public void trace(String message, long clientId, long id, boolean result) {
        trace(message + " clientId " + clientId + " id " + id + " result " + result);
    }

    public void error(String message, int id) {
        error(message + " id " + id);
    }

    public void error(String message, String error) {
        error(message + " error " + error);
    }





}

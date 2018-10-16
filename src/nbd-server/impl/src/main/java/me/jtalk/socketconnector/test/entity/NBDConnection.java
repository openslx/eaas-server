/*
 * NBD Server
 * Oleg Zharkov
 * Uni Freiburg
 * 2016
 * <p>
 * was built on the top of socketconnector
 * https://bitbucket.org/__jtalk/socketconnector
 */

package me.jtalk.socketconnector.test.entity;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


public class NBDConnection {
    private FileChannel fileChannel;
    private ByteBuffer receivedData;
    private boolean initialPhase = true;


    public NBDConnection(ByteBuffer receivedData) {
        this.receivedData = receivedData;
    }

    public FileChannel getFileChannel() {
        return fileChannel;
    }

    public ByteBuffer getReceivedData() {
        return receivedData;
    }

    public void setFileChannel(FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    public void setReceivedData(ByteBuffer receivedData) {
        this.receivedData = receivedData;
    }

    public void setInitialPhase(boolean initialPhase) {
        this.initialPhase = initialPhase;
    }

    public boolean isInitialPhase() {
        return initialPhase;
    }
}

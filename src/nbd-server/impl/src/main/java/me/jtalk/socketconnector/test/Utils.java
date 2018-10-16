/*
 * NBD Server
 * Oleg Zharkov
 * Uni Freiburg
 * 2016
 * <p>
 * was built on the top of socketconnector
 * https://bitbucket.org/__jtalk/socketconnector
 */

package me.jtalk.socketconnector.test;


import java.nio.ByteBuffer;



class Utils {
    static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    static String intToString(int x) {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(x);
        return new String(b.array());
    }

    static byte[] toBytes(int i) {
        byte[] result = new byte[4];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i /*>> 0*/);

        return result;
    }


}

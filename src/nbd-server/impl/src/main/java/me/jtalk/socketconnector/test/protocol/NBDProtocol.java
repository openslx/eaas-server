/*
 * NBD Server
 * Oleg Zharkov
 * Uni Freiburg
 * 2016
 * <p>
 * was built on the top of socketconnector
 * https://bitbucket.org/__jtalk/socketconnector
 */

package me.jtalk.socketconnector.test.protocol;

public class NBDProtocol {
    public static final byte[] SERVER_RESPOND_FLAG = new byte[2];
    public static final byte[] EMPTY_124 = new byte[122];
    public static final long CLIENT_MAGIC = 0x49484156454F5054L;
    public static final int CLIENT_MAGIC_RESPONSE = 0x25609513;
    public static final int NBD_REPLY_MAGIC = 0x67446698;
    public static final byte[] NBD_OK_BYTES = new byte[4];
    public static final int MINIMAL_HANDSHAKE_LENGTH = 16;

}

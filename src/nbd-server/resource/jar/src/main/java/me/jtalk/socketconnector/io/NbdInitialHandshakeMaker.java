package me.jtalk.socketconnector.io;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by oleg on 17.11.16.
 */
public class NbdInitialHandshakeMaker {
    public static final long OPTS_MAGIC = 0x49484156454F5054L;
    public final static String NBD_MAGIC = "NBDMAGIC";
    public static final int NBD_FLAG_HAS_FLAGS  = (1 << 0);	/* Flags are there */


    protected static ByteBuf createBuffer(ChannelHandlerContext ctx) {
        final ByteBuf initialHandshake = ctx.alloc().buffer(22); // (2)


        initialHandshake.writeBytes(NBD_MAGIC.getBytes());
        initialHandshake.writeLong(OPTS_MAGIC);
        initialHandshake.writeInt(NBD_FLAG_HAS_FLAGS);
        return initialHandshake;
    }
}

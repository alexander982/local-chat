/*
 * Copyright (c) 2017 Kozlov A.
 */

package local.chat;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by bepr on 11.09.17.
 */
public class MessageEncoder extends MessageToMessageEncoder<Message> {
    private Logger log = Logger.getLogger(MessageEncoder.class);
    private final InetSocketAddress remoteAddress;

    public MessageEncoder(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message message,
                          List<Object> out) throws Exception {
        Charset charset = Charset.forName("CP1251");
        log.debug("Encoding message " + message.id);
        byte[] id = message.id.getBytes(charset);
        byte[] type = message.type.getBytes(charset);
        byte[] channel = message.channel.getBytes(charset);
        byte[] from = message.from.getBytes(charset);
        byte[] msg = message.msg.getBytes(charset);
        byte[] color = message.color.getBytes(charset);
        ByteBuf buf = ctx.alloc().buffer(id.length + type.length + channel.length
                + 1 + from.length + 1 + msg.length + 1 + color.length + 1);
        buf.writeBytes(id);
        buf.writeBytes(type);
        buf.writeBytes(channel);
        buf.writeByte(Message.SEPARATOR);
        buf.writeBytes(from);
        buf.writeByte(Message.SEPARATOR);
        buf.writeBytes(msg);
        buf.writeByte(Message.SEPARATOR);
        buf.writeBytes(color);
        buf.writeByte(Message.SEPARATOR);
        out.add(new DatagramPacket(buf, remoteAddress));
    }
}

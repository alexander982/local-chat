/*
 * Copyright (c) 2017 Kozlov A.
 */

package local.chat;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.apache.log4j.Logger;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by bepr on 08.09.17.
 */
public class MessageDecoder extends MessageToMessageDecoder<DatagramPacket> {
    private Logger log = Logger.getLogger(MessageDecoder.class);
    private String lastId = null;
    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket datagramPacket, List<Object> out) {
        log.debug("Packed received from " + datagramPacket.sender());
        ByteBuf data = datagramPacket.content();
        Charset charset = Charset.forName("CP1251");
        log.debug("packet data: " + data.slice(0, data.readableBytes()).toString(charset));
        if (data.getByte(0) != (byte) 'X') {
            log.debug("Not a message");
            return;
        }
        String id = data.slice(0, 9).toString(charset);

        if (lastId == null || !lastId.equals(id)) {
            lastId = id;
            String type = data.slice(10, 1).toString(charset);
            log.debug("message type " + type);
            String from;
            Message msg;
            if ("2".equals(type)) {
                int idx1 = data.indexOf(11, data.readableBytes(), Message.SEPARATOR);
                String channel = data.slice(11, idx1 - 1).toString(charset);
                int idx2 = data.indexOf(idx1 + 1, data.readableBytes(), Message.SEPARATOR);
                from = data.slice(idx1 + 1, idx2 - idx1 - 1).toString(charset);
                idx1 = idx2;
                idx2 = data.indexOf(idx1 + 1, data.readableBytes(), Message.SEPARATOR);
                String message = data.slice(idx1 + 1, idx2 - idx1 - 1).toString(charset);
                log.info(from + ": " + message);
                idx1 = idx2;
                idx2 = data.indexOf(idx1 + 1, data.readableBytes(), Message.SEPARATOR);
                String color = data.slice(idx1 + 1, idx2 - idx1 - 1).toString(charset);
                msg = new Message(id, type, from, channel, message, color);
            } else {
                int idx = data.indexOf(11, data.readableBytes(), Message.SEPARATOR) - 1;
                from = data.slice(11, idx - 10)
                        .toString(charset);
                msg = new Message(id, type, from);
            }

            out.add(msg);
        } else {
            log.debug("once again message with id: " + id);
        }
    }
}

/*
 * Copyright (c) 2017 Kozlov A.
 */

package local.chat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by bepr on 08.09.17.
 */
public class MessageHandler extends
        SimpleChannelInboundHandler<Message> {
    private Logger log = Logger.getLogger(MessageHandler.class);
    private Main main;

    MessageHandler(Main main) {
        super();
        this.main = main;
        log.debug("MessageHandler created");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("Input channel active");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(msg.getTimestamp());
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(cal.get(Calendar.HOUR_OF_DAY))
                .append(":")
                .append(cal.get(Calendar.MINUTE))
                .append(":")
                .append(cal.get(Calendar.SECOND))
                .append("] ")
                .append(msg.from)
                .append(": ");
        if (msg.type.equals("2")) {
            sb.append(msg.msg).append("\n");
            SwingUtilities.invokeLater(() -> main.getMsgLog().append(sb.toString()));
        }
        log.debug("Decoded message received");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
        cause.printStackTrace();
        ctx.close();
    }
}

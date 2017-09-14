/*
 * Copyright (c) 2017 Kozlov A.
 */

package local.chat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.apache.log4j.Logger;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetSocketAddress;

public class Main {
    private String name = "admin";
    private Bootstrap bootstrapInChannel;
    private Bootstrap bootstrapOutChannel;
    private EventLoopGroup group;
    private JTextArea msgLog;
    private Channel outChannel;
    private static Logger log = Logger.getLogger(Main.class);

    Main() {
        group = new NioEventLoopGroup();
    }

    private void initInbound(InetSocketAddress address) {
        bootstrapInChannel = new Bootstrap();
        Main t = this;
        bootstrapInChannel.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new MessageDecoder());
                        pipeline.addLast(new MessageHandler(t));
                    }
                }).localAddress(address);
    }

    private void initOutbound(InetSocketAddress address) {
        bootstrapOutChannel = new Bootstrap();
        bootstrapOutChannel.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new MessageEncoder(address));
    }

    public void init(InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
        initInbound(localAddress);
        initOutbound(remoteAddress);
    }

    public JTextArea getMsgLog() {
        return msgLog;
    }

    public Channel bindIn() throws Exception {
        return bootstrapInChannel.bind().sync().channel();
    }

    public Channel bindOut() throws Exception {
        outChannel = bootstrapOutChannel.bind(0).sync().channel();
        return outChannel;
    }

    public void stop() {
        group.shutdownGracefully();
    }

    private void makeGUI() {
        JFrame jfrm = new JFrame("Chat");
        jfrm.setLayout(new BorderLayout());
        jfrm.setSize(400, 300);
        jfrm.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                super.windowClosing(windowEvent);
                group.shutdownGracefully();
                System.exit(0);
            }
        });

        msgLog = new JTextArea();
        msgLog.setEditable(false);
        //msgLog.setRows(4);
        //msgLog.setColumns(20);

        JScrollPane jsp = new JScrollPane(msgLog);

        jfrm.add(jsp, BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout());

        JTextField message = new JTextField();
        message.requestFocusInWindow();
        JButton sendBtn = new JButton("Send");
        ActionListener ae = ae1 -> {
            Message msg = new Message(null, "2", name,
                    "#Main", message.getText(), "$000B04FF");
            outChannel.writeAndFlush(msg);
            message.setText(null);
        };
        message.addActionListener(ae);
        sendBtn.addActionListener(ae);

        panel.add(message, BorderLayout.CENTER);
        panel.add(sendBtn, BorderLayout.LINE_END);

        jfrm.add(panel, BorderLayout.PAGE_END);

        //jfrm.pack();
        jfrm.setVisible(true);
        log.info("GUI created");
    }

    public static void main(String[] args) throws Exception {
        final int port = 8167;
        log.info("Start local chat...");
        Main chat = new Main();
        chat.init(new InetSocketAddress(port),
                new InetSocketAddress("255.255.255.255", port));
        try {
            Channel channel = chat.bindIn();
            try {
                chat.bindOut();
                SwingUtilities.invokeLater(chat::makeGUI);
                log.info("Output channel running on: " + chat.outChannel.localAddress());
            } catch (Exception e) {
                log.error("Bind out channel error.", e);
            }
            log.info("Monitor running on port: " + port);
            channel.closeFuture().sync();
        } finally {
            chat.stop();
        }

    }
}

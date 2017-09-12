/*
 * Copyright (c) 2017 Kozlov A.
 */

package local.chat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetSocketAddress;

public class Main {
    private Bootstrap bootstrap;
    private EventLoopGroup group;
    private JTextArea msgLog;
    private Channel channel;

    Main() {
    }

    public void init(InetSocketAddress address) {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        Main t = this;
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new MessageDecoder());
                        pipeline.addLast(new MessageHandler(t));
                        pipeline.addLast(new MessageEncoder(address));
                    }
                }).localAddress(address);
    }

    public JTextArea getMsgLog() {
        return msgLog;
    }

    public Channel bind() throws Exception {
        channel = bootstrap.bind().sync().channel();
        return channel;
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
        sendBtn.addActionListener((ae) -> {
            Message msg = new Message(null, "2", "unknown",
                    "#Main", message.getText(), "$000B04FF");
            channel.writeAndFlush(msg);
            message.setText(null);
        });

        panel.add(message, BorderLayout.CENTER);
        panel.add(sendBtn, BorderLayout.LINE_END);

        jfrm.add(panel, BorderLayout.PAGE_END);

        //jfrm.pack();
        jfrm.setVisible(true);
    }

    public static void main(String[] args) throws Exception {
        final int port = 8167;
        Main chat = new Main();
        chat.init(new InetSocketAddress(port));
        try {
            Channel channel = chat.bind();
            SwingUtilities.invokeLater(() -> chat.makeGUI());
            System.out.println("Monitor running on port: " + port);
            channel.closeFuture().sync();
        } finally {
            chat.stop();
        }

    }
}

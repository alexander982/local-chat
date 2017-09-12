/*
 * Copyright (c) 2017 Kozlov A.
 */

package local.chat;

import java.util.Random;

/**
 * Created by bepr on 08.09.17.
 */
public class Message {
    public final static byte SEPARATOR = 0;
    public String id;
    public String msg;
    public long timestamp;
    public String type;
    public String color;
    public String channel;
    public String from;
    public String to;

    public Message() {
        timestamp = System.currentTimeMillis();
    }

    public Message(String id, String type, String from) {
        this();
        if (id == null) {
            this.id = createId();
        } else {
            this.id = id;
        }
        this.type = type;
        this.from = from;
    }

    public Message(String id, String type, String from, String channel, String msg, String color) {
        this(id, type, from);
        this.channel = channel;
        this.msg = msg;
        this.color = color;
    }

    private String createId() {
        String id = "X";
        Random rnd = new Random();
        char[] chars = "abcdefghigklmnopqrstuvwxyz".toCharArray();
        for (int i = 0; i < 9; i++)
            id += chars[rnd.nextInt(26)];
        System.out.println("create msg with id: " + id);
        return id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}

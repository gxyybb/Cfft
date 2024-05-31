package com.example.cfft;

// ChatMessage.java
// ChatMessage.java
public class ChatMessage {

    public static final int TYPE_INCOMING = 1;
    public static final int TYPE_OUTGOING = 2;

    private String text;
    private int type;

    public ChatMessage(String text, int type) {
        this.text = text;
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public int getType() {
        return type;
    }
}



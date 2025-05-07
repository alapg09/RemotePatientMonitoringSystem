package com.rpms.ChatAndVideoConsultation;

import com.rpms.utilities.DateUtil;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ChatMessage implements Serializable {
    private String senderId;
    private String senderName;
    private String receiverId;
    private String content;
    private LocalDateTime timestamp;
    private static final long serialVersionUID = 1L;

    public ChatMessage(String senderId, String senderName, String receiverId, String content) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "[" + DateUtil.format(timestamp) + "] " + senderName + ": " + content;
    }
}
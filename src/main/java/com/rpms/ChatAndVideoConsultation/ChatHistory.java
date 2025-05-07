package com.rpms.ChatAndVideoConsultation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChatHistory implements Serializable {
    private String user1Id;
    private String user2Id;
    private ArrayList<ChatMessage> messages;
    private static final long serialVersionUID = 1L;

    public ChatHistory(String user1Id, String user2Id) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.messages = new ArrayList<>();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public String getUser1Id() {
        return user1Id;
    }

    public String getUser2Id() {
        return user2Id;
    }

    // This method checks if this chat history belongs to the specified pair of users
    public boolean isForUsers(String userId1, String userId2) {
        return (user1Id.equals(userId1) && user2Id.equals(userId2)) ||
               (user1Id.equals(userId2) && user2Id.equals(userId1));
    }
}
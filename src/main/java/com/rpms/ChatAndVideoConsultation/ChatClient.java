package com.rpms.ChatAndVideoConsultation;

import java.util.ArrayList;

public class ChatClient {
    // required fields
    private int userId;
    private String userType; // "Patient" or "Doctor"
    private int sessionId;
    private ChatServer server;
    private ArrayList<String> receivedMessages;

    // constructor
    public ChatClient(int userId, String userType, ChatServer server) {
        this.userId = userId;
        this.userType = userType;
        this.server = server;
        this.receivedMessages = new ArrayList<String>();
    }

    // getters and setters
    public int getUserId() {
        return userId;
    }
    public String getUserType() {
        return userType;
    }
    public int getSessionId() {
        return sessionId;
    }
    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }
    public void setUserType(String userType) {
        this.userType = userType;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }

    // connect to a specific chat session
    public void connect(int sessionId) {
        this.sessionId = sessionId;
        server.registerClient(this);

        // load message history
        ArrayList<String> history = server.getMessageHistory(sessionId);
        System.out.println("Connected to session " + sessionId + ". " + history.size() + " previous messages.");

        // add history to received messages
        for (int i = 0; i < history.size(); i++) {
            receivedMessages.add(history.get(i));
        }
    }

    // disconnect from the chat session
    public void disconnect() {
        server.unregisterClient(this);
        System.out.println("Disconnected from session " + sessionId);
    }

    // send a message to the chat session
    public void sendMessage(String message) {
        server.sendMessage(sessionId, userId, userType, message);
    }

    // receive a message from the server
    public void receiveMessage(String message) {
        receivedMessages.add(message);
        System.out.println("New message received: " + ChatServer.formatMessageForDisplay(message));
    }

    // get all received messages in displayable format
    public ArrayList<String> getDisplayMessages() {
        ArrayList<String> displayMessages = new ArrayList<String>();

        for (int i = 0; i < receivedMessages.size(); i++) {
            String formatted = ChatServer.formatMessageForDisplay(receivedMessages.get(i));
            displayMessages.add(formatted);
        }

        return displayMessages;
    }
    @Override
    public String toString() {
        return "ChatClient{" +
                "userId=" + userId +
                ", userType='" + userType + '\'' +
                ", sessionId=" + sessionId +
                '}';
    }

}

package com.rpms.ChatAndVideoConsultation;

import java.util.ArrayList;

public class ChatServer {
    // contains previous messages and all connected clients
    private ArrayList<String> messageHistory;
    private ArrayList<ChatClient> connectedClients;

    // constructor
    // initializes the message history and connected clients list
    public ChatServer() {
        messageHistory = new ArrayList<String>();
        connectedClients = new ArrayList<ChatClient>();
    }

    // register a client to receive messages
    public void registerClient(ChatClient client) {
        connectedClients.add(client);
        System.out.println("Client registered: " + client.getUserId() + " (" + client.getUserType() + ")");
    }


    // remove a client from receiving messages
    public void unregisterClient(ChatClient client) {
        connectedClients.remove(client);
        System.out.println("Client unregistered: " + client.getUserId());
    }

    // send a message to a specific chat session
    public void sendMessage(int sessionId, int senderId, String senderType, String messageContent) {
        // new timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());
        // formatted message for a specific message
        String formattedMessage = sessionId + "|" + senderId + "|" + senderType + "|" + timestamp + "|" + messageContent;

        // Save message to history
        messageHistory.add(formattedMessage);

        // deliver to connected clients in this session
        for (int i = 0; i < connectedClients.size(); i++) {
            // get the client from the list
            ChatClient client = connectedClients.get(i);

            // sending the message only if the client is in the same session and not the sender
            if (client.getSessionId() == sessionId && client.getUserId() != senderId) {
                client.receiveMessage(formattedMessage);
            }
        }
    }


    // get message history for a specific session
    public ArrayList<String> getMessageHistory(int sessionId) {
        // list to store messages for the session
        ArrayList<String> sessionMessages = new ArrayList<String>();

        // looping over all messages
        for (int i = 0; i < messageHistory.size(); i++) {
            // split the message to get the session ID
            String message = messageHistory.get(i);
            String[] parts = message.split("\\|");
            int messageSessionId = Integer.parseInt(parts[0]);

            // checking if the message belongs to the session
            if (messageSessionId == sessionId) {
                sessionMessages.add(message);
            }
        }

        return sessionMessages;
    }
    // format message for display
    public static String formatMessageForDisplay(String message) {
        // split the message to get the parts
        String[] parts = message.split("\\|");
        int sessionId = Integer.parseInt(parts[0]);
        int senderId = Integer.parseInt(parts[1]);
        String senderType = parts[2];
        String timestamp = parts[3];
        String content = parts[4];

        // format the message for display
        return "[" + sessionId + "] " + senderType + " " + senderId + " (" + timestamp + "): " + content;
    }

    // no need for toString method as this class is not meant to be printed directly

}


package com.rpms.GUI;

import com.rpms.ChatAndVideoConsultation.ChatManager;
import com.rpms.ChatAndVideoConsultation.ChatMessage;
import com.rpms.ChatAndVideoConsultation.ChatHistory;
import com.rpms.UserManagement.User;
import com.rpms.utilities.DataManager;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * Provides a chat interface for communication between doctors and patients.
 * Handles connection to the chat server, message sending/receiving, and chat history.
 */
public class ChatWindow {
    /** The current user (patient or doctor) using this chat window */
    private User currentUser;
    
    /** The us  er the current user is chatting with */
    private User chatPartner;
    
    /** The window stage for this chat */
    private Stage stage;
    
    /** Text area to display the chat history and messages */
    private TextArea chatArea;
    
    /** Socket connection to the chat server */
    private Socket socket;
    
    /** Output stream to send messages to the server */
    private ObjectOutputStream out;
    
    /** Input stream to receive messages from the server */
    private ObjectInputStream in;
    
    /** Flag indicating if connected to the chat server */
    private boolean isConnected = false;
    
    /** Thread for receiving messages asynchronously */
    private Thread receiveThread;
    
    /** Text field for entering messages */
    private TextField messageField;

    /**
     * Creates a new chat window between two users.
     * 
     * @param currentUser The user who initiated the chat
     * @param chatPartner The user to chat with
     */
    public ChatWindow(User currentUser, User chatPartner) {
        this.currentUser = currentUser;
        this.chatPartner = chatPartner;
        this.stage = new Stage();
    }

    /**
     * Initializes and displays the chat window interface.
     * Sets up UI components, loads chat history, and connects to the chat server.
     */
    public void show() {
        stage.setTitle("Chat with " + chatPartner.getName());

        // Create UI components
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPrefHeight(350);
        messageField = new TextField();
        TextField messageField = new TextField();
        messageField.setPromptText("Type your message here...");
        messageField.setPrefHeight(30);

        Button sendButton = new Button("Send");
        sendButton.setPrefHeight(30);
        
        // Status indicator
        Label statusLabel = new Label("Disconnected");
        statusLabel.setStyle("-fx-text-fill: red;");

        // Layout
        HBox inputBox = new HBox(5, messageField, sendButton);
        inputBox.setPadding(new Insets(5));
        HBox.setHgrow(messageField, Priority.ALWAYS);

        HBox statusBox = new HBox(5, new Label("Status:"), statusLabel);
        statusBox.setPadding(new Insets(5));
        statusBox.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(5, statusBox, chatArea, inputBox);
        root.setPadding(new Insets(10));
        VBox.setVgrow(chatArea, Priority.ALWAYS);

        // Event handlers
        sendButton.setOnAction(e -> sendMessage(messageField.getText()));
        messageField.setOnAction(e -> sendMessage(messageField.getText()));

        // Show window
        Scene scene = new Scene(root, 400, 500);
        stage.setScene(scene);
        // Replace the line causing the error (around line 304)
        String cssPath = com.rpms.Main.getStylesheetPath();
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        }

        stage.show();

        // Load chat history - IMPORTANT: Do this first
        loadChatHistory();

        // Connect to chat server
        connectToServer(statusLabel);
        
        // Handle window close
        stage.setOnCloseRequest(e -> disconnect());
    }

    /**
     * Loads and displays the previous chat history between these users.
     * Retrieves messages from the DataManager and adds them to the chat area.
     */
    private void loadChatHistory() {
        // Clear the chat area first
        chatArea.clear();
        
        // Get fresh chat history directly
        ChatHistory history = DataManager.getChatHistory(currentUser.getId(), chatPartner.getId());
        
        if (history.getMessages().isEmpty()) {
            chatArea.appendText("No previous messages.\n");
            return;
        }
        
        // Add all messages to the chat area
        for (ChatMessage message : history.getMessages()) {
            appendMessage(message);
        }
        
        // Log messages found
        System.out.println("Loaded " + history.getMessages().size() + " messages for chat between " + 
                          currentUser.getId() + " and " + chatPartner.getId());
    }

    /**
     * Connects to the chat server in a separate thread.
     * Updates the status label to show connection state.
     * 
     * @param statusLabel Label to update with connection status
     */
    private void connectToServer(Label statusLabel) {
        new Thread(() -> {
            try {
                // Connect to the server with the current port
                int port = ChatManager.getCurrentPort();
                socket = new Socket("localhost", port);
                
                // Create streams in correct order to avoid deadlock
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());
                
                // Send user ID to identify this connection
                out.writeObject(currentUser.getId());
                out.flush();
                
                isConnected = true;
                
                Platform.runLater(() -> {
                    statusLabel.setText("Connected");
                    statusLabel.setStyle("-fx-text-fill: green;");
                });
                
                // Start thread to receive messages
                receiveMessages();
                
            } catch (IOException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Connection Failed");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Connection Error");
                    alert.setHeaderText("Failed to connect to chat server");
                    alert.setContentText("Please try again later: " + e.getMessage());
                    alert.show();
                });
                System.err.println("Error connecting to chat server: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Starts a separate thread to receive incoming messages.
     * Continuously listens for messages from the chat partner.
     */
    private void receiveMessages() {
        receiveThread = new Thread(() -> {
            try {
                while (isConnected) {
                    Object obj = in.readObject();
                    
                    if (obj instanceof ChatMessage) {
                        ChatMessage message = (ChatMessage) obj;
                        
                        // Only process messages from our chat partner
                        if (message.getSenderId().equals(chatPartner.getId())) {
                            Platform.runLater(() -> appendMessage(message));
                        }
                    }
                }
            } catch (SocketException e) {
                // Socket closed, normal disconnect
                if (isConnected) {
                    System.out.println("Chat connection closed");
                }
            } catch (EOFException e) {
                // End of stream, normal disconnect
                if (isConnected) {
                    System.out.println("Chat connection ended");
                }
            } catch (IOException | ClassNotFoundException e) {
                if (isConnected) {
                    System.err.println("Error receiving messages: " + e.getMessage());
                }
            } finally {
                disconnect();
            }
        });
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    /**
     * Sends a message to the chat partner.
     * Creates a ChatMessage object and sends it to the server.
     * 
     * @param content The text content of the message to send
     */
    private void sendMessage(String content) {
        if (content == null || content.trim().isEmpty()) return;
        
        if (!isConnected) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Not Connected");
            alert.setHeaderText("You are not connected to the chat server");
            alert.setContentText("Please try again later");
            alert.show();
            return;
        }
        
        try {
            // Create message object
            ChatMessage message = new ChatMessage(
                currentUser.getId(),
                currentUser.getName(),
                chatPartner.getId(),
                content
            );
            
            // Send message to server
            out.writeObject(message);
            out.flush();
            out.reset(); // Important: reset object stream cache
            
            // Add to UI
            appendMessage(message);
            
            // Clear input field
            messageField.clear();
            
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Send Error");
            alert.setHeaderText("Failed to send message");
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    /**
     * Adds a message to the chat area with appropriate formatting.
     * Different formatting is applied for sent vs. received messages.
     * 
     * @param message The ChatMessage to display
     */
    private void appendMessage(ChatMessage message) {
        String formattedMessage;
        if (message.getSenderId().equals(currentUser.getId())) {
            // This is a message sent by the current user
            formattedMessage = "You: " + message.getContent() + "\n";
        } else {
            // This is a message received from the chat partner
            formattedMessage = chatPartner.getName() + ": " + message.getContent() + "\n";
        }
        chatArea.appendText(formattedMessage);
    }

    /**
     * Disconnects from the chat server and cleans up resources.
     * Called when the chat window is closed or connection is lost.
     */
    private void disconnect() {
        if (!isConnected) return;
        
        isConnected = false;
        
        try {
            if (receiveThread != null) {
                receiveThread.interrupt();
            }
            
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
            
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }
}
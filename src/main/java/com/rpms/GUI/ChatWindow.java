package com.rpms.GUI; // GUI components package for the application

// Import chat system and user management related classes
import com.rpms.ChatAndVideoConsultation.ChatManager;
import com.rpms.ChatAndVideoConsultation.ChatMessage;
import com.rpms.ChatAndVideoConsultation.ChatHistory;
import com.rpms.UserManagement.User;
import com.rpms.utilities.DataManager;

// JavaFX UI imports
import javafx.application.Platform; // For updating UI from background threads
import javafx.geometry.Insets; // For UI padding and spacing
import javafx.geometry.Pos; // For alignment of UI elements
import javafx.scene.Scene; // Container for all JavaFX content
import javafx.scene.control.*; // UI control elements like buttons and text fields
import javafx.scene.layout.*; // Layout containers for organizing UI elements
import javafx.stage.Stage; // Top-level JavaFX container (window)

// Java IO and networking imports
import java.io.*; // For input/output streams
import java.net.Socket; // For network communication
import java.net.SocketException; // For handling socket errors

/**
 * Provides a chat interface for communication between doctors and patients.
 * Handles connection to the chat server, message sending/receiving, and chat history.
 */
public class ChatWindow {
    /** The current user (patient or doctor) using this chat window */
    private User currentUser; // Stores reference to the user who opened this chat
    
    /** The user the current user is chatting with */
    private User chatPartner; // Stores reference to the conversation partner
    
    /** The window stage for this chat */
    private Stage stage; // JavaFX window container for this chat interface
    
    /** Text area to display the chat history and messages */
    private TextArea chatArea; // Displays all chat messages
    
    /** Socket connection to the chat server */
    private Socket socket; // Network connection to the chat server
    
    /** Output stream to send messages to the server */
    private ObjectOutputStream out; // Serializes and sends Java objects to server
    
    /** Input stream to receive messages from the server */
    private ObjectInputStream in; // Receives and deserializes Java objects from server
    
    /** Flag indicating if connected to the chat server */
    private boolean isConnected = false; // Tracks connection state
    
    /** Thread for receiving messages asynchronously */
    private Thread receiveThread; // Background thread that listens for incoming messages
    
    /** Text field for entering messages */
    private TextField messageField; // Where user types new messages

    /**
     * Creates a new chat window between two users.
     * 
     * @param currentUser The user who initiated the chat
     * @param chatPartner The user to chat with
     */
    public ChatWindow(User currentUser, User chatPartner) {
        this.currentUser = currentUser; // Set the current user
        this.chatPartner = chatPartner; // Set the chat partner
        this.stage = new Stage(); // Create a new window
    }

    /**
     * Initializes and displays the chat window interface.
     * Sets up UI components, loads chat history, and connects to the chat server.
     */
    public void show() {
        stage.setTitle("Chat with " + chatPartner.getName()); // Set window title with partner's name

        // Create UI components
        chatArea = new TextArea(); // Create multi-line text display area
        chatArea.setEditable(false); // Prevent user from editing message history
        chatArea.setWrapText(true); // Enable text wrapping for long messages
        chatArea.setPrefHeight(350); // Set preferred height for chat display
        messageField = new TextField(); // Create field for message input
        TextField messageField = new TextField(); // Create a new TextField (note: this duplicates the field above)
        messageField.setPromptText("Type your message here..."); // Set placeholder text
        messageField.setPrefHeight(30); // Set preferred height for input field

        Button sendButton = new Button("Send"); // Create button to send messages
        sendButton.setPrefHeight(30); // Set preferred height for button
        
        // Status indicator
        Label statusLabel = new Label("Disconnected"); // Create label showing connection status
        statusLabel.setStyle("-fx-text-fill: red;"); // Set text color to red for disconnected state

        // Layout
        HBox inputBox = new HBox(5, messageField, sendButton); // Horizontal container for input field and send button
        inputBox.setPadding(new Insets(5)); // Add padding around the input area
        HBox.setHgrow(messageField, Priority.ALWAYS); // Allow input field to grow horizontally

        HBox statusBox = new HBox(5, new Label("Status:"), statusLabel); // Horizontal container for status display
        statusBox.setPadding(new Insets(5)); // Add padding around status area
        statusBox.setAlignment(Pos.CENTER_LEFT); // Align status to the left

        VBox root = new VBox(5, statusBox, chatArea, inputBox); // Vertical container for all UI elements
        root.setPadding(new Insets(10)); // Add padding around all content
        VBox.setVgrow(chatArea, Priority.ALWAYS); // Allow chat area to grow vertically

        // Event handlers
        sendButton.setOnAction(e -> sendMessage(messageField.getText())); // Send message when button clicked
        messageField.setOnAction(e -> sendMessage(messageField.getText())); // Send message when Enter pressed

        // Show window
        Scene scene = new Scene(root, 400, 500); // Create scene with root container and dimensions
        stage.setScene(scene); // Set the scene in the window
        // Replace the line causing the error (around line 304)
        String cssPath = com.rpms.Main.getStylesheetPath(); // Get CSS styling path
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath); // Apply CSS styling if available
        }

        stage.show(); // Display the window

        // Load chat history - IMPORTANT: Do this first
        loadChatHistory(); // Load previous messages before connecting

        // Connect to chat server
        connectToServer(statusLabel); // Start connection to chat server
        
        // Handle window close
        stage.setOnCloseRequest(e -> disconnect()); // Clean up when window is closed
    }

    /**
     * Loads and displays the previous chat history between these users.
     * Retrieves messages from the DataManager and adds them to the chat area.
     */
    private void loadChatHistory() {
        // Clear the chat area first
        chatArea.clear(); // Remove any existing content
        
        // Get fresh chat history directly
        ChatHistory history = DataManager.getChatHistory(currentUser.getId(), chatPartner.getId()); // Retrieve stored messages
        
        if (history.getMessages().isEmpty()) {
            chatArea.appendText("No previous messages.\n"); // Show message if no history exists
            return;
        }
        
        // Add all messages to the chat area
        for (ChatMessage message : history.getMessages()) {
            appendMessage(message); // Display each message with proper formatting
        }
        
        // Log messages found
        System.out.println("Loaded " + history.getMessages().size() + " messages for chat between " + 
                          currentUser.getId() + " and " + chatPartner.getId()); // Log number of messages loaded
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
                int port = ChatManager.getCurrentPort(); // Get port number from chat manager
                socket = new Socket("localhost", port); // Connect to server on localhost
                
                // Create streams in correct order to avoid deadlock
                out = new ObjectOutputStream(socket.getOutputStream()); // Create output stream first
                out.flush(); // Flush header information
                in = new ObjectInputStream(socket.getInputStream()); // Then create input stream
                
                // Send user ID to identify this connection
                out.writeObject(currentUser.getId()); // Tell server who is connecting
                out.flush(); // Ensure ID is sent immediately
                
                isConnected = true; // Update connection state
                
                Platform.runLater(() -> {
                    statusLabel.setText("Connected"); // Update UI to show connected state
                    statusLabel.setStyle("-fx-text-fill: green;"); // Change status color to green
                });
                
                // Start thread to receive messages
                receiveMessages(); // Begin listening for incoming messages
                
            } catch (IOException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Connection Failed"); // Update UI to show failure
                    Alert alert = new Alert(Alert.AlertType.ERROR); // Create error popup
                    alert.setTitle("Connection Error"); // Set popup title
                    alert.setHeaderText("Failed to connect to chat server"); // Set main error message
                    alert.setContentText("Please try again later: " + e.getMessage()); // Show error details
                    alert.show(); // Display the error popup
                });
                System.err.println("Error connecting to chat server: " + e.getMessage()); // Log the error
            }
        }).start(); // Start this process in a background thread
    }

    /**
     * Starts a separate thread to receive incoming messages.
     * Continuously listens for messages from the chat partner.
     */
    private void receiveMessages() {
        receiveThread = new Thread(() -> {
            try {
                while (isConnected) {
                    Object obj = in.readObject(); // Wait for and read next object from server
                    
                    if (obj instanceof ChatMessage) {
                        ChatMessage message = (ChatMessage) obj; // Cast to chat message type
                        
                        // Only process messages from our chat partner
                        if (message.getSenderId().equals(chatPartner.getId())) {
                            Platform.runLater(() -> appendMessage(message)); // Update UI on JavaFX thread
                        }
                    }
                }
            } catch (SocketException e) {
                // Socket closed, normal disconnect
                if (isConnected) {
                    System.out.println("Chat connection closed"); // Log normal disconnection
                }
            } catch (EOFException e) {
                // End of stream, normal disconnect
                if (isConnected) {
                    System.out.println("Chat connection ended"); // Log normal stream end
                }
            } catch (IOException | ClassNotFoundException e) {
                if (isConnected) {
                    System.err.println("Error receiving messages: " + e.getMessage()); // Log unexpected errors
                }
            } finally {
                disconnect(); // Clean up resources
            }
        });
        receiveThread.setDaemon(true); // Make thread terminate when main app closes
        receiveThread.start(); // Start the receiving thread
    }

    /**
     * Sends a message to the chat partner.
     * Creates a ChatMessage object and sends it to the server.
     * 
     * @param content The text content of the message to send
     */
    private void sendMessage(String content) {
        if (content == null || content.trim().isEmpty()) return; // Don't send empty messages
        
        if (!isConnected) {
            Alert alert = new Alert(Alert.AlertType.WARNING); // Create warning popup
            alert.setTitle("Not Connected"); // Set popup title
            alert.setHeaderText("You are not connected to the chat server"); // Set main warning message
            alert.setContentText("Please try again later"); // Set additional details
            alert.show(); // Display warning
            return;
        }
        
        try {
            // Create message object
            ChatMessage message = new ChatMessage(
                currentUser.getId(), // Sender ID
                currentUser.getName(), // Sender name
                chatPartner.getId(), // Recipient ID
                content // Message text
            );
            
            // Send message to server
            out.writeObject(message); // Serialize and send message object
            out.flush(); // Ensure message is sent immediately
            out.reset(); // Important: reset object stream cache
            
            // Add to UI
            appendMessage(message); // Display sent message in chat area
            
            // Clear input field
            messageField.clear(); // Remove sent text from input field
            
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage()); // Log error
            Alert alert = new Alert(Alert.AlertType.ERROR); // Create error popup
            alert.setTitle("Send Error"); // Set popup title
            alert.setHeaderText("Failed to send message"); // Set main error message
            alert.setContentText(e.getMessage()); // Show error details
            alert.show(); // Display error
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
            formattedMessage = "You: " + message.getContent() + "\n"; // Format outgoing messages
        } else {
            // This is a message received from the chat partner
            formattedMessage = chatPartner.getName() + ": " + message.getContent() + "\n"; // Format incoming messages
        }
        chatArea.appendText(formattedMessage); // Add the formatted message to the chat display
    }

    /**
     * Disconnects from the chat server and cleans up resources.
     * Called when the chat window is closed or connection is lost.
     */
    private void disconnect() {
        if (!isConnected) return; // Don't disconnect if already disconnected
        
        isConnected = false; // Update connection state
        
        try {
            if (receiveThread != null) {
                receiveThread.interrupt(); // Stop the message receiving thread
            }
            
            if (out != null) out.close(); // Close output stream
            if (in != null) in.close(); // Close input stream
            if (socket != null && !socket.isClosed()) socket.close(); // Close network socket
            
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage()); // Log cleanup errors
        }
    }
}
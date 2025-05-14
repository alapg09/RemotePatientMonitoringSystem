package com.rpms.ChatAndVideoConsultation; // Package for chat and video consultation functionality

// Import necessary classes for user management
import com.rpms.UserManagement.Administrator;
import com.rpms.UserManagement.Doctor;
import com.rpms.UserManagement.Patient;
import com.rpms.UserManagement.User;
import com.rpms.utilities.DataManager; // For persistent data storage

// Import Java IO and networking classes
import java.io.*;
import java.net.BindException; // For handling port binding issues
import java.net.ConnectException; // For handling connection failures
import java.net.ServerSocket; // For creating the server endpoint
import java.net.Socket; // For client-server connections
import java.net.SocketException; // For handling socket errors
import java.util.ArrayList; // For storing lists of users
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap; // Thread-safe map implementation
import java.util.concurrent.ExecutorService; // For managing thread execution
import java.util.concurrent.Executors; // For creating thread pools

/**
 * Central manager for the real-time chat system.
 * Implements a TCP socket-based chat server that handles multiple concurrent client connections,
 * message routing, and integration with the persistent data storage.
 */
public class ChatManager {
    /** Base port to try first when starting server */
    private static final int BASE_PORT = 12345; // Starting port number to attempt
    
    /** Current port the server is running on */
    private static int currentPort = BASE_PORT; // Tracks which port is currently in use
    
    /** The server socket accepting client connections */
    private static ServerSocket serverSocket; // Listens for incoming client connections
    
    /** Flag indicating if the server is currently running */
    private static boolean isRunning = false; // Controls server operation state
    
    /** Map of connected users: user ID → ClientHandler */
    private static final Map<String, ClientHandler> connectedUsers = new ConcurrentHashMap<>(); // Thread-safe map for active users
    
    /** Thread pool for handling multiple client connections concurrently */
    private static ExecutorService threadPool; // Manages threads for client handlers
    
    /**
     * Starts the chat server on an available port.
     * Tries multiple ports if the default port is already in use.
     * Creates a thread pool for handling client connections.
     */
    public static void startServer() {
        if (isRunning) return; // Prevents starting server twice
        
        // Try up to 10 ports
        for (int portOffset = 0; portOffset < 10; portOffset++) { // Attempt multiple ports if needed
            currentPort = BASE_PORT + portOffset; // Calculate next port to try
            try {
                serverSocket = new ServerSocket(currentPort); // Attempt to create server socket on this port
                isRunning = true; // Mark server as running
                threadPool = Executors.newCachedThreadPool(); // Create flexible thread pool that grows as needed
                
                // Start a thread to listen for incoming connections
                new Thread(() -> { // Anonymous thread for accepting connections
                    try {
                        System.out.println("Chat server started on port " + currentPort);
                        while (isRunning) { // Continue while server should be running
                            try {
                                Socket clientSocket = serverSocket.accept(); // Block until client connects
                                ClientHandler handler = new ClientHandler(clientSocket); // Create handler for this client
                                threadPool.execute(handler); // Submit handler to thread pool
                            } catch (IOException e) {
                                if (isRunning) { // Only log errors if server should be running
                                    System.err.println("Error accepting client connection: " + e.getMessage());
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (isRunning) { // Only log errors if server should be running
                            System.err.println("Server thread error: " + e.getMessage());
                        }
                    }
                }).start(); // Start the connection acceptance thread
                
                // Successfully started server
                return; // Exit method after successful start
                
            } catch (BindException e) {
                // Port is in use, try next one
                System.out.println("Port " + currentPort + " is in use, trying next port...");
            } catch (IOException e) {
                System.err.println("Could not start chat server on port " + currentPort + ": " + e.getMessage());
            }
        }
        
        // If we got here, all ports were in use
        System.err.println("Failed to start chat server after trying multiple ports");
    }
    
    /**
     * Returns the current port the server is running on.
     * Used by clients to connect to the server.
     * 
     * @return The current server port
     */
    public static int getCurrentPort() {
        return currentPort; // Provides port number to clients
    }
    
    /**
     * Stops the chat server and releases all resources.
     * Closes all client connections and shuts down the thread pool.
     */
    public static void stopServer() {
        isRunning = false; // Signal that the server should stop
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // Close main server socket
            }
            
            if (threadPool != null) {
                threadPool.shutdown(); // Shut down the thread pool gracefully
            }
            
            // Close all client connections
            for (ClientHandler handler : connectedUsers.values()) { // Iterate through all connected clients
                handler.closeConnection(); // Close each connection
            }
            connectedUsers.clear(); // Remove all entries from the map
            
        } catch (IOException e) {
            System.err.println("Error closing server: " + e.getMessage());
        }
    }
    
    /**
     * Forces the release of the server port if it's in use.
     * Attempts to connect and immediately close to trigger port release.
     * This is useful when the server didn't shut down properly previously.
     */
    public static void forceReleasePort() {
        try {
            Socket socket = new Socket("localhost", currentPort); // Try to connect to the port
            socket.close(); // Close connection immediately
            System.out.println("Successfully connected to port " + currentPort + ", it appears to be in use.");
        } catch (ConnectException e) {
            // Port is not in use, which is good
            System.out.println("Port " + currentPort + " is available.");
        } catch (IOException e) {
            System.out.println("Error checking port " + currentPort + ": " + e.getMessage());
        }
    }
    
    /**
     * Inner class that handles individual client connections.
     * Each connected client gets its own ClientHandler instance.
     */
    private static class ClientHandler implements Runnable { // Implements Runnable for thread pool execution
        /** The socket connection to this client */
        private Socket clientSocket; // Socket for communication with this client
        
        /** Stream for receiving objects from the client */
        private ObjectInputStream in; // Deserializes objects from client
        
        /** Stream for sending objects to the client */
        private ObjectOutputStream out; // Serializes objects to client
        
        /** ID of the user this handler is serving */
        private String userId; // Identifies which user this handler represents
        
        /** Flag indicating if this handler is running */
        private boolean running = true; // Controls handler operation state
        
        /**
         * Creates a new client handler for the specified socket.
         * 
         * @param socket Client's socket connection
         */
        public ClientHandler(Socket socket) {
            this.clientSocket = socket; // Store socket for this client
        }
        
        /**
         * Closes the connection to the client.
         * Called when the server is shutting down or the client disconnects.
         */
        public void closeConnection() {
            running = false; // Signal handler to stop
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close(); // Close the socket
                }
            } catch (IOException e) {
                System.err.println("Error closing client connection: " + e.getMessage());
            }
        }
        
        /**
         * Main processing loop for this client connection.
         * Sets up streams, authenticates the user, and processes messages.
         */
        @Override
        public void run() {
            try {
                // Create streams - ORDER IS IMPORTANT to avoid deadlock
                out = new ObjectOutputStream(clientSocket.getOutputStream()); // Must create output stream first
                out.flush(); // Important to avoid deadlock
                in = new ObjectInputStream(clientSocket.getInputStream()); // Then create input stream
                
                // First message should be the user ID
                Object userIdObj = in.readObject(); // Read user identification
                if (userIdObj instanceof String) { // Verify correct type
                    userId = (String) userIdObj; // Cast and store user ID
                    connectedUsers.put(userId, this); // Register this handler in the connected users map
                    
                    System.out.println("User " + userId + " connected to chat server");
                    
                    // Process incoming messages
                    while (running && isRunning && !clientSocket.isClosed()) { // Loop until server stops, handler stops, or socket closes
                        try {
                            Object obj = in.readObject(); // Read next object from client
                            if (obj instanceof ChatMessage) { // Check if it's a chat message
                                ChatMessage message = (ChatMessage) obj; // Cast to proper type
                                processMessage(message); // Process the message
                            }
                        } catch (EOFException | SocketException e) {
                            // Client disconnected
                            break; // Exit the message processing loop
                        } catch (ClassNotFoundException e) {
                            System.err.println("Unknown message type received: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("I/O Error with client " + userId + ": " + e.getMessage());
            } catch (ClassNotFoundException e) {
                System.err.println("Error reading object from client: " + e.getMessage());
            } finally {
                if (userId != null) {
                    connectedUsers.remove(userId); // Remove from active users map
                    System.out.println("User " + userId + " disconnected from chat server");
                }
                
                // Close resources
                try {
                    if (in != null) in.close(); // Close input stream
                    if (out != null) out.close(); // Close output stream
                    if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close(); // Close socket
                } catch (IOException e) {
                    System.err.println("Error closing client resources: " + e.getMessage());
                }
            }
        }
        
        /**
         * Processes a chat message received from the client.
         * Saves the message to persistent storage and routes it to the recipient if online.
         * 
         * @param message The chat message to process
         */
        private void processMessage(ChatMessage message) {
            try {
                // CRITICAL: Save the message to persistent storage FIRST
                DataManager.addChatMessage(message); // Ensure message is stored permanently
                System.out.println("Message saved: from " + message.getSenderId() + " to " + message.getReceiverId());
                
                // Send to recipient if online
                String recipientId = message.getReceiverId(); // Get recipient's ID
                ClientHandler recipient = connectedUsers.get(recipientId); // Look up recipient's handler
                
                if (recipient != null) {
                    recipient.sendMessage(message); // Forward message to recipient if online
                }
                
                // Confirm receipt to sender
                out.writeObject("DELIVERED"); // Send confirmation to original sender
                out.flush(); // Ensure data is sent immediately
                out.reset(); // Reset object cache to avoid memory leaks
            } catch (IOException e) {
                System.err.println("Error sending delivery confirmation: " + e.getMessage());
            }
        }
        
        /**
         * Sends a message to this client.
         * 
         * @param message The message to send
         */
        public void sendMessage(ChatMessage message) {
            try {
                out.writeObject(message); // Serialize and send message
                out.flush(); // Ensure message is sent immediately
                out.reset(); // Reset object cache to avoid memory leaks
            } catch (IOException e) {
                System.err.println("Error sending message to recipient: " + e.getMessage());
                closeConnection(); // Close this connection if it's broken
            }
        }
    }
    
    /**
     * Gets a user's name by their ID.
     * Searches through all registered users in the system.
     * 
     * @param userId ID of the user to look up
     * @return User's name, or "Unknown User" if not found
     */
    public static String getUserNameById(String userId) {
        for (User user : Administrator.getAllUsers()) { // Search through all system users
            if (user.getId().equals(userId)) { // Compare IDs
                return user.getName(); // Return name if found
            }
        }
        return "Unknown User"; // Default if user not found
    }
    
    /**
     * Gets all users that a specific user can chat with.
     * For patients: their assigned doctor and any doctors who have them as patients.
     * For doctors: all their assigned patients.
     * 
     * @param user The user to find chat contacts for
     * @return List of users that can be contacted
     */
    public static List<User> getChatContactsForUser(User user) {
        List<User> contacts = new ArrayList<>(); // List to hold allowed chat contacts
        
        if (user instanceof Patient patient) { // Check if user is a patient (using Java 16+ pattern matching)
            // 1. Add their physician
            Doctor physician = patient.getPhysician(); // Get patient's primary doctor
            if (physician != null) {
                contacts.add(physician); // Add primary doctor to contacts
            }
            
            // 2. Add all doctors who have this patient in their list
            for (Doctor doctor : Administrator.getDoctors()) { // Iterate through all doctors
                if (doctor.getPatients().contains(patient) && !contacts.contains(doctor)) { // Check if doctor has this patient and isn't already added
                    contacts.add(doctor); // Add doctor to contacts
                }
            }
        } else if (user instanceof Doctor doctor) { // Check if user is a doctor
            // Doctors can chat with all their patients
            contacts.addAll(doctor.getPatients()); // Add all patients assigned to this doctor
        }
        
        return contacts; // Return list of allowed contacts
    }
}
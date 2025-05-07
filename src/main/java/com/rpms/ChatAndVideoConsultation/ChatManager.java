package com.rpms.ChatAndVideoConsultation;

import com.rpms.UserManagement.Administrator;
import com.rpms.UserManagement.Doctor;
import com.rpms.UserManagement.Patient;
import com.rpms.UserManagement.User;
import com.rpms.utilities.DataManager;

import java.io.*;
import java.net.BindException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatManager {
    private static final int BASE_PORT = 12345;
    private static int currentPort = BASE_PORT;
    private static ServerSocket serverSocket;
    private static boolean isRunning = false;
    private static final Map<String, ClientHandler> connectedUsers = new ConcurrentHashMap<>();
    private static ExecutorService threadPool;
    
    // Start the chat server
    public static void startServer() {
        if (isRunning) return;
        
        // Try up to 10 ports
        for (int portOffset = 0; portOffset < 10; portOffset++) {
            currentPort = BASE_PORT + portOffset;
            try {
                serverSocket = new ServerSocket(currentPort);
                isRunning = true;
                threadPool = Executors.newCachedThreadPool();
                
                // Start a thread to listen for incoming connections
                new Thread(() -> {
                    try {
                        System.out.println("Chat server started on port " + currentPort);
                        while (isRunning) {
                            try {
                                Socket clientSocket = serverSocket.accept();
                                ClientHandler handler = new ClientHandler(clientSocket);
                                threadPool.execute(handler);
                            } catch (IOException e) {
                                if (isRunning) {
                                    System.err.println("Error accepting client connection: " + e.getMessage());
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (isRunning) {
                            System.err.println("Server thread error: " + e.getMessage());
                        }
                    }
                }).start();
                
                // Successfully started server
                return;
                
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
    
    // Get the current port (for clients to connect)
    public static int getCurrentPort() {
        return currentPort;
    }
    
    // Stop the chat server
    public static void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            if (threadPool != null) {
                threadPool.shutdown();
            }
            
            // Close all client connections
            for (ClientHandler handler : connectedUsers.values()) {
                handler.closeConnection();
            }
            connectedUsers.clear();
            
        } catch (IOException e) {
            System.err.println("Error closing server: " + e.getMessage());
        }
    }
    
    // Client handler for each connected client
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private String userId;
        private boolean running = true;
        
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }
        
        public void closeConnection() {
            running = false;
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing client connection: " + e.getMessage());
            }
        }
        
        @Override
        public void run() {
            try {
                // Create streams - ORDER IS IMPORTANT to avoid deadlock
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                out.flush(); // Important to avoid deadlock
                in = new ObjectInputStream(clientSocket.getInputStream());
                
                // First message should be the user ID
                Object userIdObj = in.readObject();
                if (userIdObj instanceof String) {
                    userId = (String) userIdObj;
                    connectedUsers.put(userId, this);
                    
                    System.out.println("User " + userId + " connected to chat server");
                    
                    // Process incoming messages
                    while (running && isRunning && !clientSocket.isClosed()) {
                        try {
                            Object obj = in.readObject();
                            if (obj instanceof ChatMessage) {
                                ChatMessage message = (ChatMessage) obj;
                                processMessage(message);
                            }
                        } catch (EOFException | SocketException e) {
                            // Client disconnected
                            break;
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
                    connectedUsers.remove(userId);
                    System.out.println("User " + userId + " disconnected from chat server");
                }
                
                // Close resources
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing client resources: " + e.getMessage());
                }
            }
        }
        
        private void processMessage(ChatMessage message) {
            try {
                // CRITICAL FIX: Save the message to persistent storage FIRST
                DataManager.addChatMessage(message);
                System.out.println("Message saved: from " + message.getSenderId() + " to " + message.getReceiverId());
                
                // Send to recipient if online
                String recipientId = message.getReceiverId();
                ClientHandler recipient = connectedUsers.get(recipientId);
                
                if (recipient != null) {
                    recipient.sendMessage(message);
                }
                
                // Confirm receipt to sender
                out.writeObject("DELIVERED");
                out.flush();
                out.reset(); // Reset object cache to avoid memory leaks
            } catch (IOException e) {
                System.err.println("Error sending delivery confirmation: " + e.getMessage());
            }
        }
        
        public void sendMessage(ChatMessage message) {
            try {
                out.writeObject(message);
                out.flush();
                out.reset(); // Reset object cache to avoid memory leaks
            } catch (IOException e) {
                System.err.println("Error sending message to recipient: " + e.getMessage());
                closeConnection(); // Close this connection if it's broken
            }
        }
    }
    
    // Method to get user name by ID
    public static String getUserNameById(String userId) {
        for (User user : Administrator.getAllUsers()) {
            if (user.getId().equals(userId)) {
                return user.getName();
            }
        }
        return "Unknown User";
    }
    
    // Method to get all chat contacts for a user
    public static List<User> getChatContactsForUser(User user) {
        List<User> contacts = new ArrayList<>();
        
        if (user instanceof Patient patient) {
            // 1. Add their physician
            Doctor physician = patient.getPhysician();
            if (physician != null) {
                contacts.add(physician);
            }
            
            // 2. Add all doctors who have this patient in their list
            for (Doctor doctor : Administrator.getDoctors()) {
                if (doctor.getPatients().contains(patient) && !contacts.contains(doctor)) {
                    contacts.add(doctor);
                }
            }
        } else if (user instanceof Doctor doctor) {
            // Doctors can chat with all their patients
            contacts.addAll(doctor.getPatients());
        }
        
        return contacts;
    }

    /**
     * Attempts to forcibly release the chat server port
     */
    public static void forceReleasePort() {
        try {
            // Try to connect to the port to check if it's in use
            Socket socket = new Socket("localhost", BASE_PORT);
            // If we got here, the port is in use
            socket.close();
            
            // Use operating system commands to kill processes using the port
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                // Windows
                Runtime.getRuntime().exec("netstat -ano | findstr :" + BASE_PORT);
                // Note: You would need to parse the output to get the PID and then kill it
                // But this is complex and potentially dangerous
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                // Linux/Unix/Mac
                Runtime.getRuntime().exec("lsof -i :" + BASE_PORT + " | grep LISTEN");
                // Similarly, would need to parse the output
            }
        } catch (ConnectException e) {
            // Port is not in use, which is good
        } catch (Exception e) {
            System.err.println("Could not check port status: " + e.getMessage());
        }
    }
}
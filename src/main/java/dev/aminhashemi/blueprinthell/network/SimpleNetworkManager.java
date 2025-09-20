package dev.aminhashemi.blueprinthell.network;

import com.google.gson.Gson;
import dev.aminhashemi.blueprinthell.model.SaveData;
import dev.aminhashemi.blueprinthell.utils.SaveManager;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simple network manager that wraps the existing game without changing it
 */
public class SimpleNetworkManager {
    
    private final boolean isServer;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread networkThread;
    
    public SimpleNetworkManager(boolean isServer) {
        this.isServer = isServer;
    }
    
    public boolean start() {
        try {
            if (isServer) {
                return startServer();
            } else {
                return startClient();
            }
        } catch (Exception e) {
            System.err.println("Network error: " + e.getMessage());
            return false;
        }
    }
    
    private boolean startServer() throws IOException {
        serverSocket = new ServerSocket(8080);
        System.out.println("Server started on port 8080");
        
        running.set(true);
        networkThread = new Thread(() -> {
            try {
                while (running.get()) {
                    clientSocket = serverSocket.accept();
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                    
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    
                    // Handle client communication
                    handleClientCommunication();
                }
            } catch (IOException e) {
                if (running.get()) {
                    System.err.println("Server error: " + e.getMessage());
                }
            }
        });
        networkThread.start();
        return true;
    }
    
    private boolean startClient() throws IOException {
        clientSocket = new Socket("localhost", 8080);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        
        System.out.println("Connected to server");
        return true;
    }
    
    private void handleClientCommunication() {
        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null && running.get()) {
                // Echo back to client (simple ping-pong for now)
                out.println("ACK: " + inputLine);
            }
        } catch (IOException e) {
            System.err.println("Client communication error: " + e.getMessage());
        }
    }
    
    public void sendGameState(SaveData gameState) {
        if (out != null) {
            String json = SaveManager.saveDataToJson(gameState);
            out.println("GAME_STATE:" + json);
        }
    }
    
    public void sendInput(String input) {
        if (out != null) {
            out.println("INPUT:" + input);
        }
    }
    
    public void stop() {
        running.set(false);
        try {
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
            if (networkThread != null) networkThread.join(1000);
        } catch (Exception e) {
            System.err.println("Error stopping network: " + e.getMessage());
        }
    }
}

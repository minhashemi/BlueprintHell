package dev.aminhashemi.blueprinthell;

import dev.aminhashemi.blueprinthell.server.GameServer;
import dev.aminhashemi.blueprinthell.core.constants.GameConstants;
import dev.aminhashemi.blueprinthell.utils.Logger;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * CLI Server for BlueprintHell
 * Run this first, then run Main.java as client
 */
public class Server {
    
    public static void main(String[] args) {
        System.out.println("🌐 BlueprintHell Server Starting...");
        System.out.println("=" + "=".repeat(40));
        
        try {
            // Get server IP
            String serverIP = getLocalIPAddress();
            int serverPort = GameConstants.DEFAULT_SERVER_PORT;
            
            // Start the game server
            GameServer gameServer = new GameServer();
            gameServer.start();
            
            // Display server information
            System.out.println("✅ Server started successfully!");
            System.out.println("📡 Server IP: " + serverIP);
            System.out.println("🔌 Port: " + serverPort);
            System.out.println("⏳ Waiting for clients to connect...");
            System.out.println("📝 Client connections will be logged below:");
            System.out.println("=" + "=".repeat(50));
            
            // Keep server running
            System.out.println("Press Ctrl+C to stop the server");
            
            // Wait indefinitely
            while (true) {
                Thread.sleep(1000);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Gets local IP address
     */
    private static String getLocalIPAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isLoopback() && networkInterface.isUp()) {
                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();
                        if (!address.isLoopbackAddress() && address.getHostAddress().indexOf(':') == -1) {
                            return address.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.getInstance().error("Failed to get local IP address: " + e.getMessage());
        }
        return "localhost";
    }
}

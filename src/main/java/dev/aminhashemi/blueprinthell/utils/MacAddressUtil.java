package dev.aminhashemi.blueprinthell.utils;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Utility class for getting MAC address for user identification
 */
public class MacAddressUtil {
    
    /**
     * Gets the MAC address of the first available network interface
     * @return MAC address as string, or "unknown" if not found
     */
    public static String getMacAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                
                // Skip loopback and virtual interfaces
                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
                    continue;
                }
                
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null && mac.length == 6) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
                    }
                    return sb.toString();
                }
            }
        } catch (SocketException e) {
            Logger.getInstance().error("Failed to get MAC address", e);
        }
        
        return "unknown";
    }
    
    /**
     * Gets a sanitized MAC address suitable for use as filename
     * @return Sanitized MAC address
     */
    public static String getSanitizedMacAddress() {
        String mac = getMacAddress();
        return mac.replace(":", "_").replace("-", "_");
    }
}

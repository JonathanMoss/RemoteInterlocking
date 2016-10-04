package com.jgm.remoteinterlocking;

import com.jgm.remoteinterlocking.database.MySqlConnect;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class RemoteInterlocking {

    private static String riIdentity;
    private static int riIndex;
    private static int riPort;
    private static String riHostAddress;
    private static int iIndex;
    private static int iPort;
    private static String iHostAddress;
    public final static String NEW_LINE = System.lineSeparator();
    private final static String OPERATING_SYSTEM = System.getProperty("os.name");
    private static ResultSet rs;
    
    public static void main(String[] args) {
        // 1) Obtain the remote interlocking ID from the command line.
        RemoteInterlocking.sendStatusMessage("Obtaining Remote Interlocking Identity from the command line...", 
            false);
        if (args.length > 0) {
            RemoteInterlocking.riIdentity = args[0].trim();
        } else {
            RemoteInterlocking.sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                true);
            RemoteInterlocking.sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), "ERROR: Missing command line argument, cannot continue.", Colour.RESET.getColour()),
                true);
            System.exit(0);
        }
        
        // 2) Test if the remote interlocking ID is a String and between 5 - 15 characters A-Z, a-z, 0-9 and _.
        if (!RemoteInterlocking.riIdentity.matches("^[a-zA-Z_0-9_]{5,15}$")) {
            RemoteInterlocking.sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                true);
            RemoteInterlocking.sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), "ERROR: Invalid command line argument, cannot continue.", Colour.RESET.getColour()),
                true);
            System.exit(0);
        } else {
            RemoteInterlocking.sendStatusMessage(String.format ("%s%s%s",
                Colour.GREEN.getColour(), RemoteInterlocking.getOK(), Colour.RESET.getColour()),
                true);
        }
        
        // 3) Connect to the database and check if the identity passed on the command line is valid.
        try {
            RemoteInterlocking.rs = MySqlConnect.getDbCon().query(String.format ("SELECT * FROM Remote_Interlocking WHERE identity = '%s';", RemoteInterlocking.riIdentity));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        RemoteInterlocking.sendStatusMessage("Checking Remote DB for Remote Interlocking Credentials...", 
            false);
        try {
            RemoteInterlocking.rs.first();
            if (rs.getString("identity").equals(RemoteInterlocking.riIdentity)) {
                RemoteInterlocking.sendStatusMessage(String.format ("%s%s%s",
                Colour.GREEN.getColour(), RemoteInterlocking.getOK(), Colour.RESET.getColour()),
                true);
                RemoteInterlocking.riIndex = rs.getInt("index_key");
                RemoteInterlocking.riHostAddress = rs.getString("ip_address");
                RemoteInterlocking.riPort = rs.getInt("port_number");
            } 
        } catch (SQLException ex) {
            RemoteInterlocking.sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                true);
            RemoteInterlocking.sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), "ERROR: Invalid command line argument, cannot continue.", Colour.RESET.getColour()),
                true);
            System.exit(0);
        }
        
        try {
            // 4) Check IP address, then update if necessary.
            System.out.println("Here");
            if (!RemoteInterlocking.riHostAddress.equals(InetAddress.getLocalHost().getHostAddress())) {
                MySqlConnect.getDbCon().insert(String.format ("UPDATE `Remote_Interlocking` SET `ip_address` = '%s' WHERE `index_key` = '%s';",
                        InetAddress.getLocalHost().getHostAddress(), RemoteInterlocking.riIndex));
                System.out.println(String.format ("UPDATE `Remote_Interlocking` SET `ip_address` = '%s' WHERE `index_key` = '%s';",
                        InetAddress.getLocalHost().getHostAddress(), RemoteInterlocking.riIndex));
            } else {
                System.out.println(InetAddress.getLocalHost().getHostAddress() + ", " + RemoteInterlocking.riIndex);
            }
            // 5) Get the Interlocking Credentials from the Database.
        } catch (UnknownHostException | SQLException ex) {
            ex.printStackTrace();
        }
        
    }
    /**
     * This method returns a string representing 'OK' for display on the command line.
     * This method determines an appropriate indication based on the capabilities of the console.
     * 
     * @return A <code>String</code> representing 'OK' or a check mark.
     */
    public static String getOK() {
        if (OPERATING_SYSTEM.contains("Windows")) {
            return "OK";
        } else {
            return "[\u2713]";
        }
    }
    
     /**
     * This method returns a string representing 'FAILED' for display on the command line.
     * This method determines an appropriate indication based on the capabilities of the console.
     * 
     * @return A <code>String</code> representing 'FAILED' or a Cross.
     */
    public static String getFailed() {
        if (OPERATING_SYSTEM.contains("Windows")) {
            return "FAILED";
        } else {
            return "[\u2717]";
        }
    }
    /**
     * This method outputs to the console and Data Logger as required.
     * @param msg 
     * @param newLine 
     */
    public static void sendStatusMessage(String msg, Boolean newLine) {
        System.out.printf("%s%s", msg, (newLine) ? NEW_LINE : "");
    }
    
}

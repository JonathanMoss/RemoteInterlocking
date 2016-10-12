package com.jgm.remoteinterlocking;

import com.jgm.remoteinterlocking.assets.Points;
import com.jgm.remoteinterlocking.assets.PointsPosition;
import com.jgm.remoteinterlocking.database.MySqlConnect;
import com.jgm.remoteinterlocking.datalogger.DataLoggerClient;
import com.jgm.remoteinterlocking.linesidemoduleconnection.ListenForRequests;
import com.jgm.remoteinterlocking.linesidemoduleconnection.MessageHandler;
import com.jgm.remoteinterlocking.linesidemoduleconnection.MessageType;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class RemoteInterlocking {
    
    // Remote Interlocking variables
    private static String riIdentity;
    private static int riIndex;
    private static int riPort;
    private static String riHostAddress;
    
    // Interlocking variables
    private static int iIndex;
    private static int iPort;
    private static String iName;
    private static String iHostAddress;
    
    // DataLogger variables
    private static int dlPort;
    private static String dlHostAddress;
    private static boolean dataLoggerDetails = false;
    
    // LineSide Module Details
    private static final HashMap<String, Integer> LS_MOD = new HashMap<>();
    private static HashMap<String, Boolean> LinesideModuleSetupComplete = new HashMap<>();
    public static ListenForRequests lsModListen;
    
    // Database variables
    private static ResultSet rs;
    
    // General declarations.
    public final static String NEW_LINE = System.lineSeparator();
    private final static String OPERATING_SYSTEM = System.getProperty("os.name");
    
    // Data Logger variables.
    private static DataLoggerClient dataLogger;
    private static Boolean connectedToDL = false;
    
    // Points Variables.
    private static final ArrayList<Points> POINTS = new ArrayList<>();
    
    public static void main(String[] args) {
        
        
        
        String bannerMessage =  "************************************************************" + NEW_LINE +
                                "* Remote Interlocking v1.0 - October 2016 (c)JGM-NET.co.uk *" + NEW_LINE +
                                "************************************************************" + NEW_LINE;
        sendStatusMessage(bannerMessage, 
            true, false);
        sendStatusMessage("Running start-up script:", 
            true, false);
        
        // 1) Obtain the remote interlocking ID from the command line.
        sendStatusMessage("Obtaining Remote Interlocking Identity from the command line...", 
            false, false);
        if (args.length > 0) {
            riIdentity = args[0].trim();
        } else {
            sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                true, false);
            sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), "ERROR: Missing command line argument, cannot continue.", Colour.RESET.getColour()),
                true, false);
            System.exit(0);
        }
        
        // 2) Test if the remote interlocking ID is a String and between 5 - 15 characters A-Z, a-z, 0-9 and _.
        if (!riIdentity.matches("^[a-zA-Z_0-9_]{5,15}$")) {
            sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                true, false);
            sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), "ERROR: Invalid command line argument, cannot continue.", Colour.RESET.getColour()),
                true, false);
            System.exit(0);
        } else {
            sendStatusMessage(String.format ("%s%s%s",
                Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
                true, false);
        }
        
        // 3) Connect to the database and check if the identity passed on the command line is valid.
        try {
            rs = MySqlConnect.getDbCon().query(String.format ("SELECT * FROM Remote_Interlocking WHERE identity = '%s';", riIdentity));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        sendStatusMessage("Checking Remote DB for Remote Interlocking Credentials...", 
            false, false);
        try {
            rs.first();
            if (rs.getString("identity").equals(riIdentity)) {
                sendStatusMessage(String.format ("%s%s%s",
                    Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
                    false, false);
                riIndex = rs.getInt("index_key");
                riHostAddress = rs.getString("ip_address");
                riPort = rs.getInt("port_number");
                iIndex = rs.getInt("interlocking_index");
                sendStatusMessage(String.format (" - %s[validated: %s]%s",
                    Colour.BLUE.getColour(), riIdentity, Colour.RESET.getColour()),
                    true, false);
            } 
        } catch (SQLException ex) {
            sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                true, false);
            sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), "ERROR: Invalid command line argument, cannot continue.", Colour.RESET.getColour()),
                true, false);
            System.exit(0);
        }
        
        try {
            // 4) Check IP address, then update if necessary.
            if (!riHostAddress.equals(InetAddress.getLocalHost().getHostAddress())) {
                sendStatusMessage("Updating Remote Interlocking IP address within remote DB...", 
                    false, false);
                MySqlConnect.getDbCon().insert(String.format ("UPDATE `Remote_Interlocking` SET `ip_address` = '%s' WHERE `index_key` = '%s';",
                    InetAddress.getLocalHost().getHostAddress(), riIndex));
                sendStatusMessage(String.format ("%s%s%s",
                    Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
                    false, false);
                sendStatusMessage(String.format (" - %s[actual: %s => DB: %s]%s",
                    Colour.BLUE.getColour(), InetAddress.getLocalHost().getHostAddress(),
                        riHostAddress, Colour.RESET.getColour()), 
                        true, false);
                riHostAddress = InetAddress.getLocalHost().getHostAddress();
                
            } 
        } catch (UnknownHostException | SQLException ex) {
            sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                true, false);
            sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), "WARNING: Cannot update Remote DB with the IP address of this Remote Interlocking.", Colour.RESET.getColour()),
                true, false);
        }
        
        // 5) Get the Interlocking Credentials from the Database.
        sendStatusMessage("Obtaining Interlocking details from the remote DB...", 
            false, false);
        try {
            rs = MySqlConnect.getDbCon().query(String.format ("SELECT * FROM Interlocking WHERE index_key = '%s';", iIndex));
            rs.first();
            if (rs.getInt("index_key") == iIndex) {
                iHostAddress = rs.getString("ip_address");
                iPort = rs.getInt("port_number");
                iName = rs.getString("identity");
                sendStatusMessage(String.format ("%s%s%s",
                    Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
                    false, false);
                sendStatusMessage(String.format (" - %s[%s@%s:%s]%s",
                    Colour.BLUE.getColour(), iName, iHostAddress, iPort, Colour.RESET.getColour()), 
                    true, false);
            } else {
                throw new SQLException();
            }
        } catch (SQLException ex) {
            sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                true, false);
            sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), "ERROR: Cannot obtain interlocking credentials from remote DB, cannot continue.", Colour.RESET.getColour()),
                true, false);
            System.exit(0);
        }
        
        // 6) Obtain Data Logger details from the DataBase.
        try {
            sendStatusMessage("Obtaining Data Logger details from the remote DB...", 
                false, false);
            rs = MySqlConnect.getDbCon().query("SELECT * FROM Data_Logger;");
            rs.first();
            dlHostAddress = rs.getString("ip_address");
            dlPort = rs.getInt("port_number");
            sendStatusMessage(String.format ("%s%s%s",
                Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
                false, false);
            sendStatusMessage(String.format (" - %s[%s:%s]%s",
                Colour.BLUE.getColour(), dlHostAddress, dlPort, Colour.RESET.getColour()), 
                true, false);
            dataLoggerDetails = true;
        } catch (SQLException ex) {
            sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                true, false);
            sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), "WARNING: Cannot obtain Data Logger details from the remote DB.", Colour.RESET.getColour()),
                true, false);
        }
        
        // 7) Obtain list of all LineSide Modules assigned to this Remote Interlocking.
        sendStatusMessage("Obtaining LineSide Modules assigned to this Remote Interlocking from the remote DB...", 
            false, false);
        try {
            String lsModOutput = "";
            rs = MySqlConnect.getDbCon().query(String.format ("SELECT * FROM `Lineside_Module` WHERE `remote_interlocking_index` = '%s';", riIndex));
            while (rs.next()) {
                LS_MOD.put(rs.getString("identity"), rs.getInt("index_key"));
                lsModOutput += rs.getString("identity") + "(" + rs.getInt("index_key") + ")" + ((rs.isLast()) ? "" : ", ");
                LinesideModuleSetupComplete.put(rs.getString("identity"), false);
                
            }
            
            
            sendStatusMessage(String.format ("%s%s%s",
                Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
                false, false);
            sendStatusMessage(String.format (" - %s[%s]%s",
                Colour.BLUE.getColour(), lsModOutput, Colour.RESET.getColour()), 
                true, false);
        } catch (SQLException ex) {
            sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                true, false);
            sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), "ERROR: Cannot obtain LineSide Module details from remote DB, cannot continue.", Colour.RESET.getColour()),
                true, false);
            System.exit(0);
        }
        
        // 8) Attempt a connection to the DataLogger
        if (dataLoggerDetails) {
            sendStatusMessage("Attempting a connection to the Data Logger...", 
                false, false);
            try {
                dataLogger = new DataLoggerClient(dlHostAddress, dlPort, riIdentity);
                dataLogger.setName("DataLogger-Thread");
                dataLogger.start();
                connectedToDL = true;
                Thread.sleep(2000);
            } catch (IOException | InterruptedException ex) {}
        }
        
        // 9) Listenening for incoming connections from LineSide Modules.
        sendStatusMessage("Listening for incoming connections from LineSide Modules...", 
            false, true);
        lsModListen = new ListenForRequests(riPort);
        lsModListen.setName("Listening Thread");
        lsModListen.start();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(RemoteInterlocking.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Thread processMessages = new Thread (() -> {
            while (true) {
                try {
                    Thread.sleep(500);
                    MessageHandler.processMessageStack();
                } catch (InterruptedException ex) {
                    Logger.getLogger(RemoteInterlocking.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        processMessages.setName("Processing Messages Thread");
        processMessages.start();

       while (LinesideModuleSetupComplete.containsValue(false)) {
            // Loop until setup complete
        }
        
        // X) Create asset objects - Points.
        
        // X) Attempt a connection to the Interlocking
        sendStatusMessage("Attempting a connection to the Interlocking...", false, true);
        sendStatusMessage(String.format ("%s%s%s",
            Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
            true, true);
        sendStatusMessage(String.format ("%s%s%s",
            Colour.RED.getColour(), "ERROR: Cannot connect to the Interlocking, cannot continue.", Colour.RESET.getColour()),
            true, true);
        //System.exit(0);
    }
    
    public static synchronized Boolean setupLineSideModule(String lineSideModuleIdentity) {

        if (LinesideModuleSetupComplete.containsKey(lineSideModuleIdentity)) {
            if (LinesideModuleSetupComplete.get(lineSideModuleIdentity).equals(true)) {
                return false;
            }
        }
        
        int index = 0;
        
        if (LS_MOD.containsKey(lineSideModuleIdentity)) {
            index = LS_MOD.get(lineSideModuleIdentity);
        } else {
            sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                true, true);
            return false;
        }
        
        sendStatusMessage(String.format("Connection established with Line Side Module [%s] - Accessing remote DB to download assets.", 
            lineSideModuleIdentity),
            true, true);
        
        // 1) Setup Points.
        sendStatusMessage(String.format("Connected to remote DB - looking for Points assigned to Line Side Module [%s]...",
            lineSideModuleIdentity),
            false, true);

        try {
            rs = MySqlConnect.getDbCon().query(String.format ("SELECT * FROM `Points` WHERE `parentLinesideModule` = '%s';", index));
            sendStatusMessage(String.format ("%s%s%s",
                Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
                true, true);
            System.out.println();
            sendStatusMessage(String.format ("%s%s%s",
                Colour.BLUE.getColour(), "Points", Colour.RESET.getColour()),
                true, true);
            sendStatusMessage(String.format ("%s------%s",
                Colour.BLUE.getColour(), Colour.RESET.getColour()), 
                true, true);
            
            while (rs.next()) {
                addPointsToArray (rs.getString("identity"), lineSideModuleIdentity);
                sendStatusMessage(String.format ("%s%s%s", 
                    Colour.BLUE.getColour(), rs.getString("identity"), Colour.RESET.getColour()),
                    true, true);
            }
            
            MessageHandler.outGoingMessage("UPDATE.POINTS.ALL", MessageType.SETUP, lineSideModuleIdentity);
            System.out.println();
            
        } catch (SQLException ex) {
            sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                true, true);
            return false;
        }
        
        // 2) Setup Controlled Signals...
        sendStatusMessage(String.format("Connected to remote DB - looking for Controlled Signals assigned to Line Side Module [%s]...",
            lineSideModuleIdentity),
            false, true);
        
        try {
            rs = MySqlConnect.getDbCon().query(String.format ("SELECT * FROM `Controlled_Signals` WHERE `parentLinesideModule` = '%s';", index));
            sendStatusMessage(String.format ("%s%s%s",
                Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
                true, true);
            System.out.println();
            sendStatusMessage(String.format ("%s%-20s%-9s%s",
                Colour.BLUE.getColour(), "Controlled Signals", "Type", Colour.RESET.getColour()),
                true, true);
            sendStatusMessage(String.format ("%s----------------------------------------%s",
                Colour.BLUE.getColour(), Colour.RESET.getColour()), 
                true, true);
            while (rs.next()) {
                addControlledSignalsToArray (rs.getString("prefix") + rs.getString("identity"), lineSideModuleIdentity);
                sendStatusMessage(String.format ("%s%-20s%-9s%s", 
                    Colour.BLUE.getColour(), rs.getString("prefix") + rs.getString("identity"), rs.getString("type"), Colour.RESET.getColour()),
                    true, true);
            }
            
            MessageHandler.outGoingMessage("UPDATE.CONTROLLED_SIGNALS.ALL", MessageType.SETUP, lineSideModuleIdentity);
            System.out.println();
            
        } catch (SQLException ex) {
            sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                true, true);
            return false;
        }
        
        // 3) Setup Non-Controlled Signals...
        sendStatusMessage(String.format("Connected to remote DB - looking for Non-Controlled Signals assigned to Line Side Module [%s]...",
            lineSideModuleIdentity),
            false, true);
        
        try {
            rs = MySqlConnect.getDbCon().query(String.format ("SELECT * FROM `Non_Controlled_Signals` WHERE `parentLinesideModule` = '%s';", index));
            sendStatusMessage(String.format ("%s%s%s",
                Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
                true, true);
            System.out.println();
            sendStatusMessage(String.format ("%s%-23s%-9s%s",
                Colour.BLUE.getColour(), "Non-Controlled Signals", "Type", Colour.RESET.getColour()),
                true, true);
            sendStatusMessage(String.format ("%s-------------------------------------%s",
                Colour.BLUE.getColour(), Colour.RESET.getColour()), 
                true, true);
            while (rs.next()) {
                addNonControlledSignalsToArray (rs.getString("prefix") + rs.getString("identity"), lineSideModuleIdentity);
                sendStatusMessage(String.format ("%s%-23s%s%s", 
                    Colour.BLUE.getColour(), rs.getString("prefix") + rs.getString("identity"), rs.getString("type"), Colour.RESET.getColour()),
                    true, true);
            }
            
            MessageHandler.outGoingMessage("UPDATE.NON_CONTROLLED_SIGNALS.ALL", MessageType.SETUP, lineSideModuleIdentity);
            System.out.println();
            
        } catch (SQLException ex) {
            sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                true, true);
            return false;
        }
        
        // 4) Setup Train Detection...
        sendStatusMessage(String.format("Connected to remote DB - looking for Train Detection Sections assigned to Line Side Module [%s]...",
            lineSideModuleIdentity),
            false, true);
        
        try {
            rs = MySqlConnect.getDbCon().query(String.format ("SELECT * FROM `Train_Detection` WHERE `parentLinesideModule` = '%s';", index));
            sendStatusMessage(String.format ("%s%s%s",
                Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
                true, true);
            System.out.println();
            sendStatusMessage(String.format ("%s%-9s%s%s",
                Colour.BLUE.getColour(), "Section", "Type", Colour.RESET.getColour()),
                true, true);
            sendStatusMessage(String.format ("%s----------------------%s",
                Colour.BLUE.getColour(), Colour.RESET.getColour()), 
                true, true);
            while (rs.next()) {
                addTrainDetectionSectionsToArray (rs.getString("identity"), lineSideModuleIdentity);
                sendStatusMessage(String.format ("%s%-9s%-9s%s", 
                    Colour.BLUE.getColour(), rs.getString("identity"), rs.getString("type"), Colour.RESET.getColour()),
                    true, true);
            }
            
            MessageHandler.outGoingMessage("UPDATE.TRAIN_DETECTION.ALL", MessageType.SETUP, lineSideModuleIdentity);
            System.out.println();
            
        } catch (SQLException ex) {
            sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                true, true);
            return false;
        }
       
        setSetupComplete(lineSideModuleIdentity);
        return true;
    }
    
    public static synchronized void setSetupComplete (String lineSideModule) {
    
        if (LinesideModuleSetupComplete.containsKey(lineSideModule)) {
            LinesideModuleSetupComplete.remove(lineSideModule);
            LinesideModuleSetupComplete.put(lineSideModule, true);
        } 
    }    
    
    
    /**
     * This method creates a Points object and adds that object to the POINTS array.
     * This method is called during initial setup, when the points objects are being received from the LSM.
     * 
     * @param identity A <code>String</code> containing the identity of the points.
     * @param lsm A <code>String</code> containing the parent Lineside Module.
     */
    public static synchronized void addPointsToArray (String identity, String lsm) {
        POINTS.add(new Points(identity, lsm));
    }
    
    public static synchronized void updatePoints (String pointsIdentity, PointsPosition position, Boolean detected) {
        for (int i = 0; i < POINTS.size(); i++) {
            if (POINTS.get(i).getPointsIdentity().equals(pointsIdentity)) {
                POINTS.get(i).setPosition(position);
                POINTS.get(i).setDetection(detected);
                break;
            }
        }
    }
    
    public static synchronized void addControlledSignalsToArray (String identity, String lsm) {
        // TODO
    }
    
    public static synchronized void addNonControlledSignalsToArray (String identity, String lsm) {
        // TODO
    }
    
    public static synchronized void addTrainDetectionSectionsToArray (String identity, String lsm) {
        // TODO
    }
    
    /**
     * This method returns the identity of the Remote Interlocking.
     * 
     * @return A <code>String</code> that contains the remote interlocking identity.
     */
    public static String getRemoteInterlockingName() {
        return riIdentity;
    }
    
    /**
     * This method validates the identity passed against the list of associated LineSideModules.
     * This ensures that only those Lineside Modules that are registered to this remote interlocking are able to communicate.
     * 
     * @param identity A <code>String</code> containing the identity of the sending LSM.
     * @return Boolean <code>true</code> is returned if the LSM identity has been validated, otherwise <code>false</code>.
     */
    public static Boolean validateModuleIdentity(String identity) {
        
        if (RemoteInterlocking.LS_MOD.containsKey(identity)) {
            return true;
        } else {
            return false;
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
     * This method sends an output to the console and to the Data Logger as required.
     * 
     * @param msg A <code>String</code> containing the message.
     * @param newLine A <code>Boolean</code> - <i>true</i> adds a new line to the end of the message, otherwise <i>false</i>.
     * @param dLogger A <code>Boolean</code> - <i>true</i> sends the output to the Data Logger, otherwise <i>false</i>.
     */
    public static synchronized void sendStatusMessage(String msg, Boolean newLine, Boolean dLogger) {
        if (dLogger && connectedToDL) {
            try {
                dataLogger.sendToDataLogger(msg, true, newLine);
            } catch (IOException ex) {}
        } else {
            System.out.printf("%s%s", msg, (newLine) ? NEW_LINE : "");
        }
    }
}

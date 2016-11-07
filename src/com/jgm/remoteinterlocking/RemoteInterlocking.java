package com.jgm.remoteinterlocking;

import com.jgm.remoteinterlocking.assets.Aspects;
import com.jgm.remoteinterlocking.assets.ControlledSignal;
import com.jgm.remoteinterlocking.assets.AutomaticSignal;
import com.jgm.remoteinterlocking.assets.Points;
import com.jgm.remoteinterlocking.assets.PointsPosition;
import com.jgm.remoteinterlocking.assets.TrainDetectionSection;
import com.jgm.remoteinterlocking.assets.TrainDetectionStatus;
import com.jgm.remoteinterlocking.database.MySqlConnect;
import com.jgm.remoteinterlocking.datalogger.DataLoggerClient;
import com.jgm.remoteinterlocking.linesidemoduleconnection.ListenForRequests;
import com.jgm.remoteinterlocking.linesidemoduleconnection.MessageHandler;
import com.jgm.remoteinterlocking.tecui.TechniciansUserInterface;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;

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
    
    /**
     * This map contains the LineSide Modules assigned to this remote interlocking.
     * The <code>String</code> is the key, and contains the LineSideModule Identity.
     * The <code>Integer</code> is the value, and contains the index_key from within the database.
     */
    private static final HashMap<String, Integer> LS_MOD = new HashMap<>();
    
    /**
     * This map contains values that record if the LinesideModule is setup correctly.
     * The <code>String</code> is the key, and contains the LineSideModule Identity.
     * The <code>Boolean</code> is the value, 'true' indicates that full setup is needed, otherwise 'false'.
     */
    private static final HashMap<String, Boolean> LSM_SETUP_COMPLETE = new HashMap<>();
    public static ListenForRequests lsModListen;
    
    // Database variables
    private static ResultSet rs;
    
    // General declarations.
    public final static String NEW_LINE = System.lineSeparator();
    private final static String OPERATING_SYSTEM = System.getProperty("os.name");
    
    // Data Logger variables.
    private static DataLoggerClient dataLogger;
    private static Boolean connectedToDL = false;
    
    // Asset Variables.
    private static final ArrayList<Points> POINTS = new ArrayList<>();
    private static final ArrayList<ControlledSignal> CONTROLLED_SIGNALS = new ArrayList<>();
    private static final ArrayList<AutomaticSignal> AUTOMATIC_SIGNALS = new ArrayList<>();
    private static final ArrayList<TrainDetectionSection> TRAIN_DETECTION_SECTIONS = new ArrayList<>(); 
    
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
                TerminalColour.RED.getColour(), getFailed(), TerminalColour.RESET.getColour()),
                true, false);
            sendStatusMessage(String.format ("%s%s%s",
                TerminalColour.RED.getColour(), "ERROR: Missing command line argument, cannot continue.", TerminalColour.RESET.getColour()),
                true, false);
            System.exit(0);
        }
        
        // 2) Test if the remote interlocking ID is a String and between 5 - 15 characters A-Z, a-z, 0-9 and _.
        if (!riIdentity.matches("^[a-zA-Z_0-9_]{5,15}$")) {
            sendStatusMessage(String.format ("%s%s%s",
                TerminalColour.RED.getColour(), getFailed(), TerminalColour.RESET.getColour()),
                true, false);
            sendStatusMessage(String.format ("%s%s%s",
                TerminalColour.RED.getColour(), "ERROR: Invalid command line argument, cannot continue.", TerminalColour.RESET.getColour()),
                true, false);
            System.exit(0);
        } else {
            sendStatusMessage(String.format ("%s%s%s",
                TerminalColour.GREEN.getColour(), getOK(), TerminalColour.RESET.getColour()),
                true, false);
        }
        
        // 3) Connect to the database and check if the identity passed on the command line is valid.
        try {
            rs = MySqlConnect.getDbCon().query(String.format ("SELECT * FROM Remote_Interlocking WHERE identity = '%s';", riIdentity));

        } catch (SQLException ex) {

            sendStatusMessage(String.format ("%s%s%s",
                TerminalColour.RED.getColour(), "ERROR: Cannot connect to the remote DB, cannot continue.", TerminalColour.RESET.getColour()),
                true, false);
            System.exit(0);
        }
        
        sendStatusMessage("Checking Remote DB for Remote Interlocking Credentials...", 
            false, false);
        try {
            rs.first();
            if (rs.getString("identity").equals(riIdentity)) {
                sendStatusMessage(String.format ("%s%s%s",
                    TerminalColour.GREEN.getColour(), getOK(), TerminalColour.RESET.getColour()),
                    false, false);
                riIndex = rs.getInt("index_key");
                riHostAddress = rs.getString("ip_address");
                riPort = rs.getInt("port_number");
                iIndex = rs.getInt("interlocking_index");
                sendStatusMessage(String.format (" - %s[validated: %s]%s",
                    TerminalColour.BLUE.getColour(), riIdentity, TerminalColour.RESET.getColour()),
                    true, false);
            } 
        } catch (SQLException ex) {
            sendStatusMessage(String.format ("%s%s%s",
                TerminalColour.RED.getColour(), getFailed(), TerminalColour.RESET.getColour()),
                true, false);
            sendStatusMessage(String.format ("%s%s%s",
                TerminalColour.RED.getColour(), "ERROR: Invalid command line argument, cannot continue.", TerminalColour.RESET.getColour()),
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
                    TerminalColour.GREEN.getColour(), getOK(), TerminalColour.RESET.getColour()),
                    false, false);
                sendStatusMessage(String.format (" - %s[actual: %s => DB: %s]%s",
                    TerminalColour.BLUE.getColour(), InetAddress.getLocalHost().getHostAddress(),
                        riHostAddress, TerminalColour.RESET.getColour()), 
                        true, false);
                riHostAddress = InetAddress.getLocalHost().getHostAddress();
                
            } 
        } catch (UnknownHostException | SQLException ex) {
            sendStatusMessage(String.format ("%s%s%s",
                TerminalColour.RED.getColour(), getFailed(), TerminalColour.RESET.getColour()),
                true, false);
            sendStatusMessage(String.format ("%s%s%s",
                TerminalColour.RED.getColour(), "WARNING: Cannot update Remote DB with the IP address of this Remote Interlocking.", TerminalColour.RESET.getColour()),
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
                    TerminalColour.GREEN.getColour(), getOK(), TerminalColour.RESET.getColour()),
                    false, false);
                sendStatusMessage(String.format (" - %s[%s@%s:%s]%s",
                    TerminalColour.BLUE.getColour(), iName, iHostAddress, iPort, TerminalColour.RESET.getColour()), 
                    true, false);
            } else {
                throw new SQLException();
            }
        } catch (SQLException ex) {
            sendStatusMessage(String.format ("%s%s%s",
                TerminalColour.RED.getColour(), getFailed(), TerminalColour.RESET.getColour()),
                true, false);
            sendStatusMessage(String.format ("%s%s%s",
                TerminalColour.RED.getColour(), "ERROR: Cannot obtain interlocking credentials from remote DB, cannot continue.", TerminalColour.RESET.getColour()),
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
                TerminalColour.GREEN.getColour(), getOK(), TerminalColour.RESET.getColour()),
                false, false);
            sendStatusMessage(String.format (" - %s[%s:%s]%s",
                TerminalColour.BLUE.getColour(), dlHostAddress, dlPort, TerminalColour.RESET.getColour()), 
                true, false);
            dataLoggerDetails = true;
        } catch (SQLException ex) {
            sendStatusMessage(String.format ("%s%s%s",
                TerminalColour.RED.getColour(), getFailed(), TerminalColour.RESET.getColour()),
                true, false);
            sendStatusMessage(String.format ("%s%s%s",
                TerminalColour.RED.getColour(), "WARNING: Cannot obtain Data Logger details from the remote DB.", TerminalColour.RESET.getColour()),
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
                LSM_SETUP_COMPLETE.put(rs.getString("identity"), false);
                
            }
            
            sendStatusMessage(String.format ("%s%s%s",
                TerminalColour.GREEN.getColour(), getOK(), TerminalColour.RESET.getColour()),
                false, false);
            sendStatusMessage(String.format (" - %s[%s]%s",
                TerminalColour.BLUE.getColour(), lsModOutput, TerminalColour.RESET.getColour()), 
                true, false);
        } catch (SQLException ex) {
            sendStatusMessage(String.format ("%s%s%s",
                TerminalColour.RED.getColour(), getFailed(), TerminalColour.RESET.getColour()),
                true, false);
            sendStatusMessage(String.format ("%s%s%s",
                TerminalColour.RED.getColour(), "ERROR: Cannot obtain LineSide Module details from remote DB, cannot continue.", TerminalColour.RESET.getColour()),
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
        
        // Initiate and run the processMessageStack method as a Thread
        Thread processMessages = new Thread (() -> {
            while (true) {
                try {
                    Thread.sleep(500);
                    MessageHandler.processMessageStack();
                } catch (InterruptedException ex) {
                    
                }
            }
        });
        processMessages.setName("Processing Messages Thread");
        processMessages.start();

       while (LSM_SETUP_COMPLETE.containsValue(false)) {
            // Loop until setup complete
        }
        
        // X) Create asset objects - Points.
        
        // X) Attempt a connection to the Interlocking
        sendStatusMessage("Attempting a connection to the Interlocking...", false, true);
        sendStatusMessage(String.format ("%s%s%s",
            TerminalColour.RED.getColour(), getFailed(), TerminalColour.RESET.getColour()),
            true, true);
        sendStatusMessage(String.format ("%s%s%s",
            TerminalColour.RED.getColour(), "ERROR: Cannot connect to the Interlocking, cannot continue.", TerminalColour.RESET.getColour()),
            true, true);
        //System.exit(0);
    }
    
    /**
     * This method performs a setup on all the Remote Client associated assets.
     * 
     * @param lineSideModuleIdentity A <code>String</code> containing the Lineside Module Identity.
     * @return <code>Boolean</code> <i>'false'</i> is returned if there is no need to perform a setup, otherwise <i>'true'</i>.
     */
    public static synchronized Boolean setupAssets(String lineSideModuleIdentity) {
        
        // Check to see if the LSM passed to the method needs setting up - false indicates 'NO'.
        if (LSM_SETUP_COMPLETE.containsKey(lineSideModuleIdentity)) {
            if (LSM_SETUP_COMPLETE.get(lineSideModuleIdentity).equals(true)) {
                return false;
            }
        }
        
        // This block checks if the lineSideModuleIdentity is within the LS_MOD HashMap, and returns the value, which represents the index_key from the DataBase.
        int index = 0;
        if (LS_MOD.containsKey(lineSideModuleIdentity)) {
            index = LS_MOD.get(lineSideModuleIdentity);
        } else {
            // Display a message to the console and Data Logger.
            sendStatusMessage(String.format ("%s%s%s",
                TerminalColour.RED.getColour(), getFailed(), TerminalColour.RESET.getColour()),
                true, true);
            return false;
        }
        
        // Display a message to the console and Data Logger.
        sendStatusMessage(String.format("Connection established with Line Side Module [%s] - Accessing remote DB to download assets.", 
            lineSideModuleIdentity),
            true, true);
        
        // Run the setup Methods.
        try {
            setUpPoints(index, lineSideModuleIdentity);
            setUpControlledSignals(index, lineSideModuleIdentity);
            setUpNonControlledSignals(index, lineSideModuleIdentity);
            setUpTrainDetection(index, lineSideModuleIdentity);
        } catch (SQLException e) {
            sendStatusMessage(String.format ("%s%s%s",
                TerminalColour.RED.getColour(), getFailed(), TerminalColour.RESET.getColour()),
                true, true);
            return false;
        }

        setSetupComplete(lineSideModuleIdentity); // Confirm the setup is complete.
        
        new Thread (()-> {
            
            Application.launch(TechniciansUserInterface.class);
        
        }).start();
        
        return true;
    }
    
    /**
     * This method is used to set that a particular LinesideModule has been setup correctly.
     * @param lineSideModule <code>String</code> contains the Lineside Module Identity
     */
    public static synchronized void setSetupComplete (String lineSideModule) {
    
        if (LSM_SETUP_COMPLETE.containsKey(lineSideModule)) {
            LSM_SETUP_COMPLETE.remove(lineSideModule);
            LSM_SETUP_COMPLETE.put(lineSideModule, true);
        } 
    }    
    
    /**
     * This method creates a Points object and adds that object to the POINTS array.
     * This method is called during initial setup, when the points objects are being polled from the remote DB.
     * 
     * @param identity A <code>String</code> containing the identity of the points.
     * @param lsm A <code>String</code> containing the parent Lineside Module.
     */
    public static synchronized void addPointsToArray (String identity, String lsm) {
        POINTS.add(new Points(identity, lsm));
    }
    
    /**
     * This method is used to update the status of a particular points object.
     * @param pointsIdentity <code>String</code> containing the Identity of the points object.
     * @param position <code>PointsPosition</code> containing the position of the points.
     * @param detected <code>Boolean</code> 'true' indicates that the points are detected, otherwise 'false'.
     */
    public static synchronized void updatePoints (String pointsIdentity, PointsPosition position, Boolean detected) {
        for (int i = 0; i < POINTS.size(); i++) {
            if (POINTS.get(i).getPointsIdentity().equals(pointsIdentity)) {
                POINTS.get(i).setPosition(position);
                POINTS.get(i).setDetection(detected);
                break;
            }
        }
    }
    
    /**
    * This method creates a ControlledSignal object and adds that object to the CONTROLLED_SIGNALS array.
    * This method is called during initial setup, when the Controlled Signal objects are being polled from the remote DB.
    * 
    * @param prefix A <code>String</code> containing the prefix of the Controlled Signal.
    * @param signalIdentity A <code>String</code> containing the identity of the Controlled Signal.
    * @param lineSideModuleIdentity A <code>String</code> containing the parent Lineside Module.
    */
    public static synchronized void addControlledSignalsToArray (String prefix, String signalIdentity, String lineSideModuleIdentity) {
        CONTROLLED_SIGNALS.add(new ControlledSignal(prefix, signalIdentity, lineSideModuleIdentity));
    }
    
    /**
     * This method is used to update the status of a particular Controlled Signal
     * @param signalPrefix
     * @param signalIdentity <code>String</code> containing the Identity of Controlled Signal.
     * @param aspect <code>Aspects</code> containing the current aspect of the Controlled Signal
     */
    public static synchronized void updateControlledSignalAspect (String signalPrefix, String signalIdentity, Aspects aspect) {
       for (int i = 0; i < CONTROLLED_SIGNALS.size(); i++) {
            if (CONTROLLED_SIGNALS.get(i).getIdentity().equals(signalIdentity) && CONTROLLED_SIGNALS.get(i).getPrefix().equals(signalPrefix)) {
                CONTROLLED_SIGNALS.get(i).setCurrentAspect(aspect);
                break;
            }
        }
    }
    
    /**
    * This method creates a AutomaticSignal object and adds that object to the AUTOMATIC_SIGNALS array.
    * This method is called during initial setup, when the Non-Controlled Signal objects are being polled from the remote DB.
    * 
    * @param prefix A <code>String</code> containing the prefix of the Non-Controlled Signal.
    * @param signalIdentity A <code>String</code> containing the identity of the Non-Controlled Signal.
    * @param lineSideModuleIdentity A <code>String</code> containing the parent Lineside Module.
    */
    public static synchronized void addNonControlledSignalsToArray (String prefix, String signalIdentity, String lineSideModuleIdentity) {
        AUTOMATIC_SIGNALS.add(new AutomaticSignal(prefix, signalIdentity, lineSideModuleIdentity));
    }
    
    /**
     * This method is used to update the status of a particular Non-Controlled Signal
     * @param signalPrefix
     * @param signalIdentity <code>String</code> containing the Identity of Non-Controlled Signal.
     * @param aspect <code>Aspects</code> containing the current aspect of the Non-Controlled Signal.
     */
    public static synchronized void updateNonControlledSignalAspect (String signalPrefix, String signalIdentity, Aspects aspect) {
       for (int i = 0; i < AUTOMATIC_SIGNALS.size(); i++) {
            if (AUTOMATIC_SIGNALS.get(i).getIdentity().equals(signalIdentity) && AUTOMATIC_SIGNALS.get(i).getPrefix().equals(signalPrefix)) {
                AUTOMATIC_SIGNALS.get(i).setCurrentAspect(aspect);

                break;
            }
        }
    }
    
    /**
    * This method creates a TrainDetectionSection object and adds that object to the TRAIN_DETECTION_SECTIONS array.
    * This method is called during initial setup, when the Non-Controlled Signal objects are being polled from the remote DB.
    * 
    * @param identity A <code>String</code> containing the identity of the TrainDetectionSection.
    * @param lsm A <code>String</code> containing the parent Lineside Module.
    */
    public static synchronized void addTrainDetectionSectionsToArray (String identity, String lsm) {
        TRAIN_DETECTION_SECTIONS.add(new TrainDetectionSection(identity, lsm));
    }
    
    /**
     * This method is used to update the status of a particular points object.
     * @param trainDetectionSectionIdentity <code>String</code> containing the Identity of the Train Detection Section.
     * @param status <code>TrainDetectionStatus</code> detailing the status of the Train Detection Section.
     */
    public static synchronized void updateTrainDetectionSection (String trainDetectionSectionIdentity, TrainDetectionStatus status) {
        for (int i = 0; i < TRAIN_DETECTION_SECTIONS.size(); i++) {
            if (TRAIN_DETECTION_SECTIONS.get(i).getSectionIdentity().equals(trainDetectionSectionIdentity)) {
                TRAIN_DETECTION_SECTIONS.get(i).setStatus(status);
                break;
            }
        }
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
    
    /**
     * This method sets up the Points objects.
     * @param index <code>Integer</code> representing the index_key of the LinesideModule object within the remote DB.
     */
    private static synchronized void setUpPoints(int index, String lineSideModuleIdentity) throws SQLException {
    
        sendStatusMessage(String.format("Connected to remote DB - looking for Points assigned to Line Side Module [%s]...",
            lineSideModuleIdentity),
            false, true);
        
        // Define and run the query.
        rs = MySqlConnect.getDbCon().query(String.format ("SELECT * FROM `Points` WHERE `parentLinesideModule` = '%s';", index));
        sendStatusMessage(String.format ("%s%s%s",
            TerminalColour.GREEN.getColour(), getOK(), TerminalColour.RESET.getColour()),
            true, true);
        System.out.println();
        sendStatusMessage(String.format ("%s%s%s",
            TerminalColour.BLUE.getColour(), "Points", TerminalColour.RESET.getColour()),
            true, true);
        sendStatusMessage(String.format ("%s------%s",
            TerminalColour.BLUE.getColour(), TerminalColour.RESET.getColour()), 
            true, true);

        // Iterate through the resultSet and add the Points Objects to the PointsArray - displaying the details to the console and Data Logger.
        while (rs.next()) {
            addPointsToArray (rs.getString("identity"), lineSideModuleIdentity);
            sendStatusMessage(String.format ("%s%s%s", 
                TerminalColour.BLUE.getColour(), rs.getString("identity"), TerminalColour.RESET.getColour()),
                true, true);
        }
        
        System.out.println();
    }
    
    /**
     * This method sets up the Controlled Signals objects.
     * @param index <code>Integer</code> representing the index_key of the LinesideModule object within the remote DB.
     */
    private static synchronized void setUpControlledSignals(int index, String lineSideModuleIdentity) throws SQLException {
    
        sendStatusMessage(String.format("Connected to remote DB - looking for Controlled Signals assigned to Line Side Module [%s]...",
            lineSideModuleIdentity),
            false, true);
        
        // Define and run the query.
        rs = MySqlConnect.getDbCon().query(String.format ("SELECT * FROM `Controlled_Signals` WHERE `parentLinesideModule` = '%s';", index));
        sendStatusMessage(String.format ("%s%s%s",
            TerminalColour.GREEN.getColour(), getOK(), TerminalColour.RESET.getColour()),
            true, true);
        System.out.println();
        sendStatusMessage(String.format ("%s%-20s%-9s%s",
            TerminalColour.BLUE.getColour(), "Controlled Signals", "Type", TerminalColour.RESET.getColour()),
            true, true);
        sendStatusMessage(String.format ("%s----------------------------------------%s",
            TerminalColour.BLUE.getColour(), TerminalColour.RESET.getColour()), 
            true, true);
        
        // Iterate through the resultSet and add the Controlled Signal Objects to the ControlledSignal Array - displaying the details to the console and Data Logger.
        while (rs.next()) {
            addControlledSignalsToArray (rs.getString("prefix"), rs.getString("identity"), lineSideModuleIdentity);
            sendStatusMessage(String.format ("%s%-20s%-9s%s", 
                TerminalColour.BLUE.getColour(), rs.getString("prefix") + rs.getString("identity"), rs.getString("type"), TerminalColour.RESET.getColour()),
                true, true);
        }
        
        System.out.println();
            
    }
    
    private static synchronized void setUpNonControlledSignals(int index, String lineSideModuleIdentity) throws SQLException {
        
        sendStatusMessage(String.format("Connected to remote DB - looking for Non-Controlled Signals assigned to Line Side Module [%s]...",
            lineSideModuleIdentity),
            false, true);
        
        // Define and run the query.
        rs = MySqlConnect.getDbCon().query(String.format ("SELECT * FROM `Non_Controlled_Signals` WHERE `parentLinesideModule` = '%s';", index));
        sendStatusMessage(String.format ("%s%s%s",
            TerminalColour.GREEN.getColour(), getOK(), TerminalColour.RESET.getColour()),
            true, true);
        System.out.println();
        sendStatusMessage(String.format ("%s%-23s%-9s%s",
            TerminalColour.BLUE.getColour(), "Non-Controlled Signals", "Type", TerminalColour.RESET.getColour()),
            true, true);
        sendStatusMessage(String.format ("%s-------------------------------------%s",
            TerminalColour.BLUE.getColour(), TerminalColour.RESET.getColour()), 
            true, true);
        
        // Iterate through the resultSet and add the Non-Controlled Signal Objects to the AutomaticSignal Array - displaying the details to the console and Data Logger.
        while (rs.next()) {
            addNonControlledSignalsToArray (rs.getString("prefix"), rs.getString("identity"), lineSideModuleIdentity);
            sendStatusMessage(String.format ("%s%-23s%s%s", 
                TerminalColour.BLUE.getColour(), rs.getString("prefix") + rs.getString("identity"), rs.getString("type"), TerminalColour.RESET.getColour()),
                true, true);
        }

        System.out.println();
       
    }
    
    private static synchronized void setUpTrainDetection(int index, String lineSideModuleIdentity) throws SQLException {
    
        sendStatusMessage(String.format("Connected to remote DB - looking for Train Detection Sections assigned to Line Side Module [%s]...",
            lineSideModuleIdentity),
            false, true);
        
        // Define and run the query.
        rs = MySqlConnect.getDbCon().query(String.format ("SELECT * FROM `Train_Detection` WHERE `parentLinesideModule` = '%s';", index));
        sendStatusMessage(String.format ("%s%s%s",
            TerminalColour.GREEN.getColour(), getOK(), TerminalColour.RESET.getColour()),
            true, true);
        System.out.println();
        sendStatusMessage(String.format ("%s%-9s%s%s",
            TerminalColour.BLUE.getColour(), "Section", "Type", TerminalColour.RESET.getColour()),
            true, true);
        sendStatusMessage(String.format ("%s----------------------%s",
            TerminalColour.BLUE.getColour(), TerminalColour.RESET.getColour()), 
            true, true);
        
        // Iterate through the resultSet and add the Train Detection Section Objects to the TrainDetection Array - displaying the details to the console and Data Logger.
        while (rs.next()) {
            addTrainDetectionSectionsToArray (rs.getString("identity"), lineSideModuleIdentity);
            sendStatusMessage(String.format ("%s%-9s%-9s%s", 
                TerminalColour.BLUE.getColour(), rs.getString("identity"), rs.getString("type"), TerminalColour.RESET.getColour()),
                true, true);
        }
        
        System.out.println();
            
    }
    
    /**
     * This method returns the status of the Train Detection Status
     * @param identity <code>String</code> The identity of the Train Detection Section.
     * @return <code>TrainDetectionStatus</code> The status of the Train Detection Section.
     */
    public static synchronized TrainDetectionStatus getTrainDetectionStatus (String identity) {
        
        for (int i = 0; i < TRAIN_DETECTION_SECTIONS.size(); i++) {
            if (TRAIN_DETECTION_SECTIONS.get(i).getSectionIdentity().equals(identity) ) {
                return TRAIN_DETECTION_SECTIONS.get(i).getStatus();
            }
        }
        
        return null;
    }
    
    /**
     * This method returns the Current Signal Aspect of a Controlled Signal.
     * @param signalPrefix <code>String</code> The prefix of the signal.
     * @param signalIdentity <code>String</code> The identity of the signal.
     * @return <code>Aspects</code> The current aspect of the signal.
     */
    public static synchronized Aspects getControlledSignalAspect (String signalPrefix, String signalIdentity) {
        
        for (int i = 0; i < CONTROLLED_SIGNALS.size(); i++) {
            if (CONTROLLED_SIGNALS.get(i).getPrefix().equals(signalPrefix) && CONTROLLED_SIGNALS.get(i).getIdentity().equals(signalIdentity)) {
                return CONTROLLED_SIGNALS.get(i).getCurrentAspect();
            }
        }
        
        return null;
    }
    
        /**
     * This method returns the Current Signal Aspect of a Non-Controlled Signal.
     * @param signalPrefix <code>String</code> The prefix of the signal.
     * @param signalIdentity <code>String</code> The identity of the signal.
     * @return <code>Aspects</code> The current aspect of the signal.
     */
    public static synchronized Aspects getNonControlledSignalAspect (String signalPrefix, String signalIdentity) {
        
        for (int i = 0; i < AUTOMATIC_SIGNALS.size(); i++) {
            if (AUTOMATIC_SIGNALS.get(i).getPrefix().equals(signalPrefix) && AUTOMATIC_SIGNALS.get(i).getIdentity().equals(signalIdentity)) {
                return AUTOMATIC_SIGNALS.get(i).getCurrentAspect();
            }
        }
        
        return null;
    }
    
    /**
     * This method returns the Position of the Points.
     * @param pointsIdentity <code>String</code> The identity of the points.
     * @return <code>PointsPosition</code> the position of the points.
     */
    public static synchronized PointsPosition getPointsPosition (String pointsIdentity) {
        
        for (int i = 0; i < POINTS.size(); i++) {
            if (POINTS.get(i).getPointsIdentity().equals(pointsIdentity)) {
                return POINTS.get(i).getPointsPosition();
            }    
        }
        
        return null;
    }
    
    /**
     * This method returns the Detection Status of the points.
     * @param pointsIdentity <code>String</code> The identity of the points.
     * @return <code>Boolean</code> 'true; indicates the points are detected, otherwise 'false' is returned.
     */
    public static synchronized Boolean getPointsDetectionStatus (String pointsIdentity) {
        
        for (int i = 0; i < POINTS.size(); i++) {
            if (POINTS.get(i).getPointsIdentity().equals(pointsIdentity)) {
                return POINTS.get(i).getDetectionStatus();
            }    
        }
        
        return null;
    }
    
    public static ArrayList getControlledSignalsList () {
        return CONTROLLED_SIGNALS;
    }
    
    public static ArrayList getAutomaticSignalsList() {
        return AUTOMATIC_SIGNALS;
    }
    
    public static ArrayList <Points> getPointsArray() {
        return POINTS;
    }
}

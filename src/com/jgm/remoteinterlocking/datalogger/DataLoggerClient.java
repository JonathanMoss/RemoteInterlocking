package com.jgm.remoteinterlocking.datalogger;

import com.jgm.remoteinterlocking.Colour;
import com.jgm.remoteinterlocking.RemoteInterlocking;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;

/**
 * This class provides the functionality to connect to the Data Logger
 * @author Jonathan Moss
 * @version v1.0 September 2016
 */
public class DataLoggerClient extends Thread {
    private final String dataLoggerIP; // The IP address of the Data Logger.
    private final int dataLoggerPort; // The port number of the Data Logger.
    private DataOutputStream output; // The Socket output stream.
    private Socket conn; // The connection object.
    private volatile Boolean connected = false; // A flag that indicates if connected - used to keep the thread live.
    private final String identity; // The identity of the Module where a DataLoggerClient is instantiated.
    private final SocketAddress sockAddress; // The IP/Portnumber socketAddress used in the connection string.
    private DataInputStream input; // The input stream - used only to determine if the connection to the Data Logger remains live.
    private int connectionAttempts = 0; // A variable to hold the connection attempts.
    private static final int MAX_CON_ATTEMPTS = 5; // A variable to hold the maximum connection attempts.
    private Boolean establishingConnection = true; // A variable to flag if a connection is being sought.

    /**
     * This is the Constructor Method for the DataLoggerClient class
     * @param dataLoggerIP A <code>String</code> containing the IP address of the Data Logger.
     * @param dataLoggerPort An <code>int</code> containing the listening port number of the Data Logger.
     * @param identity A <code>String</code> that details the Module identity.
     * @throws IOException 
     */
    public DataLoggerClient(String dataLoggerIP, int dataLoggerPort, String identity) throws IOException {
        this.dataLoggerIP = dataLoggerIP;
        this.dataLoggerPort = dataLoggerPort;
        this.identity = identity;
        this.sockAddress = new InetSocketAddress (InetAddress.getByName(this.dataLoggerIP), this.dataLoggerPort);
    }
    
    // This method attempts to establish a connection with the server.
    private Boolean connectToServer() throws IOException {
        if (this.conn == null || !this.conn.isConnected()) {
            this.connectionAttempts ++;
            this.sendToDataLogger(String.format ("Attempting a connection to the Data Logger (Attempt %s/%s)...",this.connectionAttempts, MAX_CON_ATTEMPTS),true, false);
            this.conn = new Socket();
            this.conn.connect(sockAddress, 10000);
            this.sendToDataLogger(String.format("%s%s %s%s%s", 
                    Colour.GREEN.getColour(), RemoteInterlocking.getOK(), Colour.BLUE.getColour(), this.conn.toString(), Colour.RESET.getColour()), true, true);
            this.connectionAttempts = 0;
            return true;
        }
        return false;
    }
    
    // This method attempts to setup the output and input streams
    private void setupStreams() throws IOException {
        if (this.conn != null & this.conn.isConnected()) {
            this.output = new DataOutputStream(this.conn.getOutputStream());
            this.output.writeUTF(this.identity);
            this.output.flush();
            this.input = new DataInputStream(this.conn.getInputStream());
        }
    }
    
    // This method is called only when something goes wrong - it makes sure the connection object and input/output streams are closed.
    private void closeConnection() {
        // Attempt to close thie input/output streams and connection without throwing an error.
        if (this.conn.isConnected()) {
            try {
                this.output.close();
                this.input.close();
                this.conn.close(); 
            } catch (IOException e) {}
        }
        this.input = null;
        this.conn = null;
        this.output = null;
        this.connected = false;
        this.establishingConnection = true;
    }
    
    // This method attempts to read from the input stream; this is used to check if the connection to the DataLogger is live
    private void whileConnected() throws IOException {
        if (this.conn != null) {
            this.input.readUTF();
        } else {
            this.connected = false;
        }
    }
    
    /**
     * This method sends a message to the DataLogger where a valid connection exists, and to the console.
     * 
     * @param message A <code>String</code> representing the message to send to the DataLogger if a valid connection exists.
     * @param console A <code>Boolean</code> value, <i>true</i> prints the output of the message to the console, otherwise <i>false</i>. 
     * @param carriageReturn A <code>Boolean</code> value, <i>true</i> appends a carriage return to the end of the message, otherwise <i>false</i>.
     * @throws IOException 
     */
    public synchronized void sendToDataLogger (String message, Boolean console, Boolean carriageReturn) throws IOException {
        if (console) { // If console is set to true, display message on the console.
            System.out.print(String.format("%s%s", message, (carriageReturn) ? RemoteInterlocking.NEW_LINE : ""));
        }
        if (this.connected && this.output != null) { // Check connected, if so - send message to the DataLogger.
            this.output.writeUTF(String.format("%s", message));
            this.output.flush(); 
        }
    }
  
    // This method overrides the run() method from the Thread Class.
    @Override
    public void run() {
        do {
            try {
                if (establishingConnection && this.connectionAttempts <= MAX_CON_ATTEMPTS) {
                    if (this.connectionAttempts >= 5) {
                        this.sendToDataLogger(String.format ("%sWARNING: No further connection attempts with the Data Logger shall be made.%s",
                            Colour.RED.getColour(), Colour.RESET.getColour()),
                            true, true);
                        this.establishingConnection = false;
                    } else if (this.connectToServer()) {
                        this.connected = true;
                        this.establishingConnection = false;
                        this.connectionAttempts = 0; 
                    }
                } else {
                    if (this.connected == true && this.conn.isConnected()) {
                        this.setupStreams();
                    }
                    this.whileConnected();
                }
            } catch (EOFException eof) {  
                try {
                    // Server has disconnected
                    this.sendToDataLogger(String.format ("%sWARNING: The DataLogger Server has severed the connection%s",
                        Colour.RED.getColour(), Colour.RESET.getColour())
                        , true, true);
                } catch (IOException | NullPointerException e) {
                    System.out.println(String.format ("%s%s%s",
                        Colour.RED.getColour(), Arrays.toString(e.getStackTrace()), Colour.RESET.getColour())); 
                }
                    try {
                        this.closeConnection();
                    } catch (NullPointerException e) {
                        System.out.println(String.format ("%s%s%s",
                            Colour.RED.getColour(), Arrays.toString(e.getStackTrace()), Colour.RESET.getColour()));
                    }
                    this.run();
            } catch (IOException e) {
                try {
                    this.sendToDataLogger(String.format("%s%s%sWARNING: Cannot connect to the Data Logger (Connection Refused) [%s:%s]%s",
                        Colour.RED.getColour(), RemoteInterlocking.getFailed(), RemoteInterlocking.NEW_LINE, this.dataLoggerIP, this.dataLoggerPort, Colour.RESET.getColour())
                        ,true,true);
                    this.closeConnection();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        System.out.println(String.format ("%s%s%s",
                            Colour.RED.getColour(), Arrays.toString(ex.getStackTrace()), Colour.RESET.getColour()));
                    }
                    } catch (IOException | NullPointerException npe) {
                        System.out.println(String.format ("%s%s%s",
                            Colour.RED.getColour(), Arrays.toString(npe.getStackTrace()), Colour.RESET.getColour()));
                    }
            } catch (NullPointerException e) {
                System.out.println(String.format ("%s%s%s",
                    Colour.RED.getColour(), Arrays.toString(e.getStackTrace()), Colour.RESET.getColour()));
            }
        } while (this.connected | this.establishingConnection);
    }
}

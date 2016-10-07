package com.jgm.remoteinterlocking.linesidemoduleconnection;

import com.jgm.remoteinterlocking.Colour;
import static com.jgm.remoteinterlocking.RemoteInterlocking.getFailed;
import static com.jgm.remoteinterlocking.RemoteInterlocking.getOK;
import static com.jgm.remoteinterlocking.RemoteInterlocking.sendStatusMessage;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.HashMap;

/**
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class ListenForRequests extends Thread {

    private ServerSocket listeningSocket; // The server socket that will listen for incoming connection requests.
    private final int port; // The port number to listen for incoming connection requests.
    public static HashMap <String, ClientConnection> lsModCon = new HashMap<>(); // This map holds a reference to the Client Connection Objects.
    private static int connectionRequests = 1;
    
    /**
     * This is the constructor method for the LinesideModuleListen.
     * This object listens for incoming connections on the port specified, creating a socket connection when a request is received.
     * 
     * @param port An <code>Integer</code> specifying the port number to listen for connection requests on.
     */
    public ListenForRequests (int port) {
        this.port = port;
    }
    
    /**
     * This method is called when a connection to a Lineside Module has been validated.
     * This method replaces the key/value in lsModCon hash map with an entry with a confirmed Lineside Module Identity.
     * @param lsmIdentity A <code>String</code> that contains the Lineside Module Identity
     * @param index A <code>String</code> that contains the (former) key, prior to validation.
     */
    protected static synchronized void connectionValidated (String lsmIdentity, String index) {
        
        lsModCon.put(lsmIdentity, lsModCon.get(index)); // Create the new key/value pair using the value from the former key/value pair.
        lsModCon.remove(index); // Remove the former key/value pair as it is no longer needed.
        
    }
    
    /**
     * This method is used to get the ClientConnection object assigned to a particular LSM.
     * @param lsmIdentity A <code>String</code> representing the Lineside Module Identity
     * @return A <code>ClientConnection</code> object that is associated with the Lineside Module passed in the first parameter.
     */
    protected static synchronized ClientConnection getLsmConnection(String lsmIdentity) {
        if (lsModCon.containsKey(lsmIdentity)) { // Check if the HashMap contains the key...
            return lsModCon.get(lsmIdentity); // If it does, return the value (ClientConnection)
        } else {
            return null; // Otherwise, return null.
        }
    }
    
    @Override
    public void run() {
        try {
            this.listeningSocket = new ServerSocket(this.port, 100);
            sendStatusMessage(String.format ("%s%s%s",
                Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
                false, true);
            sendStatusMessage(String.format (" - %s[listening on port: %s]%s",
                Colour.BLUE.getColour(), this.listeningSocket.getLocalPort(), Colour.RESET.getColour()),
                true, true);
            
            do { 
                // Create and add a new ClientConnection to the HashMap with a default index using the connectionRequest integer when a connection request has been received.
                // We will change the default index later, once the identity of the LineSideModule has been verified.
                lsModCon.put(Integer.toString(connectionRequests), new ClientConnection(this.listeningSocket.accept(), Integer.toString(connectionRequests)));
                lsModCon.get(Integer.toString(connectionRequests)).setName(String.format ("ConnectionThread [%s]", connectionRequests));
                lsModCon.get(Integer.toString(connectionRequests)).start();
                sendStatusMessage(String.format("Connection request received [%s]", connectionRequests ), true, true);
                connectionRequests ++;
            } while (true);
            
        } catch (BindException b) {
            sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                true, true);
            sendStatusMessage(String.format ("%sERROR: There is a problem with the listening port configuration, cannot continue.%s",
                Colour.RED.getColour(), Colour.RESET.getColour()), 
                true, true);
            System.exit(0);
        } catch (IOException ex) {
            sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                true, true);
            sendStatusMessage(String.format ("%sERROR: Cannot listen for incoming connections from the LineSide Module(s), cannot continue.%s",
                Colour.RED.getColour(), Colour.RESET.getColour()), 
                true, true);
            System.exit(0);
        }
    }
}

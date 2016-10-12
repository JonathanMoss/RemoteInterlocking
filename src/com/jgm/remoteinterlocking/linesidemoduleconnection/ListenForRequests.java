package com.jgm.remoteinterlocking.linesidemoduleconnection;

import com.jgm.remoteinterlocking.Colour;
import static com.jgm.remoteinterlocking.RemoteInterlocking.getFailed;
import static com.jgm.remoteinterlocking.RemoteInterlocking.getOK;
import static com.jgm.remoteinterlocking.RemoteInterlocking.sendStatusMessage;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * This Class provides the functionality to Listen for connection requests from remote clients.
 * 
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class ListenForRequests extends Thread {

    private ServerSocket listeningSocket; // The server socket that will listen for incoming connection requests.
    private final int port; // The port number to listen for incoming connection requests.
    public static HashMap <String, ClientOutput> clientOutput = new HashMap<>(); // This map holds a reference to the Client Output Objects.
    private static int connectionRequests = 1; // A static integer that holds a tally of Connection Requests from remote clients.
    
    /**
     * This is the constructor method for the ListenForRequests.
     * This object listens for incoming connections on the port specified, creating a socket connection when a request is received.
     * 
     * @param port An <code>Integer</code> specifying the port number to listen for connection requests on.
     */
    public ListenForRequests (int port) {
        this.port = port;
    }
    
    @Override
    public void run() {
        try {
            this.listeningSocket = new ServerSocket(this.port, 100); // Listen for incoming connections.
            
            // Send a message to the console and Data Logger.
            sendStatusMessage(String.format ("%s%s%s",
                Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
                false, true);
            sendStatusMessage(String.format (" - %s[listening on port: %s]%s",
                Colour.BLUE.getColour(), this.listeningSocket.getLocalPort(), Colour.RESET.getColour()),
                true, true);
            
            do { 
                Socket sock = this.listeningSocket.accept(); // Wait for, and accept any incoming connection requests.
                // Set up the clientOutput and clientInput Streams and Threads.
                ClientOutput output;
                Thread threadOutput = new Thread (output = new ClientOutput(sock.getOutputStream()));
                threadOutput.setName("ClientOutput Thread");
                threadOutput.start();
                
                Thread threadInput = new Thread (new ClientInput (sock.getInputStream(), output));
                threadInput.setName("ClientInput Thread");
                threadInput.start();
                
                // Display a message to the console and Data Logger.
                sendStatusMessage(String.format("Connection request received [%s]", 
                    connectionRequests), 
                    true, true);
                sendStatusMessage(String.format ("Validating connection request [%s]...",
                    sock.getRemoteSocketAddress().toString().substring(1)), 
                    false, true);
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
    /**
     * This method is called when a connection to a Remote Client has been validated.
     * 
     * @param remoteClientIdentity A <code>String</code> that contains the Remote Client Identity.
     * @param output A <code>ClientOutput</code> object that is associated with the Remote Client Identity.
     */
    protected static synchronized void connectionValidated (String remoteClientIdentity, ClientOutput output) {
       
        if (clientOutput.containsKey(remoteClientIdentity)) {
            if (!clientOutput.get(remoteClientIdentity).equals(output)) {
                clientOutput.remove(remoteClientIdentity);
                clientOutput.put(remoteClientIdentity, output);
            }
        } else if (clientOutput.containsValue(output)) {
            clientOutput.remove(getKeyFromValue(clientOutput, output));
            clientOutput.put(remoteClientIdentity, output);
        } else {
            clientOutput.put(remoteClientIdentity, output);
            
        }
        
        sendStatusMessage(String.format ("%s%s%s",
            Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
            true, true);

    }
    
    // This method returns a key associated with a value.
    private static Object getKeyFromValue(Map hm, Object value) {
    
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
    
        return null;
  }
    
    /**
     * This method is used to get the ClientOutput object assigned to a particular client.
     * @param clientIdentity A <code>String</code> representing the Remote Client Identity
     * @return A <code>ClientOutput</code> object that is associated with the Remote Client passed in the first parameter.
     */
    protected static synchronized ClientOutput getClientOutput(String clientIdentity) {
        if (clientOutput.containsKey(clientIdentity)) { // Check if the HashMap contains the key...
            return clientOutput.get(clientIdentity); // If it does, return the value (ClientConnection)
        } else {
            return null; // Otherwise, return null.
        }
    }
    
    protected static synchronized String getClientIdentity (ClientOutput output) {
        return (String) getKeyFromValue (clientOutput, output);
    }
    
    
}

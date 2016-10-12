package com.jgm.remoteinterlocking.linesidemoduleconnection;

import com.jgm.remoteinterlocking.Colour;
import com.jgm.remoteinterlocking.RemoteInterlocking;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class provides the functionality to send messages to remote Clients.
 * 
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class ClientOutput extends DataOutputStream implements Runnable {
    
    private volatile Boolean connected = true; // This is a flag to indicate if there is a valid connection to the remote client.
    
    /**
     * This is the Constructor Method for the ClientOutput Class.
     * @param out An <code>OutputStream</code> object from a connected socket. 
     */
    protected ClientOutput(OutputStream out) {
        super(out);
    }

    /**
     * This method is called to set the Connected Flag.
     * 
     * This method should be called from the counterpart ClientInput object.
     * @param connected A <code>Boolean</code> indicating '<i>true</i>' if there is a valid connection, otherwise '<i>false</i>'.
     */
    protected synchronized void setConnected (Boolean connected) {
        this.connected = connected;
    }
    
    /**
     * This method sends a message to the Client.
     * @param message 
     */
    protected synchronized void sendMsgToRemoteClient (String message) {
        try {
            this.writeUTF(message); // Send the message.
            this.flush(); // Flush any remaiing bytes from the Output Buffer.
            // Send a message confirming that the message has been sent (An exception has not been thrown).
        } catch (IOException ex) { // There has been a problem, the message could not be sent.
            this.connected = false; // Set the connected flag to destroy this object/thread.
        }
    }
    
    @Override
    public void run() {
        while (this.connected) {} // Loop whilst connected.
    }

}

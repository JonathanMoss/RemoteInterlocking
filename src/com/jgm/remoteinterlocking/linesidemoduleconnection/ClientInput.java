package com.jgm.remoteinterlocking.linesidemoduleconnection;

import com.jgm.remoteinterlocking.Colour;
import com.jgm.remoteinterlocking.RemoteInterlocking;
import static com.jgm.remoteinterlocking.RemoteInterlocking.sendStatusMessage;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class provides objects with the functionality of receiving input from the DataInputStream.
 * 
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class ClientInput extends DataInputStream implements Runnable {
    
    private boolean connected = true;
    private String clientIdentity;
    private final ClientOutput clientOutput;

    /** 
     * This is the Constructor Method for the ClientInput Class.
     * @param in An <code>InputStream</code> object.
     * @param clientIdentity A <code>String</code> representing the identity of the Client. 
     * @param clientOutput An <code>ClientOutput</code> object that has been instantiated alongside this ClientInput object. 
     */
    protected ClientInput(InputStream in, String clientIdentity, ClientOutput clientOutput) {
        
        super(in);
        this.clientIdentity = clientIdentity;
        this.clientOutput = clientOutput;
        
    }
    
    /**
     * This method provides the ability to change the Client Identity associated with this object.
     * 
     * We need to do this once communication to a valid client have been established.
     * @param clientIdentity A <code>String</code> that contains the Client Identity
     */
    protected synchronized void setClientIdentity(String clientIdentity) {
        this.clientIdentity = clientIdentity;
    }
    
    @Override
    public void run() {
        while (connected) {
            try {
                // Read the input received on the InputDataStream.
                String message = this.readUTF(); 
                // Send the message to the MessageHandler Class.
                MessageHandler.incomingMessage(message, this.clientIdentity);
                
                // Display a message to the console and DataLogger.
                RemoteInterlocking.sendStatusMessage(String.format ("Message R/X: %s[%s]%s", 
                    Colour.BLUE.getColour(), message, Colour.RESET.getColour()),
                    true, true);
            } catch (IOException ex) { // There has been a problem
                // Display a message to the console and DataLogger.
                sendStatusMessage(String.format ("%s%s%s",
                    Colour.RED.getColour(), "WARNING: The Lineside Module has disconnected.", Colour.RESET.getColour()),
                    true, true);
                // Destroy this object by setting its connected flag to 'false'.
                this.connected = false;
                // Destroy the counterpart ClientOutput object by setting its connected flag to 'false'.
                this.clientOutput.setConnected(false);
                
            }
        }
    }
}



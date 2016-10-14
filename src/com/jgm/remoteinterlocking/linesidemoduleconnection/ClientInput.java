package com.jgm.remoteinterlocking.linesidemoduleconnection;

import com.jgm.remoteinterlocking.Colour;
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
    private final ClientOutput clientOutput;

    /** 
     * This is the Constructor Method for the ClientInput Class.
     * @param in An <code>InputStream</code> object.
     * @param clientOutput An <code>ClientOutput</code> object that has been instantiated alongside this ClientInput object. 
     */
    protected ClientInput(InputStream in, ClientOutput clientOutput) {
        
        super(in);
        this.clientOutput = clientOutput;
        
    }
    
    /**
     * This method returns the connected status flag of this object.
     * @return <code>Boolean</code> 'true' connected, otherwise 'false;.
     */
    protected synchronized Boolean getConnected() {
        return this.connected;
    }
    
    protected synchronized ClientOutput getClientOutput() {
        return this.clientOutput;
    }
    
    protected synchronized void setConnected (Boolean connected) {
        this.connected = false;
    }
    
    @Override
    public void run() {
        while (connected) {
            try {
                // Read the input received on the InputDataStream.
                String message = this.readUTF(); 
                // Send the message to the MessageHandler Class.
                MessageHandler.incomingMessage(message, this);
            } catch (IOException ex) { // There has been a problem
                // Display a message to the console and DataLogger.
                sendStatusMessage(String.format ("%sWARNING: The Remote Client [%s] has disconnected%s",
                    Colour.RED.getColour(), ListenForRequests.getClientIdentity(this.clientOutput), Colour.RESET.getColour()),
                    true, true);
                // Destroy this object by setting its connected flag to 'false'.
                this.connected = false;
                // Destroy the counterpart ClientOutput object by setting its connected flag to 'false'.
                this.clientOutput.setConnected(false);
                
            }
        }
    }
}



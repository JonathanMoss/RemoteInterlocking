package com.jgm.remoteinterlocking.linesidemoduleconnection;

import com.jgm.remoteinterlocking.Colour;
import com.jgm.remoteinterlocking.RemoteInterlocking;
import static com.jgm.remoteinterlocking.RemoteInterlocking.sendStatusMessage;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class ClientInput extends DataInputStream implements Runnable {
    
    private boolean connected = true;
    private String index;

    protected ClientInput(InputStream in, String index) {
        
        super(in);
        this.index = index;
        
    }
    
    protected synchronized void setLineSideModule(String lsmIdentity) {
        this.index = lsmIdentity;
    }
    
    @Override
    public void run() {
        while (connected) {
            try {
                String message = this.readUTF();
                MessageHandler.incomingMessage(message, this.index);
                RemoteInterlocking.sendStatusMessage(String.format ("Message R/X: %s[%s]%s", 
                    Colour.BLUE.getColour(), message, Colour.RESET.getColour()),
                    true, true);
            } catch (IOException ex) {
                sendStatusMessage(String.format ("%s%s%s",
                    Colour.RED.getColour(), "WARNING: The Lineside Module has disconnected.", Colour.RESET.getColour()),
                        true, true);
                ListenForRequests.lsModCon.get(this.index).output.setConnected(false);
                this.connected = false;
            }
        }
    }
}



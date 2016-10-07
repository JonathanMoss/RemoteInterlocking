package com.jgm.remoteinterlocking.linesidemoduleconnection;

import com.jgm.remoteinterlocking.Colour;
import com.jgm.remoteinterlocking.RemoteInterlocking;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class ClientOutput extends DataOutputStream implements Runnable {
    
    private Boolean connected = true;
    
    public ClientOutput(OutputStream out) {
        super(out);
    }

    public synchronized void setConnected (Boolean connected) {
        this.connected = connected;
    }
    
    public synchronized void sendMessageToLSM (String message) {
        try {
            this.writeUTF(message);
            this.flush();
            RemoteInterlocking.sendStatusMessage(String.format ("Message T/X: %s[%s]%s", 
                Colour.BLUE.getColour(), message, Colour.RESET.getColour()),
                true, true);
        } catch (IOException ex) {
            this.connected = false;
        }
    }
    
    @Override
    public void run() {
        while (this.connected) {
            
        }
    }

}

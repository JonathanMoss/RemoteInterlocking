package com.jgm.remoteinterlocking.linesidemoduleconnection;


import com.jgm.remoteinterlocking.Colour;
import com.jgm.remoteinterlocking.RemoteInterlocking;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class ClientInput extends DataInputStream implements Runnable {
    
    private boolean connected = true;
    private String index;

    public ClientInput(InputStream in, String index) {
        
        super(in);
        this.index = index;
        
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
                ex.printStackTrace();
            }
        }
    }
}



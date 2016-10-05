package com.jgm.remoteinterlocking.linesidemoduleconnection;

import static com.jgm.remoteinterlocking.RemoteInterlocking.sendStatusMessage;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class LinesideModuleClientConnection extends Thread {

    private final Socket socket;
    
    public LinesideModuleClientConnection (Socket socket) {
        this.socket = socket;
    }
    
    private void setUpStreams() {
        try {
            new Thread(new ClientInput(this.socket.getInputStream())).start();
        } catch (IOException ex) {
            Logger.getLogger(LinesideModuleClientConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @Override
    public void run() {
        setUpStreams();
        while (true) {
            
        }
    }
    
}

package com.jgm.remoteinterlocking.linesidemoduleconnection;

import com.jgm.remoteinterlocking.RemoteInterlocking;
import static com.jgm.remoteinterlocking.RemoteInterlocking.sendStatusMessage;
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
    public ClientOutput output;
    
    public LinesideModuleClientConnection (Socket socket) {
        this.socket = socket;
    }
    
    private void setUpStreams() {
        try {
            new Thread(new ClientInput(this.socket.getInputStream())).start();
            new Thread(output = new ClientOutput(this.socket.getOutputStream())).start();
            sendStatusMessage(String.format ("Validating connection request [%s]...",
                this.socket.getRemoteSocketAddress().toString().substring(1)), false, true);
        } catch (IOException ex) {}
    }

    @Override
    public void run() {
        setUpStreams();
    }
    
}

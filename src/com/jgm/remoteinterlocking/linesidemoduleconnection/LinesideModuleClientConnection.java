package com.jgm.remoteinterlocking.linesidemoduleconnection;

import static com.jgm.remoteinterlocking.RemoteInterlocking.sendStatusMessage;
import java.io.IOException;
import java.net.Socket;


/**
 *
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class LinesideModuleClientConnection extends Thread {

    private final Socket socket;
    public ClientOutput output;
    private String index;
    
    public LinesideModuleClientConnection (Socket socket, String index) {
        this.socket = socket;
        this.index = index;
    }
    
    private void setUpStreams() {
        try {
            new Thread(new ClientInput(this.socket.getInputStream(), this.index)).start();
            new Thread(output = new ClientOutput(this.socket.getOutputStream())).start();
            sendStatusMessage(String.format ("Validating connection request [%s]...",
                this.socket.getRemoteSocketAddress().toString().substring(1)), false, true);
        } catch (IOException ex) {}
    }

    @Override
    public void run() {
        setUpStreams();
    }

    void output() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

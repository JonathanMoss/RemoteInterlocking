package com.jgm.remoteinterlocking.linesidemoduleconnection;

import static com.jgm.remoteinterlocking.RemoteInterlocking.sendStatusMessage;
import java.io.IOException;
import java.net.Socket;

/**
 * This Class provides a Connection to the Remote Client.
 * 
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class ClientConnection extends Thread {

    private final Socket socket;
    public ClientOutput output;
    public ClientInput input;
    private final String index;
    
    /**
     * This is the Constructor Method for the Client Connection Class.
     * 
     * @param socket A <code>Socket</code> object (as a result of an accepted connection request).
     * @param index A <code>String</code> containing the identity of the Remote Client.
     */
    public ClientConnection (Socket socket, String index) {
        this.socket = socket;
        this.index = index;
    }
    
    private void setUpStreams() {
        try {
            // Attempt to create an OutputObject.
            new Thread(this.output = new ClientOutput(this.socket.getOutputStream())).start();
            
            // Attempt to create an InputObject.
            new Thread(this.input = new ClientInput(this.socket.getInputStream(), this.index, this.output)).start();
            
            // Send a message to the console and DataLogger
            sendStatusMessage(String.format ("Validating connection request [%s]...",
                this.socket.getRemoteSocketAddress().toString().substring(1)), false, true);
        } catch (IOException ex) {
            //TODO: Need to do something here?
        
        }
    }

    @Override
    public void run() {
        setUpStreams();
    }
    
}

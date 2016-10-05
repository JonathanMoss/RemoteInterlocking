package com.jgm.remoteinterlocking.linesidemoduleconnection;

import com.jgm.remoteinterlocking.Colour;
import static com.jgm.remoteinterlocking.RemoteInterlocking.getFailed;
import static com.jgm.remoteinterlocking.RemoteInterlocking.getOK;
import static com.jgm.remoteinterlocking.RemoteInterlocking.sendStatusMessage;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;

/**
 *
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class LinesideModuleListen extends Thread {

    private ServerSocket listeningSocket;
    private final int port;
    public LinesideModuleClientConnection lsModCon;
    private static int connectionRequests = 0;
    
    public LinesideModuleListen (int port) {
        this.port = port;
    }
    
    @Override
    public void run() {
        try {
            this.listeningSocket = new ServerSocket(this.port, 100);
            sendStatusMessage(String.format ("%s%s%s",
                Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
                false, true);
            sendStatusMessage(String.format (" - %s[listening on port: %s]%s",
                Colour.BLUE.getColour(), this.listeningSocket.getLocalPort(), Colour.RESET.getColour()),
                true, true);
            
            do {
                this.lsModCon = new LinesideModuleClientConnection(this.listeningSocket.accept());
                connectionRequests ++;
                sendStatusMessage(String.format("Connection request received [%s]", connectionRequests ), true, true);
                this.lsModCon.setName(String.format ("ConnectionThread [%s]", connectionRequests));
                this.lsModCon.start();
                 
            } while (true);
            
        } catch (BindException b) {
            sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                true, true);
            sendStatusMessage(String.format ("%sERROR: There is a problem with the listening port configuration, cannot continue.%s",
                Colour.RED.getColour(), Colour.RESET.getColour()), 
                true, true);
            System.exit(0);
        } catch (IOException ex) {
            sendStatusMessage(String.format ("%s%s%s",
                Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                true, true);
            sendStatusMessage(String.format ("%sERROR: Cannot listen for incoming connections from the LineSide Module(s), cannot continue.%s",
                Colour.RED.getColour(), Colour.RESET.getColour()), 
                true, true);
            System.exit(0);
        }
    }
}

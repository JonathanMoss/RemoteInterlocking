package com.jgm.remoteinterlocking.linesidemoduleconnection;

import com.jgm.remoteinterlocking.Colour;
import static com.jgm.remoteinterlocking.RemoteInterlocking.getFailed;
import static com.jgm.remoteinterlocking.RemoteInterlocking.getOK;
import static com.jgm.remoteinterlocking.RemoteInterlocking.sendStatusMessage;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class LinesideModuleListen extends Thread {

    private ServerSocket listeningSocket;
    private final int port;
    public static HashMap <String, LinesideModuleClientConnection> lsModCon = new HashMap<>();
    private static int connectionRequests = 1;
    
    public LinesideModuleListen (int port) {
        this.port = port;
    }
    
    public static void connectionValidated (String lsmIdentity, String index) {

        lsModCon.put(lsmIdentity, lsModCon.get(index));
        lsModCon.remove(index);
        
    }
    
    public static LinesideModuleClientConnection getLsmConnection(String lsmIdentity) {
        
        if (lsModCon.containsKey(lsmIdentity)) {
            return lsModCon.get(lsmIdentity);
        } else {
            return null;
        }
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
                lsModCon.put(Integer.toString(connectionRequests), new LinesideModuleClientConnection(this.listeningSocket.accept(), Integer.toString(connectionRequests)));
                lsModCon.get(Integer.toString(connectionRequests)).setName(String.format ("ConnectionThread [%s]", connectionRequests));
                lsModCon.get(Integer.toString(connectionRequests)).start();
                sendStatusMessage(String.format("Connection request received [%s]", connectionRequests ), true, true);
                connectionRequests ++;
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

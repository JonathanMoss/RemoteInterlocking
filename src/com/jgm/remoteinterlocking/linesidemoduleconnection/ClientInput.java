package com.jgm.remoteinterlocking.linesidemoduleconnection;

import static com.jgm.remoteinterlocking.RemoteInterlocking.sendStatusMessage;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class ClientInput extends DataInputStream implements Runnable {

    public ClientInput(InputStream in) {
        super(in);
    }

    @Override
    public void run() {
        while(true) {
            try {
                sendStatusMessage(this.readUTF(), true, true);
            } catch (IOException ex) {
                ex.printStackTrace();
                break;
            }
        }
    }
    
   
}

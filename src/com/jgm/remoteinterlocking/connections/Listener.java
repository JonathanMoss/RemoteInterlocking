package com.jgm.remoteinterlocking.connections;

import java.io.IOException;
import java.net.ServerSocket;


/**
 *
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class Listener extends Thread {

    private ServerSocket listen;
    
    public void run() {
        try {
            this.listen = new ServerSocket();
        } catch (IOException ex) {

        }
    }
    
}

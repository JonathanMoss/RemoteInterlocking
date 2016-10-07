package com.jgm.remoteinterlocking.linesidemoduleconnection;

import com.jgm.remoteinterlocking.RemoteInterlocking;
import static com.jgm.remoteinterlocking.RemoteInterlocking.validateModuleIdentity;
import java.util.HashMap;

/**
 * This abstract class provides the functionality of a message handler.
 * Each inter-module message is passed through this class.
 * 
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public abstract class MessageHandler {
    
    private static volatile HashMap<String, ClientOutput> output = new HashMap<>(); // This map contains the ClientOutput object references based on LSM identity.
    private static String lastMessageSent = ""; // String containing the last message sent.
    private static String lastMessageRecevied = ""; // String containing the last message received.
    
    // This method returns the ClientOutput object based on the Lineside Module identity.
    private static synchronized ClientOutput getOutputObject (String lsmIdentity) {
        return output.get(lsmIdentity);
    }
    
    /**
     * This method adds a reference to the ClientOutput object for each Lineside Module.
     * This is used to send targeted messages to only the correct Lineside Module.
     * @param lsmIdentity A <code>String</code> containing the Lineside Module Identity.
     * @param clientOutput A <code>ClientOutput</code> object that is associated with the Lineside Module passed into this method.
     */
    public static synchronized void addOutputObject (String lsmIdentity, ClientOutput clientOutput) {
        if (!output.containsKey(lsmIdentity) && validateModuleIdentity(lsmIdentity)) {
            output.put(lsmIdentity, clientOutput);
        }
    }
    
    // This method is used to format, and send the message.
    private static synchronized void sendMessage (String message, String lsModuleIdentity) {
        // Messages shall be formated thus:
        // SENDER|{ACK, }
    }
}

/**
 * This enum defines the types of inter-module communications.
 * 
 * ACK: Acknowledge previous message, the last part of the message must include the hashCode.
 * SETUP: Used during initial hand-shaking to setup up the assets between the LSM and RI.
 * RESEND: Used to 
 * @author JMoss2
 */
enum MESSAGE_TYPE {
    ACK, SETUP, RESEND, STATE_CHANGE;
}

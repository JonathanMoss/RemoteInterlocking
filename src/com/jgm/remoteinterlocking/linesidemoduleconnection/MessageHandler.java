package com.jgm.remoteinterlocking.linesidemoduleconnection;

import com.jgm.remoteinterlocking.Colour;
import com.jgm.remoteinterlocking.RemoteInterlocking;
import static com.jgm.remoteinterlocking.RemoteInterlocking.getOK;
import static com.jgm.remoteinterlocking.RemoteInterlocking.getRemoteInterlockingName;
import static com.jgm.remoteinterlocking.RemoteInterlocking.sendStatusMessage;
import static com.jgm.remoteinterlocking.RemoteInterlocking.validateModuleIdentity;
import static com.jgm.remoteinterlocking.linesidemoduleconnection.ListenForRequests.connectionValidated;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This abstract class provides the functionality of a message handler.
 * Each inter-module message is passed through this class.
 * 
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public abstract class MessageHandler {
    
    private static volatile ArrayList <Message> msgStack = new ArrayList<>();
    private static volatile HashMap <String, Boolean> handShakeNeeded = new HashMap<>();
    private static final String MESSAGE_END = "MESSAGE_END"; 
    
    private synchronized static boolean isHandShakeNeeded(String lsm) {
        if (handShakeNeeded.containsKey(lsm)) {
            if (handShakeNeeded.get(lsm)) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }      
    }
    
    private synchronized static void setHandShakeDone (String lsm) {
        
        handShakeNeeded.put(lsm, false);
   
    }
    
    /*  This method is used to format, and send the message.
        Messages shall be formated thus:
        SENDER|MessageType|MESSAGE|HASH|END_MESSAGE, e.g.
    */
    public static synchronized void sendMessage (String message, MessageType type, String lsModuleIdentity) {
        
        String messageStart = String.format ("%s|%s|%s",
            RemoteInterlocking.getRemoteInterlockingName(), type.toString(), message);
        int hashCode = messageStart.hashCode();
        
        ListenForRequests.getClientConnection(lsModuleIdentity).output.sendMessageToLSM(String.format("%s|%s|%s",
            messageStart, Integer.toString(hashCode), MESSAGE_END));
              
        switch (type) {
            case ACK:
                break;
            case SETUP:
                break;
            case STATE_CHANGE:
                break;
            case HAND_SHAKE:
                break;
            case NULL:
                break;
            case RESEND:
                break;
            }
        
    }
       
    public static synchronized void incomingMessage (String message, String index) {
        
        System.out.println("String index: " + index);
        String sender = "UNKNOWN";
        MessageType type;
        String messageText = "";
        int hashCode = 0;
        
        // Take the incoming message and split it into an array.
        String[] incomingMessage = message.split("\\|");
        
        try {
            // Check to see if the message is the correct length (Should contain 3, 4 or 5 parts)
            if (incomingMessage.length != 5) { 
                throw new Exception("Malformed message packet"); 
            }

            // Check that the first portion of the message contains something that resembles a LineSideModule Identity.
            if (!incomingMessage[0].matches("[0-9]{5}")) {
                throw new Exception("Malformed Lineside Module Identity received");
            } 

            // Validate the LineSideModule Identity.
            if (!validateModuleIdentity(incomingMessage[0])) {
                throw new Exception("Invalid Lineside Module Identity received");
            } else {
                sender = incomingMessage[0];
            }
            
            // Check that END_MESSAGE was the last part to be received.
            if (!incomingMessage[4].equals("END_MESSAGE")) {
                throw new Exception("Malformed message received");
            }
            
            type = MessageType.valueOf(incomingMessage[1]);
            
            if (incomingMessage[2] == null || incomingMessage[2].isEmpty()) {
                throw new Exception("Invalid message body");
            } else {
               messageText = incomingMessage[2];
            }
            
            if (String.format ("%s|%s|%s",sender, type.toString(), messageText).hashCode() != Integer.parseInt(incomingMessage[3])) {
                System.out.println(String.format ("%s|%s|%s",sender, type.toString(), messageText).hashCode() + ", " + Integer.parseInt(incomingMessage[3]));
                throw new Exception("Corrupted message, cannot validate hash");
            } else {
                hashCode = Integer.parseInt(incomingMessage[3]);
            }
            
            // Add the message to the message stack now it has been validated as much as possible.
            msgStack.add(new Message(sender, getRemoteInterlockingName(), MessageDirection.INCOMING, type, messageText, hashCode));
                
            switch (type) {
                case ACK:
                    break;
                case SETUP:
                    break;
                case STATE_CHANGE:
                    break;
                case HAND_SHAKE:
                    if (isHandShakeNeeded(sender)) {
                        connectionValidated(sender, index);
                        sendStatusMessage(String.format ("%s%s%s",
                            Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
                            true, true);
                        sendMessage(Integer.toString(hashCode), MessageType.ACK, sender);
                        setHandShakeDone(sender);
                        ListenForRequests.getClientConnection(sender).input.setClientIdentity(sender);
                    }
                    break;
                case NULL:
                    break;
                case RESEND: // Re-send the last message.
                    
                    break;
            }
            
        } catch (Exception e) {
            
            sendStatusMessage(String.format ("%sWARNING: Error in message received from '%s'%s - %s[%s]%s",
                Colour.RED.getColour(), sender, Colour.RESET.getColour(), Colour.BLUE.getColour(), e.getMessage(), Colour.RESET.getColour()), 
                true, true);
            
        }
        
    }

    
}



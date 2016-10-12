package com.jgm.remoteinterlocking.linesidemoduleconnection;

import com.jgm.remoteinterlocking.Colour;
import com.jgm.remoteinterlocking.RemoteInterlocking;
import static com.jgm.remoteinterlocking.RemoteInterlocking.getRemoteInterlockingName;
import static com.jgm.remoteinterlocking.RemoteInterlocking.sendStatusMessage;
import static com.jgm.remoteinterlocking.RemoteInterlocking.validateModuleIdentity;
import java.util.ArrayList;

/**
 * This abstract class provides the functionality of a message handler.
 * Each inter-module message is passed through this class.
 * 
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public abstract class MessageHandler {
    
    private static volatile ArrayList <Message> msgStack = new ArrayList<>(); // A Stack of Message Objects that are processed by the MessageProcess Thread.
    private static final String MESSAGE_END = "MESSAGE_END"; // Constant definition for the Message Ending.
    
    public synchronized static void processMessageStack() {
        
        while (msgStack.size() > 0) {
            switch (msgStack.get(0).getMsgDirection()) {
                case OUTGOING:
                    sendMessage(msgStack.get(0));
                    RemoteInterlocking.sendStatusMessage(String.format ("Message T/X (%s): %s[%s|%s|%s|%s|%s]%s", 
                        msgStack.get(0).getMsgReceiver(), Colour.BLUE.getColour(), msgStack.get(0).getMsgSender(), msgStack.get(0).getMsgType().toString(), msgStack.get(0).getMsgBody(), msgStack.get(0).getMsgHash(), MESSAGE_END, Colour.RESET.getColour()),
                    true, true);
                    break;
                case INCOMING:
                // Display a message to the console and DataLogger.
                    RemoteInterlocking.sendStatusMessage(String.format ("Message R/X: %s[%s|%s|%s|%s|%s]%s", 
                        Colour.BLUE.getColour(), msgStack.get(0).getMsgSender(), msgStack.get(0).getMsgType().toString(), msgStack.get(0).getMsgBody(), msgStack.get(0).getMsgHash(), MESSAGE_END, Colour.RESET.getColour()),
                    true, true);
                    switch (msgStack.get(0).getMsgType()) {
                        case HAND_SHAKE:
                            outGoingMessage(Integer.toString(msgStack.get(0).getMsgHash()), MessageType.ACK, msgStack.get(0).getMsgSender());
                            break;
                        case ACK:
                            break;
                        case SETUP:
                            break;
                        case STATE_CHANGE:
                            break;
                        case NULL:
                            break;
                        case RESEND: // Re-send the last message.
                            break;
                }
                    break;
            }
            
            msgStack.remove(0);
        } 
    }
    
    /**
     * This method correctly formats a message, and sends it to the Remote Client Specified
     * 
     * @param message A <code>String</code> containing the message body (content)
     * @param type A <code>MessageType</code> constant representing the meaning of / reason for the message.
     * @param remoteClientIdentity A <code>String</code> containing the Remote Client Identity.
     */
    private static synchronized void sendMessage (Message msg) {
        
        /*  
        *   Messages shall be constructed in the following format:
        *   SENDER|MessageType|MESSAGE|HASH|END_MESSAGE, e.g.
        *   '12345|HAND_SHAKE|NULL|-45362554762|END_MESSAGE'
        */
        
        // Format the message...
        String formattedMessage = String.format ("%s|%s|%s|%s|%s",
            msg.getMsgSender(), msg.getMsgType().toString(), msg.getMsgBody(), msg.getMsgHash(), MESSAGE_END);

        // Send the message to the correct Remote Client.
        ListenForRequests.getClientOutput(msg.getMsgReceiver()).sendMsgToRemoteClient(formattedMessage);
        
    }
    /**
     * This method is used to add an OutGoing Message to the Message Stack ready for processing.
     * 
     * @param message A <code>String</code> containing the message body (content)
     * @param type A <code>MessageType</code> constant representing the meaning of / reason for the message.
     * @param remoteClientIdentity A <code>String</code> containing the Remote Client Identity.
     */
    public static synchronized void outGoingMessage (String message, MessageType type, String remoteClientIdentity) {
        
        int hashCode = String.format ("%s|%s|%s",remoteClientIdentity, type.toString(), message).hashCode();
        msgStack.add(new Message(RemoteInterlocking.getRemoteInterlockingName(), remoteClientIdentity, MessageDirection.OUTGOING, type, message, hashCode, null, ListenForRequests.getClientOutput(remoteClientIdentity)));
        
    }
       
    public static synchronized void incomingMessage (String message, ClientInput input) {
        
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
            
            ListenForRequests.connectionValidated(sender, input.getClientOutput());
            
            // Add the message to the message stack now it has been validated as much as possible.
            msgStack.add(new Message(sender, getRemoteInterlockingName(), MessageDirection.INCOMING, type, messageText, hashCode, input, null));
            
        } catch (Exception e) {
            
            sendStatusMessage(String.format ("%sWARNING: Error in message received from '%s'%s - %s[%s]%s",
                Colour.RED.getColour(), sender, Colour.RESET.getColour(), Colour.BLUE.getColour(), e.getMessage(), Colour.RESET.getColour()), 
                true, true);
            
            // Destroy the connection on the streams.
            input.getClientOutput().setConnected(false);
            input.setConnected(false);
            
        }
        
    }

    
}



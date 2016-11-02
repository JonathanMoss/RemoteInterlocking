package com.jgm.remoteinterlocking.linesidemoduleconnection;

import com.jgm.remoteinterlocking.Colour;
import static com.jgm.remoteinterlocking.RemoteInterlocking.getControlledSignalAspect;
import static com.jgm.remoteinterlocking.RemoteInterlocking.getNonControlledSignalAspect;
import static com.jgm.remoteinterlocking.RemoteInterlocking.getPointsDetectionStatus;
import static com.jgm.remoteinterlocking.RemoteInterlocking.getPointsPosition;
import static com.jgm.remoteinterlocking.RemoteInterlocking.getRemoteInterlockingName;
import static com.jgm.remoteinterlocking.RemoteInterlocking.getTrainDetectionStatus;
import static com.jgm.remoteinterlocking.RemoteInterlocking.sendStatusMessage;
import static com.jgm.remoteinterlocking.RemoteInterlocking.setupAssets;
import static com.jgm.remoteinterlocking.RemoteInterlocking.updateControlledSignalAspect;
import static com.jgm.remoteinterlocking.RemoteInterlocking.updateNonControlledSignalAspect;
import static com.jgm.remoteinterlocking.RemoteInterlocking.updatePoints;
import static com.jgm.remoteinterlocking.RemoteInterlocking.updateTrainDetectionSection;
import static com.jgm.remoteinterlocking.RemoteInterlocking.validateModuleIdentity;
import com.jgm.remoteinterlocking.assets.Aspects;
import com.jgm.remoteinterlocking.assets.PointsPosition;
import com.jgm.remoteinterlocking.assets.TrainDetectionStatus;
import java.util.ArrayList;
import java.util.Arrays;


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
                    
                    break;
                case INCOMING:

                    switch (msgStack.get(0).getMsgType()) {
                        
                        case ACK:
                            break;
                        case STATE_CHANGE:
                            
                            String msgBody[] = msgStack.get(0).getMsgBody().split("\\.");
                             
                            if (Arrays.toString(msgBody).contains("POINTS")) {
                                // Example: POINTS.994.NORMAL.TRUE   
                                updatePoints(msgBody[1], PointsPosition.valueOf(msgBody[2]), Boolean.valueOf(msgBody[3]));
                                sendStatusMessage(String.format ("Points Status Update: %s[Points: %s, Position: %s, Detection: %s]%s",
                                    Colour.BLUE.getColour(), msgBody[1], getPointsPosition(msgBody[1]), getPointsDetectionStatus(msgBody[1]), Colour.RESET.getColour()),
                                    true, true);
       
                            } else if (Arrays.toString(msgBody).contains("CONTROLLED_SIGNAL")) {
                                // Example: CONTROLLED_SIGNAL.CE.105.RED
                                updateControlledSignalAspect(msgBody[1], msgBody[2], Aspects.valueOf(msgBody[3]));
                                sendStatusMessage(String.format ("Controlled Signal Status Update: %s[Signal: %s%s, Aspect: %s]%s",
                                    Colour.BLUE.getColour(), msgBody[1], msgBody[2], getControlledSignalAspect(msgBody[1], msgBody[2]), Colour.RESET.getColour()),
                                    true, true);  
                                
                            } else if (Arrays.toString(msgBody).contains("AUTOMATIC_SIGNALS")) {
                                // Example: AUTOMATIC_SIGNAL.CE.105.RED
                                updateNonControlledSignalAspect(msgBody[1], msgBody[2], Aspects.valueOf(msgBody[3]));
                                sendStatusMessage(String.format ("Non-Controlled Signal Status Update: %s[Signal: %s%s, Aspect: %s]%s",
                                    Colour.BLUE.getColour(), msgBody[1], msgBody[2], getNonControlledSignalAspect(msgBody[1], msgBody[2]), Colour.RESET.getColour()),
                                    true, true);
                                
                            } else if (Arrays.toString(msgBody).contains("TRAIN_DETECTION")) {
                                // Example: TRAIN_DETECTION.T117.OCCUPIED
                                updateTrainDetectionSection(msgBody[1], TrainDetectionStatus.valueOf(msgBody[2]));
                                sendStatusMessage(String.format ("Train Detection Section Status Update: %s[Section: %s, Status: %s]%s",
                                    Colour.BLUE.getColour(), msgBody[1], getTrainDetectionStatus(msgBody[1]), Colour.RESET.getColour()),
                                    true, true); 

                            }

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
        
        int hashCode = String.format ("%s|%s|%s", getRemoteInterlockingName(), type.toString(), message).hashCode();
        msgStack.add(new Message(getRemoteInterlockingName(), remoteClientIdentity, MessageDirection.OUTGOING, type, message, hashCode, null, ListenForRequests.getClientOutput(remoteClientIdentity)));
        
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
            if (!incomingMessage[4].equals(MESSAGE_END)) {
                throw new Exception("Malformed message received");
            }
            
            type = MessageType.valueOf(incomingMessage[1]);
            
            if (incomingMessage[2] == null || incomingMessage[2].isEmpty()) {
                throw new Exception("Invalid message body");
            } else {
               messageText = incomingMessage[2];
            }
            
            if (String.format ("%s|%s|%s",sender, type.toString(), messageText).hashCode() != Integer.parseInt(incomingMessage[3])) {
                throw new Exception("Corrupted message, cannot validate hash");
            } else {
                hashCode = Integer.parseInt(incomingMessage[3]);
            }
            
            setupAssets(sender);  // Check if we need to setup the Remote Client assets.
            ListenForRequests.connectionValidated(sender, input.getClientOutput());
            
            
            // Add the message to the message stack now it has been validated as much as possible.
            msgStack.add(new Message(sender, getRemoteInterlockingName(), MessageDirection.INCOMING, type, messageText, hashCode, input, null));
            
        } catch (Exception e) { // The message received has failed the check.
            
            // Send a warning message to the console and data logger.
            sendStatusMessage(String.format ("%sWARNING: Error in message received from '%s'%s - %s[%s]%s",
                Colour.RED.getColour(), sender, Colour.RESET.getColour(), Colour.BLUE.getColour(), e.getMessage(), Colour.RESET.getColour()), 
                true, true);
            
            // Destroy the connection on the streams.
            input.getClientOutput().setConnected(false); // Destroy the OutputClient object.
            input.setConnected(false); // Destroy the InputClient object.
            
        }   
    }
}



package com.jgm.remoteinterlocking.linesidemoduleconnection;

import com.jgm.remoteinterlocking.Colour;
import com.jgm.remoteinterlocking.RemoteInterlocking;
import static com.jgm.remoteinterlocking.RemoteInterlocking.getFailed;
import static com.jgm.remoteinterlocking.RemoteInterlocking.getOK;
import static com.jgm.remoteinterlocking.RemoteInterlocking.getRemoteInterlockingName;
import static com.jgm.remoteinterlocking.RemoteInterlocking.lsModListen;
import static com.jgm.remoteinterlocking.RemoteInterlocking.sendStatusMessage;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import static com.jgm.remoteinterlocking.RemoteInterlocking.validateModuleIdentity;


/**
 *
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class ClientInput extends DataInputStream implements Runnable {
    
    private Boolean firstMessage = true;
    private Boolean connected = true;
    private String[] incomingMessage;
    private Boolean waitingSetup = false;
    private Setup_Type setupType;
    private int processedPointTally = 0;
    private int incomingPointTally = 0;
    
    public ClientInput(InputStream in) {
        super(in);
    }

    @Override
    public void run() {
        while(connected) {
            try {
                // Read the message.
                incomingMessage = this.readUTF().split("\\|");
                
                // Check that the first portion of the message contains something that resembles a LineSideModule Identity.
                if (!incomingMessage[0].matches("[0-9]{5}")) {
                    this.connected = false;
                    throw new IOException();
                } 
                
                // Validate the LineSideModule Identity.
                if (!validateModuleIdentity(incomingMessage[0])) {
                    this.connected = false;
                    throw new IOException();
                } 
                
                if (this.firstMessage) {
                    // Confirm that the sendeing module has been validated (only need to do this once).
                    sendStatusMessage(String.format ("%s%s%s",
                        Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
                        true, true);
                }
                
                // Catch waitingSetup
                if (this.waitingSetup && !this.firstMessage) {
                    if (Arrays.toString(incomingMessage).contains("POINTS")) {
                        this.setupType = Setup_Type.POINTS;
                        this.incomingPointTally = Integer.parseInt(incomingMessage[2]); // This is how many points identities we will receive.
                        sendStatusMessage("Receiving Points details from the Lineside Module...", 
                            false, true);
                        sendStatusMessage(String.format ("%s%s%s",
                            Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()), 
                            false, true);
                        sendStatusMessage(String.format (" - %s[%s]%s",
                            Colour.BLUE.getColour(), this.incomingPointTally, Colour.RESET.getColour()), 
                            true, true);
                        System.out.println();

                        sendStatusMessage(String.format ("%s%-8s%-8s%-4s%s",
                            Colour.BLUE.getColour(), "Index", "Points", "Created", Colour.RESET.getColour()),
                            true, true);
                        sendStatusMessage(String.format ("%s%s%s",
                            Colour.BLUE.getColour(), "---------------------", Colour.RESET.getColour()),
                            true, true);
                        
                    } else if (Arrays.toString(incomingMessage).contains("CONTROLLED_SIGNALS")) {
                        
                    } else if (Arrays.toString(incomingMessage).contains("NON_CONTOLLED_SIGNALS")) {
                        
                    } else if (Arrays.toString(incomingMessage).contains("TRAIN_DETECTION")) {
                        
                    } else {
                        switch (this.setupType) {
                            case POINTS:
                                this.processedPointTally ++;
                                sendStatusMessage(String.format ("%s%-8s%-8s%-4s",
                                    Colour.BLUE.getColour(), this.processedPointTally + "/" + this.incomingPointTally, this.incomingMessage[1], Colour.RESET.getColour()),
                                        false, true);
                                RemoteInterlocking.addPointsToArray(this.incomingMessage[1], this.incomingMessage[0]);
                                if (this.incomingPointTally == this.processedPointTally) {
                                    this.setupType = Setup_Type.NOT_APPLICABLE;
                                }
                                break;
                            case CONTROLLED_SIGNALS:
                                break;
                            case NON_CONTROLLED_SIGNALS:
                                break;
                            case TRAIN_DETECTION:
                                break;
                            case NOT_APPLICABLE:
                                System.out.println(Arrays.toString(incomingMessage));
                                break;
                        }
                        System.out.println();
                    }
                }
                
                // Catch if the LSM is sending a setup request.
                if (Arrays.toString(incomingMessage).contains("SETUP")) {
                    sendStatusMessage(String.format("Waiting for setup request from Line Side Module [%s]...",
                        this.incomingMessage[0]),
                        false, true);
                    sendStatusMessage(String.format ("%s%s%s",
                        Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
                        true, true);
                    // Send the setup acknowledgement back to the LSM.
                    lsModListen.lsModCon.output.sendMessageToLSM(String.format ("%s|ACK|SETUP", getRemoteInterlockingName()));
                    this.firstMessage = false; // Remove the firstMessageFlag.
                    this.waitingSetup = true; // Set the WaitingSetup Flag.
                    
                }

            } catch (IOException ex) {
                
                sendStatusMessage(String.format ("%s%s%s",
                    Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                    true, true);
                sendStatusMessage("Listening for incoming connections from LineSide Modules...", 
                    true, true);
                break;
            }
        }
    }
    
   
}

enum Setup_Type {
    POINTS, CONTROLLED_SIGNALS, NON_CONTROLLED_SIGNALS, TRAIN_DETECTION, NOT_APPLICABLE;
}

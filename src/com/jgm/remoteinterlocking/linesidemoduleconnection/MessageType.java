package com.jgm.remoteinterlocking.linesidemoduleconnection;
/**
 * This Enum defines the types of inter-module communications.
 * 
 * ACK: Acknowledge previous message, the last part of the message must include the hashCode.
 * SETUP: Used during initial hand-shaking to setup up the assets between the LSM and RI.
 * STATE_CHANGE: Used to signify that the following message contains a state change directive.
 * HAND_SHAKE: Used during the initial setup of LSM > Remote Interlocking communications.
 * NULL: Used to signify the message content is not to be read.
 * RESEND: If there is an issue in the validation of the hash code, then RESEND requires that the last message is sent again.
 * 
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public enum MessageType {
    
    ACK, SETUP, STATE_CHANGE, HAND_SHAKE, NULL, RESEND;
    
}

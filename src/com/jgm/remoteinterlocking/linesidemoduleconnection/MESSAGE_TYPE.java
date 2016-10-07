/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jgm.remoteinterlocking.linesidemoduleconnection;

/**
 * This enum defines the types of inter-module communications.
 * 
 * ACK: Acknowledge previous message, the last part of the message must include the hashCode.
 * SETUP: Used during initial hand-shaking to setup up the assets between the LSM and RI.
 * STATE_CHANGE: 
 * HAND_SHAKE:
 * NULL:
 * RESEND:
 * @author JMoss2
 */
public enum MESSAGE_TYPE {
    ACK, SETUP, STATE_CHANGE, HAND_SHAKE, NULL, RESEND;
}

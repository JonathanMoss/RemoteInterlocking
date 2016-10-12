package com.jgm.remoteinterlocking.linesidemoduleconnection;

/**
 * The Message Class provides objects representing Message that have been received or that require transmitting.
 * 
 * @author Jonathan Moss
 * @version v1.0 October 2016.
 */

public class Message {

    private final String msgSender; // The sender of the message.
    private final String msgReceiver; // The intended receiver of the message.
    private final MessageDirection msgDirection; // The direction of the message, is the message being received or transmitted?
    private final MessageType msgType; // The type of the message, i.e. STATE_CHANGE, HAND_SHAKE etc...
    private final String msgBody; // The message body.
    private final int msgHash; // The hash of the relevant portion of the message.
    private final ClientInput input; // The ClientInput object assigned to the message (where relevant, otherwise null).
    private final ClientOutput output; // The ClientOutput object assigned to the message (where relevant, otherwise null).
    
    /**
    * This is the Constructor method for the Message Class object.
    *  
    * @param sender A <code>String</code> representing the identity of the message sender. 
    * @param receiver A <code>String</code> representing the identity of the intended recipient of the message.
    * @param direction A <code>MessageDirection</code> constant that determines if the message has been received, or requires T/X.
    * @param type A <code>MessageType</code> constant that informs the receiving module regarding the purpose of the message.
    * @param message A <code>String</code> that contains that actual message body text.
    * @param hash An <code>int</code> that contains the hashCode of the relevant portions of the message.
    * @param input A <code>ClientInput</code> object where the message was received (if relevant, otherwise null);
    * @param output A <code>ClientOutput</code> object where the message should be sent to (if relevant, otherwise null);
    */
    protected Message (String sender, String receiver, MessageDirection direction, MessageType type, String message, int hash, ClientInput input, ClientOutput output) {
        
        // Assign the values received in the constructor to the instance variables.
        this.msgBody = message;
        this.msgDirection = direction;
        this.msgHash = hash;
        this.msgReceiver = receiver;
        this.msgSender = sender;
        this.msgType = type;
        this.input = input;
        this.output = output;
        
    }
    /**
     * This method returns the ClientInput object associated with this message.
     * @return <code>ClientInput</Code> Object
     */
    protected ClientInput getInput() {
        return input;
    }
    
    /**
     * This method returns the ClientOutput associated with this message.
     * @return <code>ClientOutput</Code> Object
     */
    protected ClientOutput getOutput() {
        return output;
    }
    
    /**
     * This method returns the message sender identity.
     * @return <code>String</Code> The identity of the message sender
     */
    protected String getMsgSender() {
        return msgSender;
    }

    /**
     * This method returns the identity of the message receiver.
     * @return <code>String</Code> The identity of the message receiver
     */
    protected String getMsgReceiver() {
        return msgReceiver;
    }

    /**
     * This method returns the direction of the message.
     * @return <code>MessageDirection</Code> INCOMING = message received, OUTGOING = message requires transmitting.
     */
    protected MessageDirection getMsgDirection() {
        return msgDirection;
    }

    /**
     * This method returns the type of message.
     * @return <code>MessageType</Code> Indicates to the receiver the reason the message was sent.
     */
    protected MessageType getMsgType() {
        return msgType;
    }

    /**
     * This method returns the body of the message, i.e. the message content.
     * @return <code>String</Code> containing the message body.
     */
    protected String getMsgBody() {
        return msgBody;
    }

    /**
     * This method returns the hashCode of the relevant portions of the message.
     * @return <code>Integer</Code> containing the hash code of the message.
     */
    protected int getMsgHash() {
        return msgHash;
    }

}

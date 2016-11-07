package com.jgm.remoteinterlocking.assets;

import com.jgm.remoteinterlocking.tecui.TechniciansUserInterface;

/**
 * This Class provides Controlled Signal objects as referenced within the Interlocking / Remote Interlocking.
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class AutomaticSignal {

    private final String prefix, identity, lineSideModuleIdentity;
    private Aspects currentAspect;

     /**
      * This is the Constructor Method for a Non-Controlled Signal Object.
      * @param prefix <code>String</code> The prefix of the signal.
      * @param identity <code>String</code> The identity of the signal.
      * @param lineSideModuleIdentity <code>String</code> The identity of the Lineside Module associated with this signal.
      */
    public AutomaticSignal (String prefix, String identity, String lineSideModuleIdentity) {
        
        this.prefix = prefix;
        this.identity = identity;
        this.lineSideModuleIdentity = lineSideModuleIdentity;
        
    }
    
    /**
     * This method returns the current signal aspect.
     * @return <code>Aspects</code> the current signal aspect.
     */
    public Aspects getCurrentAspect() {
        return currentAspect;
    }

    /**
     * This method sets the current signal aspect.
     * 
     * Note: This method should only be called as a result of a request from the Lineside Module.
     * @param currentAspect <code>Aspect</code> the current signal aspect.
     */
    public void setCurrentAspect(Aspects currentAspect) {
        
        if (currentAspect != this.currentAspect) {
            this.currentAspect = currentAspect; 
            TechniciansUserInterface.updateSignalAspect(this);
        }
        

    }
    
    /**
     * This method returns the Signal prefix.
     * @return <code>String</code> the prefix of the signal.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * This method returns the identity of the signal.
     * @return <code>String</code> the identity of the signal.
     */
    public String getIdentity() {
        return identity;
    }
    
    /**
     * This method returns the Lineside Module Identity associated with the signal.
     * @return <code>String</code> the Lineside Module Identity.
     */
    public String getLineSideModuleIdentity() {
        return lineSideModuleIdentity;
    }
    
    public Boolean isSignalDisplayingMostRestrictiveAspect() {
        return this.currentAspect == Aspects.RED;
    }

}
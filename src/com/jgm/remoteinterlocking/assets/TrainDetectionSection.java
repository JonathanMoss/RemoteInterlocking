package com.jgm.remoteinterlocking.assets;

/**
 * This Class provides Train Detection Section objects as referenced within the Interlocking / Remote Interlocking.
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class TrainDetectionSection {
    
    private final String sectionIdentity; // The sectionIdentity of the points
    private final String linesideModuleIdentity; // The identity of the Lineside Module associated with this Train Detection Section.
    private TrainDetectionStatus status; // The status of the Train Detection Section - OCCUPIED or CLEAR.

    /**
     * This is the constructor method.
     * @param identity <code>String</code> the sectionIdentity of the Train Detection Section.
     * @param lsModuleIdentity <code>String</code> the sectionIdentity of the Lineside Module that the Train Detection Section is associated with.
     */
    public TrainDetectionSection (String identity, String lsModuleIdentity) {
        this.sectionIdentity = identity;
        this.linesideModuleIdentity = lsModuleIdentity;
    }
    
    /**
     * This method returns the status of the Train Detection Section.
     * @return <code>TrainDetectionStatus</code> <i>'OCCUPIED'</i> or <i>'CLEAR'</i>
     */
    public TrainDetectionStatus getStatus() {
        return status;
    }

    /**
     * This method sets the status of the Train Detection Status.
     * @param status 
     */
    public void setStatus(TrainDetectionStatus status) {
        this.status = status;
    }

    /**
     * This method returns the identity of the Train Detection Section.
     * @return <code>String</code> containing the identity of the Train Detection Section.
     */
    public String getSectionIdentity() {
        return sectionIdentity;
    }

    /**
     * This method returns the identity of the Lineside Module associated with the Train Detection Section.
     * @return <code>String</code> containing the identity of the Lineside Module.
     */
    public String getLinesideModuleIdentity() {
        return linesideModuleIdentity;
    }
}

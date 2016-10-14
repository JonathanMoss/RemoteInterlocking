package com.jgm.remoteinterlocking.assets;

/**
 * This Class provides Points objects as referenced within the Interlocking / Remote Interlocking.
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class Points {

    private final String identity; // The identity of the Points.
    private volatile PointsPosition position; // The position of the Points.
    private final String lsModuleIdentity; // The identity of the Lineside Module associated with these points.
    private volatile Boolean arePointsDetected; // Boolean value indicating 'true' of the points are detected, otherwise 'false'.
    
    /**
     * This is the constructor method for the Points Class.
     * @param pointsIdentity <code>String</code> containing the identity of the points.
     * @param lsModuleIdentity <code>String</code> containing the identity of the Lineside Module associated to the Points object.
     */
    public Points(String pointsIdentity, String lsModuleIdentity) {
        this.identity = pointsIdentity;
        this.lsModuleIdentity = lsModuleIdentity;
    }
    
    /**
     * This method returns the identity of the Points.
     * @return <code>String</code> containing the identity of the points.
     */
    public synchronized String getPointsIdentity () {
        return this.identity;
    }
    
    /**
     * This method sets the position of the Points.
     * 
     * Note: this method should only be called as a result of a status update from the Lineside Module.
     * @param position <code>PointsPosition</code> stating 'NORMAL', or 'REVERSE'
     */
    public synchronized void setPosition(PointsPosition position) {
        this.position = position;
    }
    
    /**
     * 
     * @param detection <code></code>
     */
    public synchronized void setDetection (Boolean detection) {
        this.arePointsDetected = detection;
    }
    
    /**
     * 
     * @return <code></code>
     */
    public synchronized PointsPosition getPointsPosition() {
        return this.position;
    }
    
    /**
     * 
     * @return 
     */
    public synchronized Boolean getDetectionStatus() {
        return this.arePointsDetected;
    }
    
}




package com.jgm.remoteinterlocking.assets;
/**
 *
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class Points {

    private final String identity;
    private volatile PointsPosition position;
    private final String lsModuleIdentity;
    private volatile Boolean arePointsDetected;
    
    public Points(String pointsIdentity, String lsModuleIdentity) {
        this.identity = pointsIdentity;
        this.lsModuleIdentity = lsModuleIdentity;
    }
    
    public synchronized String getPointsIdentity () {
        return this.identity;
    }
    
    public synchronized void setPosition(PointsPosition position) {
        this.position = position;
    }
    
    public synchronized void setDetection (Boolean detection) {
        this.arePointsDetected = detection;
    }
    
    public synchronized PointsPosition getPointsPosition() {
        return this.position;
    }
    
    public synchronized Boolean getDetectionStatus() {
        return this.arePointsDetected;
    }
    
}




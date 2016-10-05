package com.jgm.remoteinterlocking.assets;
/**
 *
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class Points {

    private final String identity;
    private Points_Position position;
    private final String lsModuleIdentity;
    private Boolean arePointsDetected;
    
    public Points(String pointsIdentity, String lsModuleIdentity) {
        this.identity = pointsIdentity;
        this.lsModuleIdentity = lsModuleIdentity;
    }
    
}

enum Points_Position {
    NORMAL, REVERSE, UNKNOWN;
}



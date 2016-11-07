package com.jgm.remoteinterlocking.assets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jonathan Moss
 * @version v1.0 November 2016
 */
public class SignalLamps {

    private final Map <Aspects, Boolean> LAMP_PROVEN = new HashMap<>();
    private final String prefix, identity;
    private static final ArrayList <SignalLamps> SIGNAL_LAMPS = new ArrayList<>();
    
    public SignalLamps (String prefix, String identity) {
        
        this.prefix = prefix;
        this.identity = identity;
        addToArray();
        
    }
    
    private void addToArray() {
        
        SIGNAL_LAMPS.add(this);
        
    }
    
    
    public void stateChange (Aspects aspect, Boolean lampProven) {
        
        LAMP_PROVEN.put(aspect, lampProven);

    }
    
    public Map getLampMap() {
        
        return this.LAMP_PROVEN;
        
    } 
    
    /**
     * This method returns the SignalLamp object, based on the prefix and identity passed in the parameters.
     * @param prefix <code>String</code> The Prefix of the Signal.
     * @param identity <code>String</code> The Identity of the Signal.
     * @return <code>SignalLamps</code> The SignalLamps object.
     */
    public static SignalLamps returnSignal (String prefix, String identity) {
        
        for (int i = 0; i < SIGNAL_LAMPS.size(); i++) {
        
            if (SIGNAL_LAMPS.get(i).prefix.equals(prefix) && SIGNAL_LAMPS.get(i).identity.equals(identity)) {
                return SIGNAL_LAMPS.get(i);
            }
            
        }
        
        return null;
    }
}

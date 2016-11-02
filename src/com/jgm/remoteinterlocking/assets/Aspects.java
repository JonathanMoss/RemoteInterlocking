package com.jgm.remoteinterlocking.assets;

/**
 * This Enum defines available Signal Aspects across all Signal Types.
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public enum Aspects {
    
    /**
     * Applicable to Position Light Signals or 4/3 Colour Light Signals with Co-acting Position Light (CA).
     */
    SUB_OFF, 
    
    /**
     * Applicable to all Colour Light Stop Signals, LOS, Buffer Stops and Fixed Red Signals. 
     */
    RED, 
    
    /**
     * Applicable to Banner Repeaters only.
     * The signal being repeated is showing a Stop aspect, and the train must be prepared to stop at that signal.
     */
    CAUTION, 
    
    /**
     * Applicable to Banner Repeaters only.
     * The signal being repeated is showing a proceed aspect.
     */
    CLEAR, 
    
    /**
     * Applicable to all Colour Light Signals.
     */
    YELLOW, 
    
    /**
     * Applicable to flashing aspect junction indication (3 and 4 aspect).
     * GREEN > FLASHING_DOUBLE_YELLOW > FLASHING YELLOW > YELLOW (This is the junction signal, it steps up to YY or G on approach - if able to do so), or
     * GREEN > FLASHING_YELLOW > YELLOW (This is the junction signal, it steps up to G on approach - if able to do so). 
     */
    FLASHING_YELLOW, 
    
    /**
     * Applicable to 4 aspect Colour Light Signals only.
     * The next signal is displaying a yellow aspect.
     */
    DOUBLE_YELLOW, 

    /**
     * Applicable to flashing aspect junction indication (4 aspect).
     * GREEN > FLASHING_DOUBLE_YELLOW > FLASHING YELLOW > YELLOW (This is the junction signal, it steps up to YY or G on approach - if able to do so).
     */
    FLASHING_DOUBLE_YELLOW, 
    
    /**
     * Applicable to 4 aspect Colour Light Signals only, where the bottom yellow lamp has blown.
     */
    TOP_YELLOW, 
    
    /**
     * Applicable to Colour Light Signals.
     */
    GREEN,
    
    /**
     * Applicable to Colour Light Signals only.
     * There is no aspect or indication being displayed at the Signal.
     */
    BLACK, 

    /**
     * Only applicable to POSA.
     * 2 flashing white lights.
     */
    FLASHING_WHITE, 
    
    /**
     * Only applicable for SPAD Indicators.
     * Steady red light between two flashing red lights arranged vertically.
     */
    SPAD_INDICATOR_ILLUMINATED;
    
}

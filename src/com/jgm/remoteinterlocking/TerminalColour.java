package com.jgm.remoteinterlocking;

/**
 * This enum provides the ANSI TerminalColour Codes, used to present colour output in the console.
 * @author Jonathan Moss
 */
public enum TerminalColour {
    
    BLACK("\u001B[30m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    PURPLE("\u001B[35m"),
    CYAN("\u001B[36m"),
    WHITE("\u001B[37m"),
    RESET("\u001B[0m");
    
    private final String uniCode;
    
    TerminalColour (String uniCode) {
        this.uniCode = uniCode;
        
    }
    
    /**
     * This method returns the ANSI Escape sequence for the applicable colour.
     * @return A <code>String</code> representing the ANSI escape sequence for the applicable colour.
     */
    public String getColour () {
        
        return uniCode;
        
    }
}


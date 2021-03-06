package com.jgm.remoteinterlocking.tecui;

import com.jgm.remoteinterlocking.assets.Points;
import java.util.ArrayList;
import javafx.scene.control.RadioButton;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

/**
 *
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class IndividualPointSwitch {

    private final Circle blackKnob;
    private Line indicatorLine;
    private final RadioButton[] pointIndication;
    
    
    public IndividualPointSwitch (ArrayList <Points> pointEnds, Circle blackKnob, Line indicatorLine, RadioButton[] pointIndication) {
        
        this.blackKnob = blackKnob;
        this.indicatorLine = indicatorLine;
        this.pointIndication = pointIndication;
        this.indicatorLine.setRotate(-37.1);
        this.pointIndication[1].setSelected(true);
        
        for (RadioButton pIn : this.pointIndication) {
            
            pIn.setDisable(true);
            
        }
        
        this.blackKnob.setOnMouseClicked(e -> {
        
            if (null != e.getButton()) switch (e.getButton()) {
                case PRIMARY:
                    this.indicatorLine.setRotate(-37.1);
                    break;
                case MIDDLE:
                    this.indicatorLine.setRotate(0);
                    break;
                case SECONDARY:
                    this.indicatorLine.setRotate(37.1);
                    break;
                default:
                    break;
            }
        
        });
        

        
    }
    
}

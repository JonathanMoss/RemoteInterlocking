package com.jgm.remoteinterlocking.tecui;

import com.jgm.remoteinterlocking.RemoteInterlocking;
import com.jgm.remoteinterlocking.assets.Aspects;
import com.jgm.remoteinterlocking.assets.AutomaticSignal;
import com.jgm.remoteinterlocking.assets.ControlledSignal;
import com.jgm.remoteinterlocking.linesidemoduleconnection.MessageHandler;
import com.jgm.remoteinterlocking.linesidemoduleconnection.MessageType;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 *
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class TechniciansUserInterface extends Application implements Runnable{

    private static final HashMap <AutomaticSignal, Circle> AUT_SIG = new HashMap<>();
    private static final HashMap <ControlledSignal, Circle> CON_SIG = new HashMap<>();
    private static Stage stage;
    private static Scene scene;
    
    // Automatic Signal Menu Items
    private static final ContextMenu AUTO_MENU = new ContextMenu();
    private static final MenuItem AUTO_MENU_REPLACE = new MenuItem ("Operate Signal Replacement");
    private static final MenuItem AUTO_MENU_RESTORE = new MenuItem ("Restore Signal Replacement");
    
    // Controlled Signal Menu Items
    private static final ContextMenu CONT_MENU = new ContextMenu();
    private static final MenuItem CONT_CLEAR_SIGNAL = new MenuItem ("Clear Signal");
    private static final MenuItem CONT_REPLACE_SIGNAL = new MenuItem ("Replace Signal to Danger");
    private static final Menu CONT_RESTRICT_ASPECT = new Menu("Clear with Restricted Aspect");
    private static final MenuItem[] CONT_RESTRICTED_ASPECTS = {new MenuItem("Yellow"), new MenuItem("Double Yellow")};
    
    // All Menu Items
    private static final Menu ALL_SUB = new Menu("Lamp Proving");
    private static final CheckMenuItem[] ALL_LAMP_PROVING = {new CheckMenuItem ("Red"), new CheckMenuItem ("Bottom Yellow"), new CheckMenuItem ("Top Yellow"), new CheckMenuItem ("Green"),};

    private static ComboBox CE175_RouteSelect;
    private static ComboBox CE184_RouteSelect;
    private static ComboBox CE193_RouteSelect;
    private static ComboBox CE521_RouteSelect;
    private static ComboBox CE524_RouteSelect;
    
    @Override
    public void start(Stage primaryStage) throws Exception {

        stage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        
        // Build Menus
        for (int i=0; i < ALL_LAMP_PROVING.length; i++) {
            ALL_LAMP_PROVING[i].setSelected(true);
        }
        ALL_SUB.getItems().addAll(ALL_LAMP_PROVING);
        AUTO_MENU.getItems().addAll(AUTO_MENU_REPLACE, AUTO_MENU_RESTORE, new SeparatorMenuItem(), ALL_SUB);
        CONT_RESTRICT_ASPECT.getItems().addAll(CONT_RESTRICTED_ASPECTS);
        CONT_MENU.getItems().addAll(CONT_CLEAR_SIGNAL, CONT_REPLACE_SIGNAL, new SeparatorMenuItem(), CONT_RESTRICT_ASPECT, new SeparatorMenuItem(), ALL_SUB);
        
        //Populate Route Selects.
        CE175_RouteSelect = (ComboBox)scene.lookup("#CE175_RouteSelect");
        CE175_RouteSelect.getItems().addAll("CE183", "CE181", "CE179", "Siding");
        
        CE521_RouteSelect = (ComboBox)scene.lookup("#CE521_RouteSelect");
        CE521_RouteSelect.getItems().addAll("CE181", "CE179", "Siding");
        
        CE524_RouteSelect = (ComboBox)scene.lookup("#CE524_RouteSelect");
        CE524_RouteSelect.getItems().addAll("SOT474", "CE178");
        
        CE193_RouteSelect = (ComboBox)scene.lookup("#CE193_RouteSelect");
        CE193_RouteSelect.getItems().addAll("CE119", "CE121");
        
        CE184_RouteSelect = (ComboBox)scene.lookup("#CE184_RouteSelect");
        CE184_RouteSelect.getItems().addAll("CE178", "SOT474");
        
        // Iterate through the Automatic Signals and build the hashmap.
        ArrayList <AutomaticSignal> automaticSignals = RemoteInterlocking.getAutomaticSignalsList();
        for (int i = 0; i < automaticSignals.size(); i++) {
            
            String lookUp = String.format ("#%s%s", automaticSignals.get(i).getPrefix(), automaticSignals.get(i).getIdentity());
            Circle circle = (Circle) scene.lookup(lookUp);
            if (circle != null) {
                AUT_SIG.put(automaticSignals.get(i), circle);
                updateSignalAspect(automaticSignals.get(i));
                String prefix = automaticSignals.get(i).getPrefix();
                String identity = automaticSignals.get(i).getIdentity();
                String remoteClientID = automaticSignals.get(i).getLineSideModuleIdentity();
                circle.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                
                    if (e.getButton() == MouseButton.SECONDARY) {
                        showAutomaticSignalMenu(prefix, identity, remoteClientID);
                        AUTO_MENU.show(stage, e.getScreenX(), e.getScreenY());
                    }
                    
                    
                });
                
            } else {
                
                System.out.println(String.format ("Cant find the circle! [%s%s]", automaticSignals.get(i).getPrefix(), automaticSignals.get(i).getIdentity()));
            
            }
            
        }
        
        // Iterate through the Controlled Signals and build the hashmap.
        ArrayList <ControlledSignal> controlledSignals = RemoteInterlocking.getControlledSignalsList();
        for (int i = 0; i < controlledSignals.size(); i++) {
            
            String lookUp = String.format ("#%s%s", controlledSignals.get(i).getPrefix(), controlledSignals.get(i).getIdentity());
            Circle circle = (Circle) scene.lookup(lookUp);
            if (circle != null) {
                CON_SIG.put(controlledSignals.get(i), circle);
                updateSignalAspect(controlledSignals.get(i));
                String prefix = controlledSignals.get(i).getPrefix();
                String identity = controlledSignals.get(i).getIdentity();
                String remoteClientID = controlledSignals.get(i).getLineSideModuleIdentity();
                circle.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                
                    if (e.getButton() == MouseButton.SECONDARY) {
                        showControlledSignalMenu(prefix, identity, remoteClientID);
                        CONT_MENU.show(stage, e.getScreenX(), e.getScreenY());
                    }
                    
                    
                });
            } else {
                
                System.out.println(String.format ("Cant find the circle! [%s%s]", controlledSignals.get(i).getPrefix(), controlledSignals.get(i).getIdentity()));
            
            }

        }
    }
    
    private void showControlledSignalMenu (String prefix, String identity, String remoteClient) {
    
        CONT_CLEAR_SIGNAL.setOnAction(e -> {    
            String fullIdentity = String.format ("%s%s", prefix, identity);
            String msg;
            String selection;
            switch (fullIdentity) {
                case "CE175": // ->183, 181, 179, Siding
                    selection = CE175_RouteSelect.getValue().toString();
                    if (selection != null) {
                        switch (selection) {
                            case "CE183":
                                msg = "CONTROLLED_SIGNAL.CE.175.CE.183.MAIN.null";
                                MessageHandler.outGoingMessage(msg, MessageType.REQUEST, remoteClient);
                                break;
                            case "CE181":
                                msg = "CONTROLLED_SIGNAL.CE.175.CE.181.MAIN.null";
                                MessageHandler.outGoingMessage(msg, MessageType.REQUEST, remoteClient);
                                break;
                            case "CE179":
                                msg = "CONTROLLED_SIGNAL.CE.175.CE.179.MAIN.null";
                                MessageHandler.outGoingMessage(msg, MessageType.REQUEST, remoteClient);
                                break;
                            case "Siding":
                                msg = "CONTROLLED_SIGNAL.CE.175.CE.BS1.MAIN.null";
                                MessageHandler.outGoingMessage(msg, MessageType.REQUEST, remoteClient);
                                break;
                        }
                    }
                    break;
                case "CE183": // ->185
                    msg = "CONTROLLED_SIGNAL.CE.183.CE.185.MAIN.null";
                    MessageHandler.outGoingMessage(msg, MessageType.REQUEST, remoteClient);
                    break;
                case "CE185": // ->189
                    msg = "CONTROLLED_SIGNAL.CE.185.CE.189.MAIN.null";
                    MessageHandler.outGoingMessage(msg, MessageType.REQUEST, remoteClient);
                    break;
                case "CE189": // ->191
                    msg = "CONTROLLED_SIGNAL.CE.189.CE.191.MAIN.null";
                    MessageHandler.outGoingMessage(msg, MessageType.REQUEST, remoteClient);
                    break;
                case "CE193": // ->119, 121
                    selection = CE193_RouteSelect.getValue().toString();
                    if (selection != null) {
                        switch (selection) {
                            case "CE119":
                                msg = "CONTROLLED_SIGNAL.CE.193.CE.119.MAIN.null";
                                MessageHandler.outGoingMessage(msg, MessageType.REQUEST, remoteClient);
                                break;
                            case "CE121":
                                msg = "CONTROLLED_SIGNAL.CE.193.CE.121.MAIN.null";
                                MessageHandler.outGoingMessage(msg, MessageType.REQUEST, remoteClient);
                                break;
                        }
                    }
                    break;
                case "CE196": // ->192
                    msg = "CONTROLLED_SIGNAL.CE.196.CE.192.MAIN.null";
                    MessageHandler.outGoingMessage(msg, MessageType.REQUEST, remoteClient);
                    break;
                case "CE198": // ->192
                    msg = "CONTROLLED_SIGNAL.CE.198.CE.192.MAIN.null";
                    MessageHandler.outGoingMessage(msg, MessageType.REQUEST, remoteClient);
                    break;
                case "CE190": // ->186
                    msg = "CONTROLLED_SIGNAL.CE.190.CE.186.MAIN.null";
                    MessageHandler.outGoingMessage(msg, MessageType.REQUEST, remoteClient);
                    break;
                case "CE186": // ->184
                    msg = "CONTROLLED_SIGNAL.CE.186.CE.184.MAIN.null";
                    MessageHandler.outGoingMessage(msg, MessageType.REQUEST, remoteClient);
                    break;
                case "CE184": // ->SOT474, CE178
                    selection = CE184_RouteSelect.getValue().toString();
                    if (selection != null) {
                        switch (selection) {
                            case "SOT474":
                                msg = "CONTROLLED_SIGNAL.CE.184.SOT.474.MAIN.null";
                                MessageHandler.outGoingMessage(msg, MessageType.REQUEST, remoteClient);
                                break;
                            case "CE178":
                                msg = "CONTROLLED_SIGNAL.CE.184.CE.178.MAIN.null";
                                MessageHandler.outGoingMessage(msg, MessageType.REQUEST, remoteClient);
                                break;
                        }
                    }
                    break;
                case "CE176": // ->SOT474
                    msg = "CONTROLLED_SIGNAL.CE.176.SOT.474.MAIN.null";
                    MessageHandler.outGoingMessage(msg, MessageType.REQUEST, remoteClient);
                    break;
                case "CE178": // ->SOT474
                    msg = "CONTROLLED_SIGNAL.CE.178.SOT.474.MAIN.null";
                    MessageHandler.outGoingMessage(msg, MessageType.REQUEST, remoteClient);
                    break;
                case "CE179": // ->CE181
                    msg = "CONTROLLED_SIGNAL.CE.179.CE.181.MAIN.null";
                    MessageHandler.outGoingMessage(msg, MessageType.REQUEST, remoteClient);
                    break;
                case "CE521": // ->181, 179, Siding
                    selection = CE521_RouteSelect.getValue().toString();
                    if (selection != null) {
                        switch (selection) {
                            case "CE181":
                                msg = "CONTROLLED_SIGNAL.CE.521.CE.181.SHUNT.null";
                                MessageHandler.outGoingMessage(msg, MessageType.REQUEST, remoteClient);
                                break;
                            case "CE179":
                                msg = "CONTROLLED_SIGNAL.CE.521.CE.179.SHUNT.null";
                                MessageHandler.outGoingMessage(msg, MessageType.REQUEST, remoteClient);
                                break;
                        }
                    }
                case "CE524": // ->SOT474, CE178
                    selection = CE524_RouteSelect.getValue().toString();
                    if (selection != null) {
                        switch (selection) {
                            case "CE181":
                                msg = "CONTROLLED_SIGNAL.CE.524.SOT.474.SHUNT.null";
                                MessageHandler.outGoingMessage(msg, MessageType.REQUEST, remoteClient);
                                break;
                            case "CE179":
                                msg = "CONTROLLED_SIGNAL.CE.524.CE.178.SHUNT.null";
                                MessageHandler.outGoingMessage(msg, MessageType.REQUEST, remoteClient);
                                break;
                        }
                    }
                case "CE181": // ->185
                    msg = "CONTROLLED_SIGNAL.CE.181.CE.185.MAIN.null";
                    MessageHandler.outGoingMessage(msg, MessageType.REQUEST, remoteClient);
                    break;
            }
        
        });
        
        CONT_REPLACE_SIGNAL.setOnAction(e -> {
        
            String message;
            message = String.format ("CONTROLLED_SIGNAL.%s.%s.null.null.SIGNAL_ON.null", prefix, identity);
            MessageHandler.outGoingMessage(message, MessageType.REQUEST, remoteClient);   
        
        });
        
    }
    
    private void showAutomaticSignalMenu(String prefix, String identity, String remoteClient) {
        
        AUTO_MENU_REPLACE.setOnAction(e -> {
        
            String message = String.format("AUTOMATIC_SIGNAL.%s.%s.false", prefix, identity);
            MessageHandler.outGoingMessage(message, MessageType.REQUEST, remoteClient);
        
        });
        
        AUTO_MENU_RESTORE.setOnAction(e -> {
        
            String message = String.format("AUTOMATIC_SIGNAL.%s.%s.true", prefix, identity);
            MessageHandler.outGoingMessage(message, MessageType.REQUEST, remoteClient);
        
        });
        
    }
    
    public synchronized static void updateSignalAspect (Object signal) {
        
        if (signal instanceof ControlledSignal) {
            
            CON_SIG.forEach((key, value) -> {
            
                if (key.equals(signal)) {
                    value.setFill(getAspectSymbol(key.getCurrentAspect()));
                }
                
            });
            
        } else {
            
            AUT_SIG.forEach((key, value) -> {
            
                if (key.equals(signal)) {
                    value.setFill(getAspectSymbol(key.getCurrentAspect()));
                }
                
            });
            
        }    
    }
        
    private static Color getAspectSymbol (Aspects signalAspect) {
        
        switch (signalAspect) {
            case SUB_OFF:
            case FLASHING_WHITE:
                return Color.WHITE;
            case RED:
            case SPAD_INDICATOR_ILLUMINATED:
                return Color.RED;
            case CAUTION:
            case YELLOW:
            case DOUBLE_YELLOW:
            case TOP_YELLOW:
            case FLASHING_DOUBLE_YELLOW:
            case FLASHING_YELLOW:
                return Color.YELLOW;
            case CLEAR:
            case GREEN:
                return Color.GREEN;
            case BLACK:
            default: 
                return Color.SLATEGRAY;

        }
    }

    @Override
    public void run() {
        
    }

}

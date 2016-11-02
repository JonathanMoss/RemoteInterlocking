package com.jgm.remoteinterlocking.tecui;

import com.jgm.remoteinterlocking.RemoteInterlocking;
import com.jgm.remoteinterlocking.assets.Aspects;
import com.jgm.remoteinterlocking.assets.AutomaticSignal;
import com.jgm.remoteinterlocking.assets.ControlledSignal;
import com.jgm.remoteinterlocking.linesidemoduleconnection.MessageHandler;
import com.jgm.remoteinterlocking.linesidemoduleconnection.MessageType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 *
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class TechniciansUserInterface extends Application implements Runnable{

    private static ArrayList <ControlledSignal> CON_SIG  = new ArrayList<>();
    private static ArrayList <AutomaticSignal> AUT_SIG = new ArrayList<>();
    private static final Map <Object, Integer> ASSET_MAP = new HashMap<>();
    private static final Map <Integer, Label> ASPECT_MAP = new HashMap<>();
    private static final GridPane SIG_GRID = new GridPane();
    private ContextMenu AutoSignalMenu;
    private MenuItem AutoSignalMenu_Replace;
    private MenuItem AutoSignalMenu_Restore;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        
        //Build Automatic Signal Menu.
        AutoSignalMenu = new ContextMenu();
        AutoSignalMenu_Replace = new MenuItem();
        AutoSignalMenu_Replace.setText("Replace Signal to most restrictive aspect");
        AutoSignalMenu_Restore = new MenuItem();
        AutoSignalMenu_Restore.setText("Restore signal to automatic working");
        AutoSignalMenu.getItems().addAll(AutoSignalMenu_Replace, AutoSignalMenu_Restore);
        primaryStage.setTitle("Technicians User Interface");
        primaryStage.setOnCloseRequest(e-> {
            
            e.consume();
            System.exit(0);
        
        });
        
        TabPane tabPane = new TabPane();
        Tab[] tab = {new Tab("Signals"), new Tab("Points"), new Tab("Train Detection")};
        tabPane.getTabs().addAll(tab);
        
        // Controlled Signals
       
        CON_SIG = RemoteInterlocking.getControlledSignalsList();
        for (int i = 0; i < CON_SIG.size(); i++) {
            ASSET_MAP.put(CON_SIG.get(i), i);
        }
        
        int currentSize = ASSET_MAP.size();
        AUT_SIG = RemoteInterlocking.getAutomaticSignalsList();
        for (int i = 0; i < AUT_SIG.size(); i++) {
            ASSET_MAP.put(AUT_SIG.get(i), (i + currentSize));
        }
        
        SIG_GRID.setHgap(1);
        SIG_GRID.setVgap(1);
        SIG_GRID.setPadding(new Insets(5,10,0,10));
        
        ASSET_MAP.forEach((key, value) -> {
            
            if (key instanceof ControlledSignal) {
                SIG_GRID.add(new Label(((ControlledSignal) key).getPrefix()), 1, value);
                SIG_GRID.add(new Label(((ControlledSignal) key).getIdentity()), 2, value);
                
                Label temp = new Label();
                getAspectSymbol(temp, ((ControlledSignal) key).getCurrentAspect());
                ASPECT_MAP.put(value, temp);

                SIG_GRID.add(ASPECT_MAP.get(value), 3, value);
                
            } else {
                
                SIG_GRID.add(new Label(((AutomaticSignal) key).getPrefix()), 1, value);
                SIG_GRID.add(new Label(((AutomaticSignal) key).getIdentity()), 2, value);
                
                Label temp = new Label();
                temp.setOnMouseClicked(e->{
                    
                   if (e.isAltDown())  {
                       
                       MessageHandler.outGoingMessage(String.format ("AUTOMATIC_SIGNAL.%s.%s.false",
                            ((AutomaticSignal) key).getPrefix(),
                            ((AutomaticSignal) key).getIdentity()), 
                            MessageType.REQUEST, 
                            ((AutomaticSignal) key).getLineSideModuleIdentity());
                            AutoSignalMenu.show(primaryStage, e.getScreenX(), e.getScreenY());
                       
                   } else {
                       
                       MessageHandler.outGoingMessage(String.format ("AUTOMATIC_SIGNAL.%s.%s.true",
                            ((AutomaticSignal) key).getPrefix(),
                            ((AutomaticSignal) key).getIdentity()), 
                            MessageType.REQUEST, 
                            ((AutomaticSignal) key).getLineSideModuleIdentity());
                       
                   }
                
                });
                
                getAspectSymbol(temp, ((AutomaticSignal) key).getCurrentAspect());
                ASPECT_MAP.put(value, temp);
                
                SIG_GRID.add(ASPECT_MAP.get(value), 3, value);
            }
        
        });
        
        ScrollPane scroll = new ScrollPane();
        scroll.setContent(SIG_GRID);
        tab[0].setContent(scroll);
        Scene scene = new Scene (tabPane, 1024, 768);
        primaryStage.setScene(scene);
        primaryStage.show();
        
    }
    
    public synchronized static void updateSignalAspect (Object signal) {
        

        if (signal instanceof ControlledSignal) {
            ASSET_MAP.forEach((k,v)->{
            if (k.equals(signal)) {
                getAspectSymbol(ASPECT_MAP.get(v), ((ControlledSignal) signal).getCurrentAspect());
            }
        
            });
        } else {
            ASSET_MAP.forEach((k,v)->{
            if (k.equals(signal)) {
                getAspectSymbol(ASPECT_MAP.get(v), ((AutomaticSignal) signal).getCurrentAspect());
            }
        
            });
        }
        
        
    } 
    
    private static void getAspectSymbol (Label aspectLabel, Aspects signalAspect) {
        
        aspectLabel.setFont(new Font(30));
        
        if (signalAspect == null) {
            
            aspectLabel.setText(String.format("%s", "\u25ce"));
            
        } else {
        
        switch (signalAspect) {
            case SUB_OFF: //2687
                aspectLabel.setText(String.format("%s", "\u2681"));
                break;
            case RED:
                aspectLabel.setText(String.format("%s", "\u25cf"));
                aspectLabel.setTextFill(Color.RED);
                break;
                
            case CAUTION:
            case YELLOW:
                aspectLabel.setText(String.format("%s", "\u25cf"));
                aspectLabel.setTextFill(Color.YELLOW);
                break;
                
            case DOUBLE_YELLOW: 
                aspectLabel.setText(String.format("%s%s", "\u25cf", "\u25cf"));
                aspectLabel.setTextFill(Color.YELLOW);
                break;
                
            case CLEAR:
            case GREEN:
                aspectLabel.setText(String.format("%s", "\u25cf"));
                aspectLabel.setTextFill(Color.GREEN);
                break;
 
            case TOP_YELLOW:
                aspectLabel.setText(String.format("%s%s", "\u25cb", "\u25cf"));
                break;
            case BLACK:
                aspectLabel.setText(String.format("%s", "\u25cb"));
                aspectLabel.setTextFill(Color.BLACK);
                break;
                
            case FLASHING_DOUBLE_YELLOW: 
                aspectLabel.setText(String.format("F %s%s", "\u25cf", "\u25cf"));
                aspectLabel.setTextFill(Color.YELLOW);
                break;
            case FLASHING_YELLOW:
                aspectLabel.setText(String.format("F %s", "\u25cf"));
                aspectLabel.setTextFill(Color.YELLOW);
                break;
            case FLASHING_WHITE:
                aspectLabel.setText(String.format("F %s", "\u2681"));
                break;
            case SPAD_INDICATOR_ILLUMINATED:
                aspectLabel.setText(String.format("%s%s%s", "\u25cf", "\u25cf", "\u25cf"));
                aspectLabel.setTextFill(Color.RED);
                break;
                
        }
        }
    }

    @Override
    public void run() {
        
    }

}

package org.jevis.jeconfig.tool;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ScreenSizeManager {

    private static ScreenSizeManager screenSizeManager=null;
    private Timer timer;
    private List<Stage> stageList;

    public ScreenSizeManager() {
        this.timer = new Timer();

        stageList= new ArrayList<>();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try{
                    System.out.println("run timerTask");
                    Screen screen = Screen.getPrimary();//TODO: what if the window is on a other Screen
                    for(Stage stage:stageList){
                        System.out.println("Stage.size: "+stage.getWidth()+"/"+stage.getHeight());
                        System.out.println("Screen.size: "+screen.getVisualBounds().getWidth()+"/"+screen.getBounds().getHeight());
                        System.out.println("SWT.size   :"+Toolkit.getDefaultToolkit().getScreenSize().width);

//                        Platform.runLater(() -> {
//                            stage.setWidth(screen.getBounds().getWidth());
//                            stage.setHeight(screen.getBounds().getHeight());
//                        });

                        if(stage.getWidth()!=screen.getBounds().getWidth()){
                            System.out.println("Max width");
                            Platform.runLater(() -> {
                                stage.setWidth(screen.getBounds().getWidth());
                            });

                        }

                        if(stage.getHeight()!=screen.getBounds().getHeight()){
                            System.out.println("Max Height");
                            Platform.runLater(() -> {
                                stage.setHeight(screen.getBounds().getHeight());
                            });

                        }
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        };

        timer.scheduleAtFixedRate(timerTask, 0, 5000);
    }

    public static ScreenSizeManager getInstance(){
        if(screenSizeManager==null){
            screenSizeManager = new ScreenSizeManager();
        }

        return screenSizeManager;
    }

    public void bindToScreenSize(Stage stage){
        if(!stageList.contains(stage)){
            stageList.add(stage);
        }
    }


}

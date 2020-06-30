package util;

import javafx.application.Platform;
import javafx.scene.control.Label;

import java.util.concurrent.atomic.AtomicBoolean;

public class LoadingAnimation {

    private Label loadingLabel;
    private volatile AtomicBoolean aFlag = new AtomicBoolean(false);;
    private String message;
    private volatile AtomicBoolean isLastThreadRunning = new AtomicBoolean(false);
    private volatile AtomicBoolean isQueueEmpty= new AtomicBoolean(true);

    public LoadingAnimation(Label loadingLabel , String message){

        this.loadingLabel = loadingLabel;
        this.message = message;
        loadingLabel.setText("");
    }

    public void startLoadingTask(){
        System.out.println("Start animation");
        if(isLastThreadRunning.get()){
            System.out.println(isQueueEmpty.get());
            if(isQueueEmpty.get()){
                isQueueEmpty.set(false);
                Thread waiting = new Thread(()->{
                    while(isLastThreadRunning.get()){
                    }
                    isQueueEmpty.set(true);
                    startAnimationThread();
                });
                waiting.setDaemon(true);
                waiting.start();
            }else{
            }
        }else{
            if(isQueueEmpty.get()){
                startAnimationThread();
            }
        }



    }
    private void startAnimationThread(){
        aFlag.set(true);
        isLastThreadRunning.set(true);
        Thread loadingTask = new Thread(new Runnable() {
            volatile int i = 0;

            @Override
            public void run() {
                Runnable updater = () -> {
                    if (aFlag.get()) {

                        if (i == 0) {
                            loadingLabel.setText(message + " .");
                        }
                        if (i == 1) {
                            loadingLabel.setText(message + " . .");
                        }
                        if (i == 2) {
                            loadingLabel.setText(message + " . . .");
                        }
                        if (i == 3) {
                            loadingLabel.setText(message + " . . . .");
                        }
                    }

                };


                while (aFlag.get()) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (i == 4) {
                        i = 0;
                    } else {
                        i++;
                    }

                    // UI update is run on the Application thread
                    Platform.runLater(updater);
                }
                isLastThreadRunning.set(false);
                i = 0;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        loadingLabel.setText("");
                    }
                });

                System.out.println("Thread Destroyed");
            }

        });

        loadingTask.setDaemon(true);
        loadingTask.start();
    }

    public void stopAnimation(){
        System.out.println("Stop animation");
        aFlag.set(false);
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

package Main;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import music.Controller.MongoUtils;

import java.io.File;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../View/LogIn.fxml"));
        System.out.println(getClass().getResource("../View/LogIn.fxml").toString());
        primaryStage.setTitle("L Music Player");
//        primaryStage.getIcons().add(new Image("logo_window.png"));
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.setResizable(false);
        primaryStage.show();

    }
    

    public static void main(String[] args) {


        launch(args);
    }
}

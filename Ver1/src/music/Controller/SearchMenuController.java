package music.Controller;


import Model.Singer;
import Model.User;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SearchMenuController implements Initializable {

    public Label currentMusicLabel;
    public Button playbutton;
    private MongoUtils mongoUtils;
    private User user;
    private PlayerUtils playerUtils;
    private ChangeListener<String> currentMusicTitleListener;
    private StringProperty titleProperty;
    private ChangeListener<Boolean> isReadyListener;
    private ReadOnlyBooleanProperty isPlayerReady;

    @FXML
    void back(ActionEvent event) {
        titleProperty.removeListener(currentMusicTitleListener);
        FXMLLoader loader = new FXMLLoader();
        try{
            loader.setLocation(getClass().getResource("/View/MainMenu.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            MainMenuController mainMenuController = loader.getController();
            mainMenuController.initData(user,mongoUtils,playerUtils);


            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    void searchByAlbum(ActionEvent event) {
        titleProperty.removeListener(currentMusicTitleListener);
        FXMLLoader loader = new FXMLLoader();
        try{
            loader.setLocation(getClass().getResource("/View/SearcAlbum.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            SearchAlbumController searchAlbumController = loader.getController();
            searchAlbumController.initData(mongoUtils,user,playerUtils);


            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    void searchByArtist(ActionEvent event) {
        titleProperty.removeListener(currentMusicTitleListener);
        FXMLLoader loader = new FXMLLoader();
        try{
            loader.setLocation(getClass().getResource("/View/SearchSinger.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            SearchSingerController searchSingerController = loader.getController();
            searchSingerController.initData(mongoUtils,user,playerUtils);


            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @FXML
    void searchByMusic(ActionEvent event) {
        titleProperty.removeListener(currentMusicTitleListener);
        FXMLLoader loader = new FXMLLoader();
        try{
            loader.setLocation(getClass().getResource("/View/SearchMusic.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            SearchMusicController searchMusicController = loader.getController();
            searchMusicController.initData(mongoUtils,user,playerUtils);


            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void play(ActionEvent actionEvent) throws IOException {
        if(playerUtils.status=="PLAYING"){
            playerUtils.pause();
            playbutton.setText(">");
        }else{
            playerUtils.play();
            playbutton.setText("| |");
        }
    }

    public void nextSong(ActionEvent actionEvent) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        playerUtils.nextPlay();
    }

    public void prevSong(ActionEvent actionEvent) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        playerUtils.playPrev();
    }

    public void initData(MongoUtils mongoUtils , User user , PlayerUtils playerUtils){
        this.mongoUtils = mongoUtils;
        this.user = user;
        this.playerUtils = playerUtils;

        currentMusicLabel.setText(playerUtils.getCurrentTitlePlaying().get());
        
        titleProperty = playerUtils.getCurrentTitlePlaying();
        
        currentMusicTitleListener =new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                currentMusicLabel.setText(newValue);
            }
        };

        isPlayerReady = playerUtils.isReady();

        if(playerUtils.status.equals("STOPPED")){
            isReadyListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if(newValue){
                        isPlayerReady.removeListener(isReadyListener);
                        playbutton.setText("| |");
                    }
                }
            };
            isPlayerReady.addListener(isReadyListener);
        }
        titleProperty.addListener(currentMusicTitleListener);

        if(playerUtils.status.equals("STOPPED") || playerUtils.status.equals("PAUSED")){
            playbutton.setText(">");
        }else{
            playbutton.setText("| |");
        }
    }

    public void goToQueue(ActionEvent actionEvent) {
    }
}

package music.Controller;

import Model.User;
import com.mongodb.Mongo;
import com.mongodb.client.MongoCollection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;

import java.net.URL;
import java.util.ResourceBundle;

public class PlayerController implements Initializable {

    @FXML
    private Slider musicSlider;

    @FXML
    private Label timeLabel;

    @FXML
    private Label timerLabel;

    @FXML
    private Button playButton;

    @FXML
    private Slider volumeSlider;

    @FXML
    private Label VolumeLabel;



    private MongoUtils mongoUtils;
    private User user;
    private PlayerUtils playerUtils;

    @FXML
    void back(ActionEvent event) {

    }

    @FXML
    void play(ActionEvent event) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void initData(MongoUtils mongoUtils , User user , PlayerUtils playerUtils){
        this.mongoUtils = mongoUtils;
        this.user = user;
        this.playerUtils = playerUtils;

        playerUtils.getCurrentTitlePlaying().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                
            }
        });

        playButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                Status status = playerUtils.mediaPlayer.getStatus();

                if (status == Status.UNKNOWN  || status == Status.HALTED)
                {
                    // don't do anything in these states
                    return;
                }

                if ( status == Status.PAUSED
                        || status == Status.READY
                        || status == Status.STOPPED)
                {
                    if (playerUtils.atEndOfMedia) {
                        playerUtils.mediaPlayer.seek(playerUtils.mediaPlayer.getStartTime());
                        playerUtils.atEndOfMedia = false;
                    }
                    playerUtils.mediaPlayer.play();
                } else {
                    playerUtils.mediaPlayer.pause();
                }
            }
        });
    }
}

package music.Controller;

import Model.Music;
import Model.User;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

public class MainMenuController implements Initializable {

    public Label labelWelcome;
    public Label currentMusicLabel;
    public Button playbutton;
    public Slider timeSlider;
    public Label cuurentTimeLabel;
    public Label maxTimeLabel;
    private MongoUtils mongoUtils;
    private User user;
    private PlayerUtils playerUtils;
    private ReadOnlyStringProperty musicLabelProperty;
    private boolean exit=false;
    private ReadOnlyObjectProperty<Duration> currentTimeProperty;
    private ReadOnlyBooleanProperty isPlayerReady;
    private boolean firstTime;


    private ChangeListener currentTitleListener;;
    private ChangeListener<? super Duration> currentTimeListener;
    private ChangeListener<? super Number> changeTimeListener;
    private ReadOnlyBooleanProperty isMusicReady;
    private ChangeListener<? super Boolean> isReadyListener;

    public void initData(User user , MongoUtils mongoUtils, PlayerUtils playerUtils){
        this.user = user;
        this.mongoUtils = mongoUtils;
        this.playerUtils = playerUtils;

        System.out.println("name");




        firstTime = true;
        currentTitleListener = (ChangeListener<String>) (observable, oldValue, newValue) -> currentMusicLabel.setText(newValue);


        currentTimeListener = (observable, oldValue, newValue) -> {
            timeSlider.setValue(newValue.toSeconds());

        };

        isPlayerReady = playerUtils.isReady();
        if(isPlayerReady !=null){
            System.out.println("IS PLAYER TIDAK NULL");
        }



        changeTimeListener = (ChangeListener<Number>) (observable, oldValue, newValue) -> {
            boolean validChange = newValue.doubleValue() - oldValue.doubleValue() > 1.0
                    ||newValue.doubleValue() - oldValue.doubleValue() < -1.0;
            if(validChange && !firstTime && !playerUtils.getStatus().equals("STOPPED"))
                playerUtils.mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
            if(firstTime) firstTime=false;
        };

        musicLabelProperty = playerUtils.getCurrentTitlePlaying();

        currentMusicLabel.setText(musicLabelProperty.get());

        musicLabelProperty.addListener(currentTitleListener);

        timeSlider.valueProperty().addListener(changeTimeListener);

        isReadyListener = (ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
            System.out.println("Diterima melakukan rebind");
            reBind();
                isPlayerReady.removeListener(isReadyListener);
                playbutton.setText("| |");

        };

        if(!playerUtils.status.equals("STOPPED")){
            reBind();
        }else{
            isPlayerReady.addListener(isReadyListener);
        }

        if(playerUtils.status.equals("STOPPED") || playerUtils.status.equals("PAUSED")){
            playbutton.setText(">");
        }else{
            playbutton.setText("| |");
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void back(ActionEvent actionEvent) {
        unBind();
        gotoLogInScene(actionEvent);
    }

    public void gotoLogInScene(ActionEvent actionEvent){
    	if(playerUtils!=null) {
    		if(playerUtils.mediaPlayer != null) {
                if(!playerUtils.status.equals("STOPPED"))playerUtils.mediaPlayer.stop();
                playerUtils.mediaPlayer.dispose();
    		}
    	}
        FXMLLoader loader = new FXMLLoader();
        try{
            loader.setLocation(getClass().getResource("/View/LogIn.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            LoginController loginController = loader.getController();
            loginController.initDataFromSignUp(mongoUtils);


            Stage window = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void searchMusic(ActionEvent actionEvent) {
        unBind();
        FXMLLoader loader = new FXMLLoader();
        try{
            loader.setLocation(getClass().getResource("/View/SearchMenu.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            SearchMenuController searchMenuController = loader.getController();
            searchMenuController.initData(mongoUtils,user,playerUtils);


            Stage window = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void EditProfile(ActionEvent actionEvent) {

        unBind();
        FXMLLoader loader = new FXMLLoader();
        try{
            loader.setLocation(getClass().getResource("/View/EditUser.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            EditUserController editUserController = loader.getController();
            editUserController.initData(mongoUtils,user,playerUtils);


            Stage window = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void play(ActionEvent actionEvent) throws IOException {
//        if(!playerUtils.getStatus(){
            if(playerUtils.status=="PLAYING"){
                playerUtils.pause();
                playbutton.setText(">");
            }else{
                playerUtils.play();
                playbutton.setText("| |");
            }
        //}
    }

    public void nextSong(ActionEvent actionEvent) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        playerUtils.nextPlay();
        isPlayerReady.addListener(isReadyListener);


    }

    public void prevSong(ActionEvent actionEvent) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        playerUtils.playPrev();
        isPlayerReady.addListener(isReadyListener);

    }

    public void goToQueue(ActionEvent actionEvent) {
        unBind();
        FXMLLoader loader = new FXMLLoader();
        try{
            loader.setLocation(getClass().getResource("/View/UserQueue.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            UserQueueController userQueueController = loader.getController();
            userQueueController.initData(user,mongoUtils,playerUtils);


            Stage window = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void unBind(){
        if(currentTimeProperty!=null){
            currentTimeProperty.removeListener(currentTimeListener);
        }
        musicLabelProperty.removeListener(currentTitleListener);
        timeSlider.maxProperty().unbind();
        maxTimeLabel.textProperty().unbind();
        cuurentTimeLabel.textProperty().unbind();
    }

    private void reBind(){
        if(currentTimeProperty!=null){
            currentTimeProperty.removeListener(currentTimeListener);
        }
        timeSlider.maxProperty().unbind();
        maxTimeLabel.textProperty().unbind();
        cuurentTimeLabel.textProperty().unbind();
        if(playerUtils.mediaPlayer!=null){
            if(playerUtils.media!=null){
                currentTimeProperty = playerUtils.mediaPlayer.currentTimeProperty();
                currentTimeProperty.addListener(currentTimeListener);

                timeSlider.maxProperty().bind(
                        Bindings.createDoubleBinding(
                                () -> {
                                        if(playerUtils.mediaPlayer.getTotalDuration()!=null){
                                            return playerUtils.mediaPlayer.getTotalDuration().toSeconds();
                                        }
                                        return 0.0;
                                    },
                                playerUtils.mediaPlayer.totalDurationProperty()));

                cuurentTimeLabel.textProperty().bind(Bindings.createStringBinding(() -> {
                    if(playerUtils.mediaPlayer.getCurrentTime()!=null){
                        Duration time = playerUtils.mediaPlayer.getCurrentTime();
                        return String.format("%02d:%.02f",
                                (int) time.toMinutes() % 60,
                                time.toSeconds() % 3600);
                    }else{
                        return "00:00";
                    }
                },playerUtils.mediaPlayer.currentTimeProperty()));

                maxTimeLabel.textProperty().bind(Bindings.createStringBinding(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        if(playerUtils.mediaPlayer.getTotalDuration()!=null){
                            Duration time = playerUtils.mediaPlayer.getTotalDuration();
                            return String.format("%02d:%.02f",
                                    (int) time.toMinutes() % 60,
                                    time.toSeconds() % 3600);
                        }else{
                            return "00:00";
                        }
                    }
                },playerUtils.mediaPlayer.totalDurationProperty()));
            }

            }


    }

    public void GoToPlaylistMenu(ActionEvent actionEvent) {
        unBind();
        FXMLLoader loader = new FXMLLoader();
        try{
            loader.setLocation(getClass().getResource("/View/PlaylistMenu.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            PlaylistMenuController playlistMenuController = loader.getController();
            playlistMenuController.initData(mongoUtils,playerUtils,user);


            Stage window = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

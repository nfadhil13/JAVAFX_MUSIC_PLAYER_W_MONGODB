package music.Controller;

import Model.Album;
import Model.Music;
import Model.Singer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import util.LoadingAnimation;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditMusicController implements Initializable {
    public TextField enterPasswordShow;
    public PasswordField passwordTextField;
    public PasswordField rePasswordTextField;
    public Button clear;
    public Label warnLabel;
    public Button showPasswordButton;
    public TextField titleTextField;
    public TextField genreTextField;
    public Label progressLabel;
    public Slider musicSlider;
    public Button playBtn;

    private MongoUtils mongoUtils;
    private Singer singer;
    private Album album;
    private Music music;
    private boolean isPasswordShowed;

    private LoadingAnimation mLoadingAnimation;
    private Task<Boolean> inputTask;
    private PlayerUtils playerUtils;
    private ReadOnlyObjectProperty<Duration> currentTimeProperty;
    private ChangeListener<? super Duration> currentTimeListener;
    private ChangeListener<? super Boolean> isReadyListener;
    private BooleanProperty isReady;
    private ChangeListener<Number> changeTimeListener;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        isPasswordShowed = false;
    }

    public void initData(MongoUtils mongoUtils , Singer singer , Album album, Music music){
        this.mongoUtils = mongoUtils;
        this.singer = singer;
        this.album = album;
        this.music = music;

        titleTextField.setText(music.getTitle());
        genreTextField.setText(music.getGenre());

        playerUtils = new PlayerUtils(mongoUtils);

        mLoadingAnimation = new LoadingAnimation(progressLabel,"UPDATING MUSIC");

        isReady = playerUtils.isReady();

        isReadyListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue) {
                    bind();
                    isReady.removeListener(isReadyListener);
                }
            }
        };

        isReady.addListener(isReadyListener);

        playerUtils.playOnce(music);
    }


    public void back(ActionEvent actionEvent) {
        backToAlbumPage(actionEvent);
    }

    private void backToAlbumPage(ActionEvent actionEvent){
        cancelAsyncTask();
        disposePlayer();
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(getClass().getResource("/View/EditorAlbumPage.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            EditorAlbumPageController editorAlbumPageController = loader.getController();
            editorAlbumPageController.initData(mongoUtils,singer,album);

            Stage window = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void clear(ActionEvent actionEvent) {
        clearActivity();
    }

    private void clearActivity(){
        genreTextField.setText("");
        passwordTextField.setText("");
        rePasswordTextField.setText("");
        titleTextField.setText("");
    }

    public void showPassword(ActionEvent actionEvent) {
        if(isPasswordShowed){
        enterPasswordShow.setVisible(false);
        passwordTextField.setText(enterPasswordShow.getText());
        passwordTextField.setVisible(true);
        isPasswordShowed=false;
    }else{
        passwordTextField.setVisible(false);
        enterPasswordShow.setText(passwordTextField.getText());
        enterPasswordShow.setVisible(true);
        isPasswordShowed=true;
    }

    }

    public void play(ActionEvent actionEvent) {
        if(!playerUtils.status.equals("STOPPED")){
            if(playerUtils.status.equals("PLAYING")){
                playBtn.setText("PLAY");
                try {
                    playerUtils.pause();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                playBtn.setText("PAUSE");
                playerUtils.play();
            }
        }
    }


    private void bind(){
        currentTimeListener = (observable, oldValue, newValue) -> {
            musicSlider.setValue(newValue.toSeconds());

        };


        changeTimeListener = (ChangeListener<Number>) (observable, oldValue, newValue) -> {
            boolean validChange = newValue.doubleValue() - oldValue.doubleValue() > 1.0
                    ||newValue.doubleValue() - oldValue.doubleValue() < -1.0;
            if(validChange && !playerUtils.getStatus().equals("STOPPED"))
                playerUtils.mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
        };

        musicSlider.valueProperty().addListener(changeTimeListener);
        if(playerUtils.mediaPlayer!=null) {
            if (playerUtils.media != null) {
                currentTimeProperty = playerUtils.mediaPlayer.currentTimeProperty();
                currentTimeProperty.addListener(currentTimeListener);


                musicSlider.maxProperty().bind(
                        Bindings.createDoubleBinding(
                                () -> {
                                    if (playerUtils.mediaPlayer.getTotalDuration() != null) {
                                        return playerUtils.mediaPlayer.getTotalDuration().toSeconds();
                                    }
                                    return 0.0;
                                },
                                playerUtils.mediaPlayer.totalDurationProperty()));
            }
        }
    }

    public void editMusic(ActionEvent actionEvent) {
        if(!titleTextField.getText().isEmpty()){
            if(!genreTextField.getText().isEmpty()){
                if(!passwordTextField.getText().isEmpty()){
                    if(isPasswordShowed)passwordTextField.setText(enterPasswordShow.getText());
                    if(passwordTextField.getText().equals(rePasswordTextField.getText())){
                        if(singer.getPassword().equals(passwordTextField.getText())){
                            warnLabel.setText("");
                            String title = titleTextField.getText();
                            String genre = genreTextField.getText();
                            inputTask = new Task<Boolean>() {
                                @Override
                                protected Boolean call() throws Exception {
                                    return mongoUtils.updateMusic(music.getCode(),title,genre);
                                }
                            };

                            inputTask.setOnSucceeded(event1 -> {
                                mLoadingAnimation.stopAnimation();
                                boolean isSuccess = inputTask.getValue();
                                if(isSuccess){
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("ADD Music");
                                    alert.setHeaderText("SUCCESS UPDATE Music" + title);
                                    alert.showAndWait();
                                }else{
                                    warnLabel.setText("Failed to Input Album");
                                }

                            });


                            ExecutorService executorService
                                    = Executors.newFixedThreadPool(1);
                            executorService.execute(inputTask);
                            executorService.shutdown();
                            mLoadingAnimation.startLoadingTask();
                        }
                    }else{
                        warnLabel.setText("Re-Enter Same Password");
                    }
                }else{
                    warnLabel.setText("Input password");
                }
            }else{
                warnLabel.setText("Input genre");
            }
        }else{
            warnLabel.setText("Input Track Title ");
        }
    }

    private void cancelAsyncTask() {
        if (isUploading()) {
            inputTask.cancel();
        }
        mLoadingAnimation.stopAnimation();

    }

    private boolean isUploading(){
        return inputTask!=null && inputTask.isRunning();
    }

    private void disposePlayer(){
        if(playerUtils.mediaPlayer!=null){
            if(!playerUtils.getStatus().equals("STOPPED"))playerUtils.mediaPlayer.stop();
            playerUtils.mediaPlayer.dispose();
        }
    }

    private void unBind(){
        musicSlider.maxProperty().unbind();
        musicSlider.valueProperty().removeListener(changeTimeListener);
        currentTimeProperty.removeListener(currentTimeListener);
    }
}

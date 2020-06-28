package music.Controller;

import Model.Album;
import Model.Singer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import jdk.jfr.Event;
import util.LoadingAnimation;

import java.beans.BeanProperty;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddMusicController implements Initializable {

    public Label fileNameLabel;

    public Label progressLabel;
    public Slider musicSlider;
    public Button playBtn;
    @FXML
    private TextField enterPasswordShow;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private PasswordField rePasswordTextField;

    @FXML
    private Button clear;

    @FXML
    private Label warnLabel;

    @FXML
    private Button showPasswordButton;

    @FXML
    private TextField titleTextField;

    @FXML
    private TextField genreTextField;




    private boolean isPasswordShowed;
    private String filePath;
    private MongoUtils mongoUtils;
    private Singer singer;
    private Album album;
    private LoadingAnimation mLoadingAnimation;
    private Task<Boolean> inputTask;

    ButtonType buttonTypeBackgroundUpload = new ButtonType("Exit And Upload In Background");
    ButtonType buttonTypeBack = new ButtonType("Cancel Uploading");
    ButtonType buttonTypeKeepUpload = new ButtonType("Keep Uploading", ButtonBar.ButtonData.CANCEL_CLOSE);
    private PlayerUtils playerUtils;
    private ReadOnlyObjectProperty<Duration> currentTimeProperty;
    private ChangeListener<? super Duration> currentTimeListener;
    private ChangeListener<? super Boolean> isReadyListener;
    private BooleanProperty isReady;
    private ChangeListener<Number> changeTimeListener;

    @FXML
    void addMusic(ActionEvent actionEvent) {
        if(!isUploading()){
            addMusicEvent(actionEvent);
        }else{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Look, At Dialog");
            alert.setContentText("You are still Uploading. Do you want to stop last upload?");
            alert.getButtonTypes().setAll(buttonTypeBack,buttonTypeKeepUpload);
            Optional<ButtonType> result = alert.showAndWait();
            if(result.isPresent()){
                if(result.get()==buttonTypeBack){
                    cancelAsyncTask();
                    addMusicEvent(actionEvent);
                }
            }
        }
    }

    private void addMusicEvent(ActionEvent event){
        if(!titleTextField.getText().isEmpty()){
            if(!genreTextField.getText().isEmpty()){
                if(isPasswordShowed)passwordTextField.setText(enterPasswordShow.getText());
                if(passwordTextField.getText().equals(rePasswordTextField.getText())){
                    if(singer.getPassword().equals(passwordTextField.getText())){
                        if(!filePath.isEmpty()){
                            warnLabel.setText("");
                            String title = titleTextField.getText();
                            String genre = genreTextField.getText();
                            String singerID = singer.getUsername();
                            String albumId = album.getCode();
                            inputTask = new Task<Boolean>() {
                                @Override
                                protected Boolean call() throws Exception {
                                    return mongoUtils.createDataMusic(title,genre,filePath,singerID,albumId);
                                }
                            };

                            inputTask.setOnSucceeded(event1 -> {
                                mLoadingAnimation.stopAnimation();
                                boolean isSuccess = inputTask.getValue();
                                if(isSuccess){
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("ADD Music");
                                    alert.setHeaderText("SUCCESS ADD Music" + title);
                                    clearActivity();
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

                        }else{
                            warnLabel.setText("Input Music File");
                        }
                    }
                }else{
                    warnLabel.setText("Re-Enter Same Password");
                }
            }else{
                warnLabel.setText("Input genre");
            }
        }else{
            warnLabel.setText("Input Track Title ");
        }
    }

    @FXML
    void back(ActionEvent actionEvent) {
        if(isUploading()){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Look, At Dialog");
            alert.setContentText("You are still uploading new Album.\n");
            alert.getButtonTypes().setAll(buttonTypeBack,buttonTypeKeepUpload,buttonTypeBackgroundUpload);
            Optional<ButtonType> result = alert.showAndWait();
            if(result.isPresent()){
                if(result.get()==buttonTypeBack){
                    cancelAsyncTask();
                    disposePlayer();
                    unBind();
                    backEvent(actionEvent);
                }else if(result.get()==buttonTypeBackgroundUpload){
                    disposePlayer();
                    unBind();
                    backEvent(actionEvent);
                }
            }
        }else{
            disposePlayer();
            unBind();

            backEvent(actionEvent);
        }

    }

    private void backEvent(ActionEvent event){
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(getClass().getResource("/View/EditorAlbumPage.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            EditorAlbumPageController editorAlbumPageController = loader.getController();
            editorAlbumPageController.initData(mongoUtils,singer,album);

            Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void disposePlayer(){
        if(playerUtils.mediaPlayer!=null){
            if(!playerUtils.getStatus().equals("STOPPED"))playerUtils.mediaPlayer.stop();
            playerUtils.mediaPlayer.dispose();
        }
    }
    @FXML
    void clear(ActionEvent event) {
        clearActivity();
    }

    void clearActivity(){
        genreTextField.setText("");
        passwordTextField.setText("");
        rePasswordTextField.setText("");
        titleTextField.setText("");
        filePath="";
        fileNameLabel.setText("");
    }

    @FXML
    void importMusic(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("mp3", "*.mp3")
        );
        try {
            File file = fc.showOpenDialog(null);
            if(file != null){
                filePath = file.getAbsolutePath();
                fileNameLabel.setText(file.getName());
                if(!playerUtils.getStatus().equals("STOPPED")){
                    playerUtils.stop();
                    playerUtils.mediaPlayer.dispose();
                    unBind();
                }
                playerUtils.playOnce(filePath);
                playBtn.setText("PAUSE");
                bind();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void showPassword(ActionEvent event) {
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filePath="";
        isPasswordShowed=false;
        progressLabel.setText("");
        mLoadingAnimation = new LoadingAnimation(progressLabel,"Adding Music");


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

    public void initData(MongoUtils mongoUtils , Singer singer , Album album){
        this.mongoUtils = mongoUtils;
        this.singer = singer;
        this.album = album;
        playerUtils = new PlayerUtils();

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
    }

    private void cancelAsyncTask(){
        if(isUploading()){
            inputTask.cancel();
        }
        mLoadingAnimation.stopAnimation();
    }

    private boolean isUploading(){
      return inputTask != null && inputTask.isRunning();
    }

    private void unBind(){
        musicSlider.maxProperty().unbind();
        musicSlider.valueProperty().removeListener(changeTimeListener);
        currentTimeProperty.removeListener(currentTimeListener);
    }


    public void play(ActionEvent actionEvent) throws IOException {
        if(!playerUtils.status.equals("STOPPED")){
            if(playerUtils.status.equals("PLAYING")){
                playBtn.setText("PLAY");
                playerUtils.pause();
            }else{
                playBtn.setText("PAUSE");
                playerUtils.play();
            }
        }
    }
}

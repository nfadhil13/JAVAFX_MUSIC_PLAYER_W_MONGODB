package music.Controller;

import Model.Singer;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import util.LoadingAnimation;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddAlbumController implements Initializable {
    public Label warnLabel;
    public PasswordField passwordTextField;
    public PasswordField rePasswordTextField;
    public ImageView imageUser;
    public Button showPasswordButton;
    public TextField enterPasswordShow;
    public TextField titleTextField;
    public Label progressLabel;

    private String filePath;
    private MongoUtils mongoUtils;
    private Singer singer;
    private boolean isPasswordShowed;

    private Task<Boolean> mAddAlbumTask;
    private LoadingAnimation loadingAnimation;

    ButtonType buttonTypeBackgroundUpload = new ButtonType("Exit And Upload In Background");
    ButtonType buttonTypeBack = new ButtonType("Cancel Uploading");
    ButtonType buttonTypeKeepUpload = new ButtonType("Keep Uploading", ButtonBar.ButtonData.CANCEL_CLOSE);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filePath = "";
        isPasswordShowed = false;
        loadingAnimation = new LoadingAnimation(progressLabel, "Adding Album in Progress");

    }

    public void initData(MongoUtils mongoUtils, Singer singer) {
        this.mongoUtils = mongoUtils;
        this.singer = singer;

    }

    public void clear(ActionEvent actionEvent) {
        passwordTextField.clear();
        rePasswordTextField.clear();
        titleTextField.clear();
        enterPasswordShow.clear();
        filePath = "";
        warnLabel.setText("");
        imageUser.setImage(null);

    }


    public void back(ActionEvent actionEvent) {
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
                    backEvent(actionEvent);
                }else if(result.get()==buttonTypeBackgroundUpload){
                    backEvent(actionEvent);
                }
            }
        }else{
            backEvent(actionEvent);
        }

    }

    private void backEvent(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader();

        try {
            loader.setLocation(getClass().getResource("/View/EditorSingerPage.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            EditorSingerPageController editorSingerPageController = loader.getController();
            editorSingerPageController.initThis(singer, mongoUtils);

            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void importPhoto(ActionEvent actionEvent) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("jpg", "*.jpg")
                , new FileChooser.ExtensionFilter("png", "*.png")
                , new FileChooser.ExtensionFilter("jpeg", "*.jpeg")
        );
        try {
            File file = fc.showOpenDialog(null);
            if (file != null) {
                if (file.length() / 1048576 < 15) {
                    filePath = file.getAbsolutePath();
                    File newFile = new File(filePath);
                    System.out.println(file.getAbsolutePath());
                    Image img = new Image(file.toURI().toString());
                    imageUser.setImage(img);
                    imageUser.setFitWidth(100);
                    imageUser.setFitHeight(100);
                    imageUser.setVisible(true);
                } else {
                    warnLabel.setText("Pic Size Max size is 15 MB");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addAlbum(ActionEvent actionEvent) {
        if(!isUploading()){
            addAlbumEvent(actionEvent);
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
                    addAlbumEvent(actionEvent);
                }
            }
        }
    }
    private void addAlbumEvent(ActionEvent actionEvent){
        if (!titleTextField.getText().isEmpty()) {
            if (isPasswordShowed) passwordTextField.setText(enterPasswordShow.getText());
            if (passwordTextField.getText().equals(rePasswordTextField.getText())) {
                if (singer.getPassword().equals(passwordTextField.getText())) {
                    if (!filePath.isEmpty()) {
                        String title = titleTextField.getText();
                        String singerID = singer.getUsername();
                        mAddAlbumTask = new Task<Boolean>() {
                            @Override
                            protected Boolean call() throws Exception {
                                Thread.sleep(3000);
                                return mongoUtils.createAlbum(title, singerID, filePath);
                            }
                        };
                        mAddAlbumTask.setOnSucceeded(event -> {
                            boolean isSuccess = mAddAlbumTask.getValue();
                            if (isSuccess) {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("ADD ALBUM INFORMATION");
                                alert.setHeaderText("SUCCESS ADD ALBUM" + title);
                                alert.showAndWait();
                                back(actionEvent);
                            } else {
                                warnLabel.setText("Failed to Input Album");
                            }
                        });

                        ExecutorService executorService
                                = Executors.newFixedThreadPool(1);
                        executorService.execute(mAddAlbumTask);
                        executorService.shutdown();
                        warnLabel.setText("");
                        loadingAnimation.startLoadingTask();


                    } else {
                        warnLabel.setText("Input Photo");
                    }
                }
            } else {
                warnLabel.setText("Re-Enter Same Password");
            }
        } else {
            warnLabel.setText("INPUT ALBUM'S TITLE");
        }
    }
    public void showPassword(ActionEvent actionEvent) {
        if (isPasswordShowed) {
            enterPasswordShow.setVisible(false);
            if(!enterPasswordShow.getText().isEmpty())
                passwordTextField.setText(enterPasswordShow.getText());
            passwordTextField.setVisible(true);
            isPasswordShowed = false;
        } else {
            passwordTextField.setVisible(false);
            if(!passwordTextField.getText().isEmpty())
                enterPasswordShow.setText(passwordTextField.getText());
            enterPasswordShow.setVisible(true);
            isPasswordShowed = true;
        }
    }

    private boolean isUploading() {
        return mAddAlbumTask != null && mAddAlbumTask.isRunning();
    }

    private void cancelAsyncTask() {
        if (isUploading()) {
            mAddAlbumTask.cancel();
        }
        loadingAnimation.stopAnimation();


    }


}

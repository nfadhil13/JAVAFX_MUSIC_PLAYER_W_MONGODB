package music.Controller;

import Model.Singer;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import util.LoadingAnimation;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditSingerPhotoController implements Initializable {
    public TextField enterPasswordShow;
    public PasswordField passwordTextField;
    public PasswordField rePasswordTextField;
    public ImageView imageUser;
    public Label warnLabel;
    public Button showPasswordButton;
    public Label progressLabel;
    public Rectangle rectangleCover;
    public Button uploadPhotoBtn;
    public Button backBtn;
    public Button importPhotoBtn;
    private MongoUtils mongoUtils;
    private Singer singer;
    private LoadingAnimation loadingAnimation;
    private String filePath;
    private boolean isPasswordShowed=false;
    private Task<Boolean> uploadTastask;
    private boolean doinchange = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
    }

    public void initData(MongoUtils mongoUtils , Singer singer){
        this.mongoUtils = mongoUtils;
        this.singer = singer;

        loadingAnimation = new LoadingAnimation(progressLabel,"UPDATING YOUR ACCOUNT PASSWORD");
    }


    public void clear(ActionEvent actionEvent) {
        passwordTextField.setText("");
        enterPasswordShow.setText("");
        rePasswordTextField.setText("");
        imageUser.setImage(null);
        filePath = "";
    }

    public void back(ActionEvent actionEvent) {
        if(doinchange){
            Task<Singer> backTask = new Task<>() {
                @Override
                protected Singer call() throws Exception {
                    return mongoUtils.logInSinger(singer.getUsername());
                }
            };

            backTask.setOnSucceeded(event -> {
                stopAnimation();
                this.singer = backTask.getValue();
                backEvent(actionEvent);
            });

            ExecutorService executorService
                    = Executors.newFixedThreadPool(1);
            executorService.execute(backTask);
            executorService.shutdown();
            startAnimation();
            warnLabel.setText("");
        }else{
            backEvent(actionEvent);
        }
    }

    private void backEvent(ActionEvent actionEvent){
        FXMLLoader loader = new FXMLLoader();
        try{
            loader.setLocation(getClass().getResource("/View/EditSingerMenu.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            EditSingerMenuController editSingerMenuController = loader.getController();
            editSingerMenuController.initThis(singer,mongoUtils);


            Stage window = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void importPhoto(ActionEvent actionEvent) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("jpg", "*.jpg")
                ,new FileChooser.ExtensionFilter("png", "*.png")
                ,new FileChooser.ExtensionFilter("jpeg", "*.jpeg")
        );
        try{
            File file = fc.showOpenDialog(null);
            if(file!=null && file.exists()) {
                if (file.length() / 1048576 < 15) {
                    filePath = file.getAbsolutePath();
                    File newFile = new File(filePath);
                    System.out.println(file.getAbsolutePath());
                    Image img = new Image(file.toURI().toString());
                    imageUser.setImage(img);
                    imageUser.setFitWidth(150);
                    imageUser.setFitHeight(150);
                    imageUser.setVisible(true);
                } else {
                    warnLabel.setText("Pic Size Max size is 15 MB");
                }
            }else{
                warnLabel.setText("PHOTO NOT FOUND");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void showPassword(ActionEvent actionEvent) {
        if(isPasswordShowed){
            enterPasswordShow.setVisible(false);
            if(!enterPasswordShow.getText().isEmpty())
                passwordTextField.setText(enterPasswordShow.getText());
            passwordTextField.setVisible(true);
            isPasswordShowed=false;
        }else{
            passwordTextField.setVisible(false);
            if(!passwordTextField.getText().isEmpty())
                enterPasswordShow.setText(passwordTextField.getText());
            enterPasswordShow.setVisible(true);
            isPasswordShowed=true;
        }
    }

    public void UpdatePhoto(ActionEvent actionEvent) {
        if(isPasswordShowed) passwordTextField.setText(enterPasswordShow.getText());
        if(!passwordTextField.getText().isEmpty()){
            if(passwordTextField.getText().equals(rePasswordTextField.getText())){
                if(singer.getPassword().equals(passwordTextField.getText())){
                    if(filePath!=null && !filePath.equals("")){
                        uploadTastask = new Task<Boolean>() {
                            @Override
                            protected Boolean call() throws Exception {
                                return mongoUtils.updateSinger(singer.getUsername(),filePath,true);
                            }
                        };

                        uploadTastask.setOnSucceeded(event -> {
                            stopAnimation();
                            boolean isSuccess = uploadTastask.getValue();
                            if (isSuccess) {
                                clear(actionEvent);
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("UPDATE PHOTO INFORMATION");
                                alert.setHeaderText("SUCCESS CHANGE YOUR PROFILE PHOTO");
                                alert.showAndWait();
                                doinchange = true;
                                back(actionEvent);

                            } else {
                                warnLabel.setText("Fail to update PROFILE PHOTO");
                            }
                        });

                        ExecutorService executorService
                                = Executors.newFixedThreadPool(1);
                        executorService.execute(uploadTastask);
                        executorService.shutdown();
                        startAnimation();
                        warnLabel.setText("");
                    }else {
                        warnLabel.setText("YOU ARE NOT CHOOSE ANY PHOYO YET");
                    }
                }else{
                    warnLabel.setText("WRONG PASSWORD");
                }
            }else{
                warnLabel.setText("RE ENTER THE SAME PASSOWRD");
            }
        }else{
            warnLabel.setText("INPUT YOUR PASSWORD");
        }
    }

    private void startAnimation(){
        loadingAnimation.startLoadingTask();
        rectangleCover.setVisible(true);
        showPasswordButton.setDisable(true);
        backBtn.setDisable(true);
        uploadPhotoBtn.setDisable(true);
        importPhotoBtn.setDisable(true);

    }

    private void stopAnimation(){
        loadingAnimation.stopAnimation();
        rectangleCover.setVisible(false);
        showPasswordButton.setDisable(false);
        backBtn.setDisable(false);
        uploadPhotoBtn.setDisable(false);
        importPhotoBtn.setDisable(false);
    }
}

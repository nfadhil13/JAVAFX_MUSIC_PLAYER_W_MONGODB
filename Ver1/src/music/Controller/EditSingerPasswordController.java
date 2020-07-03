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
import javafx.stage.Stage;
import util.LoadingAnimation;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditSingerPasswordController implements Initializable {
    public TextField enterPasswordShow;
    public PasswordField passwordTextField;
    public PasswordField rePasswordTextField;
    public Button editButton;
    public Button clearButton;
    public Label warnLabel;
    public Label progressLabel;
    public PasswordField newpasswordTextField;
    public TextField enterNewPasswordShow;

    private MongoUtils mongoUtils;
    private boolean isPasswordShowed = false;
    private Task<Boolean> task;
    private LoadingAnimation loadingAnimation;
    private Singer singer;
    private boolean isNewPasswordShowed =false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }


    public void editPassword(ActionEvent actionEvent) {
        loadingAnimation.stopAnimation();
        if(isNewPasswordShowed) newpasswordTextField.setText(enterNewPasswordShow.getText());
        if(isPasswordShowed) passwordTextField.setText(enterPasswordShow.getText());

        if(singer.getPassword().equals(passwordTextField.getText())){
            if(!singer.getPassword().equals(newpasswordTextField.getText())){
                boolean validPassword = newpasswordTextField.getText().equals(rePasswordTextField.getText());
                if (!newpasswordTextField.getText().isEmpty()) {
                    if (validPassword) {
                        String password = newpasswordTextField.getText();
                        task = new Task<Boolean>() {
                            @Override
                            protected Boolean call() throws Exception {
                                return mongoUtils.updateSinger(singer.getUsername(),password);
                            }
                        };
                        task.setOnSucceeded(workerStateEvent -> {
                            editButton.setDisable(false);
                            loadingAnimation.stopAnimation();
                            boolean isSuccess = task.getValue();
                            if (isSuccess) {
                                singer.setPassword(password);
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("UPDATE PASSWORD INFORMATION");
                                alert.setHeaderText("SUCCESS CHANGE YOUR PASSWORD");
                                alert.showAndWait();
                                clear(actionEvent);
                            } else {
                                warnLabel.setText("Fail to update Password");
                            }
                        });
                        task.setOnFailed(workerStateEvent -> {
                            warnLabel.setText("ERROR COMEBACK LATER");
                        });
                        ExecutorService executorService
                                = Executors.newFixedThreadPool(1);
                        executorService.execute(task);
                        executorService.shutdown();
                        loadingAnimation.startLoadingTask();
                        editButton.setDisable(true);

                    } else {
                        warnLabel.setText("RE - ENTER THE SAME PASSWORD");
                    }
                } else {
                    warnLabel.setText("INPUT YOUR NEW PASSWORD");
                }
            }else{
                warnLabel.setText("NEW PASSWORD CANNOT BE SAME AS OLD PASSWORD");
            }
        }else{
            warnLabel.setText("OLD PASSWORD WRONG");
        }


    }

    public void clear(ActionEvent actionEvent) {
        passwordTextField.clear();
        rePasswordTextField.clear();
        warnLabel.setText("");
        newpasswordTextField.setText("");
        enterNewPasswordShow.setText("");
        enterPasswordShow.setText("");
    }






    public void back(ActionEvent actionEvent) {
        loadingAnimation.stopAnimation();
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

    public void initData(MongoUtils mongoUtils , Singer singer){
        this.mongoUtils = mongoUtils;
        this.singer = singer;

        loadingAnimation = new LoadingAnimation(progressLabel,"UPDATING YOUR ACCOUNT PASSWORD");
    }

    public void edit(ActionEvent actionEvent) {
        editPassword(actionEvent);
    }

    public void showNewPassword(ActionEvent actionEvent) {
        if(isNewPasswordShowed){
            enterNewPasswordShow.setVisible(false);
            if(!enterNewPasswordShow.getText().isEmpty())
                newpasswordTextField.setText(enterNewPasswordShow.getText());
            newpasswordTextField.setVisible(true);
            isNewPasswordShowed=false;
        }else{
            System.out.println("ini new password");
            newpasswordTextField.setVisible(false);
            if(!newpasswordTextField.getText().isEmpty())
                enterNewPasswordShow.setText(newpasswordTextField.getText());
            enterNewPasswordShow.setVisible(true);
            isNewPasswordShowed=true;
        }
    }
}

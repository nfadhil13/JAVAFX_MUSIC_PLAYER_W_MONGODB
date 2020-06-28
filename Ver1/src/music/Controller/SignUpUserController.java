package music.Controller;

import Model.User;
import com.mongodb.client.MongoCollection;
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
import javafx.stage.Stage;
import util.LoadingAnimation;

import javax.print.DocFlavor;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignUpUserController implements Initializable {
    public Button SignupButton;
    public Button clearButton;
    public Label warnLabel;
    public TextField userTextField;
    public TextField passwordTextField;
    public TextField emailTextField;
    public TextField rePasswordTextField;
    public TextField enterPasswordShow;
    public Label progressLabel;

    private MongoUtils mongoUtils;
    private boolean isPasswordShowed = false;
    private Task<String> task;
    private LoadingAnimation loadingAnimation;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void initData(MongoUtils mongoUtils){
       this.mongoUtils = mongoUtils;

       loadingAnimation = new LoadingAnimation(progressLabel,"SIGN IN IN PROGRESS");

    }

    public void signUp(ActionEvent actionEvent) {
        loadingAnimation.stopAnimation();
        boolean emailValidation = !emailTextField.getText().isEmpty() && emailTextField.getText().contains("@");
        boolean validPassword = passwordTextField.getText().equals(rePasswordTextField.getText());
        if(emailValidation){
            if(!userTextField.getText().isEmpty()){
                if(!passwordTextField.getText().isEmpty()){
                    if(validPassword){
                        String email = emailTextField.getText();
                        String username = userTextField.getText();
                        String password = passwordTextField.getText();
                        task = new Task<>() {
                            @Override
                            protected String call() throws Exception {
                                return mongoUtils.createDataUser(email, username, password);
                            }
                        };
                        task.setOnSucceeded(workerStateEvent -> {
                            SignupButton.setDisable(false);
                            loadingAnimation.stopAnimation();
                            String answer = task.getValue();
                            if(answer.equals("CONGRATZ YOUR SIGNED UP SUCCESS")){
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("SING UP INFORMATION");
                                alert.setHeaderText("SUCCESS TO SING UP");
                                alert.showAndWait();
                                gotoLogInScene(actionEvent);
                            }else{
                                warnLabel.setText(answer);
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
                        SignupButton.setDisable(true);
                    }else{
                        warnLabel.setText("RE - ENTER THE SAME PASSWORD");
                    }
                }else{
                    warnLabel.setText("INPUT YOUR PASSWORD");
                }
            }else{
                warnLabel.setText("INPUT VALID USERNAME");
            }
        }else{
            warnLabel.setText("INPUT YOUR VALID EMAIL ADRESS!!");
        }
    }

    public void clear(ActionEvent actionEvent) {
        userTextField.clear();
        passwordTextField.clear();
        emailTextField.clear();
        rePasswordTextField.clear();
        warnLabel.setText("");

    }

    public void gotoLogInScene(ActionEvent actionEvent){
        if(!task.isRunning()){
            loadingAnimation.stopAnimation();
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

        }else{
            warnLabel.setText("YOU ARE SIGNING IN , PLEASE WAIT");
        }
    }




    public void back(ActionEvent actionEvent) {
        loadingAnimation.stopAnimation();
        FXMLLoader loader = new FXMLLoader();
        try{
            loader.setLocation(getClass().getResource("/View/SignUpMenu.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            SignUpMenuController signUpMenuController = loader.getController();
            signUpMenuController.initData(mongoUtils);

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




}

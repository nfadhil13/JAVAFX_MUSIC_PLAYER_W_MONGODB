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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import util.LoadingAnimation;

import javax.print.DocFlavor;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignUpArtistController implements Initializable {
    public Label warnLabel;
    public TextField userTextField;
    public PasswordField passwordTextField;
    public TextField emailTextField;
    public PasswordField rePasswordTextField;
    public TextField nameTextField;
    public ImageView imageUser;
    public Button showPasswordButton;
    public TextField enterPasswordShow;
    public Label progressLabel;
    public Rectangle rectangleCover;

    private String filePath;
    private MongoUtils mongoUtils;
    private boolean isPasswordShowed;
    private Task<String> signUpTask;
    private LoadingAnimation loadingAnimation;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filePath="";
        isPasswordShowed=false;

    }

    public void initData(MongoUtils mongoUtils){
        this.mongoUtils = mongoUtils;

        loadingAnimation = new LoadingAnimation(progressLabel,"SIGNING UP YOUR ACCOUNT");
        rectangleCover.setVisible(false);
    }

    public void signUp(ActionEvent actionEvent) {
        if(signUpTask==null){
            signUpEvent(actionEvent);
        }else if(!signUpTask.isRunning()){
            signUpEvent(actionEvent);
        }else{
            warnLabel.setText("YOU ARE STILL SIGNING UP");
        }


    }

    private void signUpEvent(ActionEvent actionEvent){
        boolean emailValidation = !emailTextField.getText().isEmpty() && emailTextField.getText().contains("@");
        boolean validPassword = passwordTextField.getText().equals(rePasswordTextField.getText());
        if(emailValidation){
            if(!userTextField.getText().isEmpty()){
                if(!passwordTextField.getText().isEmpty()){
                    if(!nameTextField.getText().isEmpty()){
                        if(validPassword){
                            if(!filePath.isEmpty()){
                                String email = emailTextField.getText();
                                String username = userTextField.getText();
                                String password;
                                String name = nameTextField.getText();
                                if(!isPasswordShowed){
                                    password = passwordTextField.getText();
                                }else{
                                    password = enterPasswordShow.getText();
                                }
                                signUpTask = new Task<String>(){

                                    @Override
                                    protected String call() throws Exception {
                                        Thread.sleep(5000);
                                        return mongoUtils.createSinger(email,username,name, password,filePath);
                                    }
                                };

                                signUpTask.setOnSucceeded(event -> {
                                    loadingAnimation.stopAnimation();
                                    rectangleCover.setVisible(false);
                                    String answer =  signUpTask.getValue();
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

                                ExecutorService executorService
                                        = Executors.newFixedThreadPool(1);
                                executorService.execute(signUpTask);
                                executorService.shutdown();
                                loadingAnimation.startLoadingTask();
                                rectangleCover.setVisible(true);
                            }else{
                                warnLabel.setText("Profile pic can't be Empty");
                            }
                        }else{
                            warnLabel.setText("RE - ENTER THE SAME PASSWORD");
                        }
                    }else{
                        warnLabel.setText("Name Cannot Be Empty");
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
        nameTextField.clear();
        enterPasswordShow.clear();
        filePath="";
        warnLabel.setText("");
        imageUser.setImage(null);

    }

    public void gotoLogInScene(ActionEvent actionEvent){
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




    public void back(ActionEvent actionEvent) {
        boolean isRunning = signUpTask!=null && signUpTask.isRunning();
        if(!isRunning){
            backEvent(actionEvent);
        }
    }

    private void backEvent(ActionEvent actionEvent){
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


    public void importPhoto(ActionEvent actionEvent) {
        System.out.println("IMPORTING");
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("jpg", "*.jpg")
                ,new FileChooser.ExtensionFilter("png", "*.png")
                ,new FileChooser.ExtensionFilter("jpeg", "*.jpeg")
        );
            try{
                File file = fc.showOpenDialog(null);
                if(file.length()/1048576<15){
                    filePath = file.getAbsolutePath();
                    File newFile = new File(filePath);
                    System.out.println(file.getAbsolutePath());
                    Image img = new Image(file.toURI().toString());
                    imageUser.setImage(img);
                    imageUser.setFitWidth(100);
                    imageUser.setFitHeight(100);
                    imageUser.setVisible(true);
                }else{
                    warnLabel.setText("Pic Size Max size is 15 MB");
                }
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

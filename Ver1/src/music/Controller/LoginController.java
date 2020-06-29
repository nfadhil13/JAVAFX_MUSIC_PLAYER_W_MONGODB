package music.Controller;

import Model.Singer;
import Model.User;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import util.LoadingAnimation;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoginController implements Initializable {
    public TextField userTextField;
    public TextField passwordTextField;
    public Label warnLabel;
    public ProgressIndicator progressBar;
    public Label proggressText;

    private MongoUtils mongoUtils;
    private PlayerController playerController;
    private User user;
    private Singer singer;
    private PlayerUtils playerUtils;
    Task<Object> loginTask;
    private LoadingAnimation loadingAnimation;


    public LoginController() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
    }

    public void signup(ActionEvent actionEvent) {
        firstInit();
        stopAsync();
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(getClass().getResource("/View/SignUpMenu.fxml"));
            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            SignUpMenuController signUpMenuController = loader.getController();
            signUpMenuController.initData(mongoUtils);

            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to go sign Up Menu !");
            System.exit(0);
        }
    }

    public void loginUser(ActionEvent actionEvent) {
        stopAsync();
        firstInit();
        if (playerController == null) playerController = new PlayerController();
        if (!userTextField.getText().isEmpty()) {
            if (!passwordTextField.getText().isEmpty()) {
                try {
                    loginTask = new Task<Object>() {
                        @Override
                        protected Object call() throws Exception {
                            return mongoUtils.logInUser(userTextField.getText());
                        }
                    };

                    loginTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent event) {
                            user = (User) loginTask.getValue();
                            loadingAnimation.stopAnimation();
                            if (user != null) {
                                if (passwordTextField.getText().equals(user.getPassword())) {
                                    warnLabel.setText("logIn Successs");
                                    FXMLLoader loader = new FXMLLoader();
                                    try {
                                        loader.setLocation(getClass().getResource("/View/MainMenu.fxml"));
                                        Parent parent = loader.load();

                                        Scene scene = new Scene(parent);

                                        MainMenuController controller = loader.getController();
                                        controller.initData(user, mongoUtils, playerUtils);


                                        System.out.println(actionEvent.getSource());

                                        Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

                                        window.setScene(scene);
                                        window.show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        warnLabel.setText("Cant find Menu , exiting in less than a second");
                                        System.exit(0);
                                    }

                                } else {
                                    System.out.println("Password salah");
                                    warnLabel.setText("Password and Username are not match");
                                }
                            } else {
                                warnLabel.setText("No Such username. Please Sign Up first");
                            }
                        }
                    });

                    loginTask.setOnFailed(event -> warnLabel.setText("Some Error Happen ComebackLater"));


                    loginTask.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent event) {
                            System.out.println("Task Canceled");
                        }
                    });

                    ExecutorService executorService
                            = Executors.newFixedThreadPool(1);
                    executorService.execute(loginTask);
                    executorService.shutdown();
                    warnLabel.setText("");
                    loadingAnimation.startLoadingTask();


                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("NULL WEH TERUS");
                    System.exit(0);
                }
            } else {
                warnLabel.setText("INPUT PASSWORD!!!");
            }
        } else {
            System.out.println("Username kosong");
            warnLabel.setText("INPUT USERNAME !!");
        }
    }

    public void loginArtist(ActionEvent actionEvent) {
        firstInit();
        stopAsync();
        if (!userTextField.getText().isEmpty()) {
            if (!passwordTextField.getText().isEmpty()) {
                try {
                    loginTask = new Task<>() {
                        @Override
                        protected Object call() throws Exception {
                            return mongoUtils.logInSinger(userTextField.getText());
                        }
                    };
                    loginTask.setOnSucceeded(event -> {
                        loadingAnimation.stopAnimation();
                        singer = (Singer) loginTask.getValue();
                        if (singer != null) {
                            if (passwordTextField.getText().equals(singer.getPassword())) {
                                warnLabel.setText("logIn Successs");
                                FXMLLoader loader = new FXMLLoader();
                                try {
                                    loader.setLocation(getClass().getResource("/View/ArtistMainMenu.fxml"));
                                    Parent parent = loader.load();

                                    Scene scene = new Scene(parent);

                                    ArtistMainMenu artistMainMenu = loader.getController();
                                    artistMainMenu.initData(mongoUtils, singer);


                                    System.out.println(actionEvent.getSource());

                                    Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

                                    window.setScene(scene);
                                    window.show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    warnLabel.setText("Cant find Menu , exiting in less than a second");
                                    System.exit(0);
                                }

                            } else {
                                System.out.println("Password salah");
                                warnLabel.setText("Password and Username are not match");
                            }
                        } else {
                            warnLabel.setText("No Such username. Please Sign Up first");
                        }

                    });


                    loginTask.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent event) {
                            loadingAnimation.stopAnimation();
                        }
                    });


                    ExecutorService executorService
                            = Executors.newFixedThreadPool(1);
                    executorService.execute(loginTask);
                    executorService.shutdown();
                    warnLabel.setText("");
                    loadingAnimation.startLoadingTask();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("NULL WEH TERUS");
                    System.exit(0);
                }
            } else {
                warnLabel.setText("INPUT PASSWORD!!!");
            }
        } else {
            System.out.println("Username kosong");
            warnLabel.setText("INPUT USERNAME !!");
        }
    }



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadingAnimation = new LoadingAnimation(proggressText, "Searching for Account");
        proggressText.setText("");
    }

    public void initDataFromSignUp(MongoUtils mongoUtils) {
        System.out.println("disini lebih dulu");
        this.mongoUtils = mongoUtils;
    }

    public void firstInit() {
        if (mongoUtils == null) {
            mongoUtils = new MongoUtils();
            System.out.println("New Connection");
        }
        if (playerUtils == null) playerUtils = new PlayerUtils(mongoUtils);
    }

    private void stopAsync() {
        if (loginTask != null && loginTask.isRunning()) {
            loginTask.cancel();
        }

        loadingAnimation.stopAnimation();

    }
}

package music.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class SignUpMenuController implements Initializable {

    private MongoUtils mongoUtils;


    public void singUpUser(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader();
        try{
            loader.setLocation(getClass().getResource("/View/SignUpUser.fxml"));
            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            SignUpUserController signUpUserController = loader.getController();
            signUpUserController.initData(mongoUtils);

            Stage window = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Failed to go sign Up Menu !");
            System.exit(0);
        }
    }

    public void singUpArtist(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader();
        try{
            loader.setLocation(getClass().getResource("/View/SignUpArtist.fxml"));
            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            SignUpArtistController signUpArtistController = loader.getController();
            signUpArtistController.initData(mongoUtils);

            Stage window = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Failed to go sign Up Menu !");
            System.exit(0);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void initData(MongoUtils mongoUtils){
        this.mongoUtils = mongoUtils;
    }

    public void back(ActionEvent actionEvent) {
        gotoLogInScene(actionEvent);
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
}

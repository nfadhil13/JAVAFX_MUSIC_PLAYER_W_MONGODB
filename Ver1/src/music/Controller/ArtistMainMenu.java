package music.Controller;

import Model.Singer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;


public class ArtistMainMenu implements Initializable {

    private MongoUtils mongoUtils;
    private Singer singer;

    @FXML
    void EditProfile(ActionEvent event) {

    }

    @FXML
    void back(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader();
        try{
            loader.setLocation(getClass().getResource("/View/LogIn.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            LoginController loginController = loader.getController();
            loginController.initDataFromSignUp(mongoUtils);


            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    void insertMusic(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader();
        try{
            loader.setLocation(getClass().getResource("/View/EditorSingerPage.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            EditorSingerPageController editorSingerPageController = loader.getController();
            editorSingerPageController.initThis(singer,mongoUtils);


            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void initData(MongoUtils mongoUtils , Singer singer){
        this.mongoUtils = mongoUtils;
        this.singer = singer;
    }
}

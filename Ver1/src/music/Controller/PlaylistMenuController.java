package music.Controller;

import Model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class PlaylistMenuController implements Initializable {

    private MongoUtils mongoUtils;
    private PlayerUtils playerUtils;
    private User user;

    public void initData(MongoUtils mongoUtils , PlayerUtils playerUtils , User user){
        this.mongoUtils = mongoUtils;
        this.playerUtils = playerUtils;
        this.user = user;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void back(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader();
        try{
            loader.setLocation(getClass().getResource("/View/MainMenu.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            MainMenuController mainMenuController = loader.getController();
            mainMenuController.initData(user,mongoUtils,playerUtils);


            Stage window = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void searchPlaylist(ActionEvent actionEvent) {


    }

    public void myPlaylist(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader();
        try{
            loader.setLocation(getClass().getResource("/View/MyPlaylist.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            MyPlaylistController myPlaylistController = new MyPlaylistController();
            myPlaylistController.initData(mongoUtils,playerUtils,user);


            Stage window = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

package music.Controller;

import Model.Album;
import Model.Singer;
import Model.User;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class EditSingerMenuController implements Initializable {

    private MongoUtils mongoUtils;
    private Singer singer;

    public void initThis(Singer singer , MongoUtils mongoUtils) {
        this.mongoUtils = mongoUtils;
        this.singer = singer;

    }


        @Override
    public void initialize(URL location, ResourceBundle resources) {
        
    }


    public void changePhoto(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(getClass().getResource("/View/EditSingerPhoto.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            EditSingerPhotoController editSingerPhotoController = loader.getController();
            editSingerPhotoController.initData(mongoUtils,singer);

            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changePassword(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(getClass().getResource("/View/EditSingerPassword.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            EditSingerPasswordController editSingerPasswordController = loader.getController();
            editSingerPasswordController.initData(mongoUtils,singer);

            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void back(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(getClass().getResource("/View/ArtistMainMenu.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            ArtistMainMenu artistMainMenu = loader.getController();
            artistMainMenu.initData(mongoUtils, singer);

            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

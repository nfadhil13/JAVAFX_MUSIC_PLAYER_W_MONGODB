package music.Controller;

import Model.Album;
import Model.Music;
import Model.User;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class UserQueueController implements Initializable {


    @FXML
    private TableView<Music> tableView;


    @FXML
    private TableColumn<Music, String> nameColoumn;
    public Label singerNameLabel;
    private User user;
    private MongoUtils mongoUtils;
    private PlayerUtils playerUtils;

    public void initData(User user , MongoUtils mongoUtils, PlayerUtils playerUtils){
        this.user = user;
        this.mongoUtils = mongoUtils;
        this.playerUtils = playerUtils;

        List<Music> musicList = playerUtils.getMusicQueue();
        ObservableList<Music> musicObservableList = FXCollections.observableList(musicList);
        nameColoumn.setCellValueFactory(itemData->new ReadOnlyStringWrapper(itemData.getValue().getTitle()));
        tableView.setItems(musicObservableList);
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void back(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader();
        try{
            loader.setLocation(getClass().getResource("/View/MainMenu.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            MainMenuController mainMenuController = loader.getController();
            mainMenuController.initData(user,mongoUtils,playerUtils);


            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

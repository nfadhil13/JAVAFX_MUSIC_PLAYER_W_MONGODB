package music.Controller;

import Model.Playlist;
import Model.User;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MyPlaylistController implements Initializable {

    @FXML
    private TableView<Playlist> tableView;


    @FXML
    private TableColumn<Playlist, String> nameColoumn;
    public Label singerNameLabel;
    private User user;
    private MongoUtils mongoUtils;
    private PlayerUtils playerUtils;
    private ObservableList<Playlist> playlistObservableList;

    private ListChangeListener onQueueListChangeListener;


    ButtonType buttonTypePlayNow = new ButtonType("Play Playlist");
    ButtonType buttonDeleteFromQueue = new ButtonType("Delete Playlist");
    ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    private Alert alert;


    public void initData(User user , MongoUtils mongoUtils, PlayerUtils playerUtils){
        this.user = user;
        this.mongoUtils = mongoUtils;
        this.playerUtils = playerUtils;

        playlistObservableList = FXCollections.observableList(user.getPlaylistList());
        nameColoumn.setCellValueFactory(itemData->new ReadOnlyStringWrapper(itemData.getValue().getName()));
        tableView.setItems(playlistObservableList);


        alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Confirmation");
        alert.setHeaderText("Look, At Dialog");
        alert.setContentText("Choose your option.");
        alert.getButtonTypes().setAll(buttonTypePlayNow, buttonDeleteFromQueue,buttonTypeCancel);

        onQueueListChangeListener = c -> {
            tableView.refresh();
        };
        playlistObservableList.addListener(onQueueListChangeListener);
        tableView.setOnMousePressed(event -> {
            if(event.getButton().equals(MouseButton.PRIMARY)){
                if(event.getClickCount() == 2) {
                    Optional<ButtonType> result = alert.showAndWait();
                    if(result.get() == buttonTypePlayNow){
                        playerUtils.clearQueue();
                    }else if(result.get() == buttonDeleteFromQueue){
                        playerUtils.deleteFromQueue(tableView.getSelectionModel().getSelectedIndex());
                    }
                }
            }

        });
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void back(ActionEvent event) {
        playlistObservableList.removeListener(onQueueListChangeListener);
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



    public void addPlaylist(ActionEvent actionEvent) {
    }
}

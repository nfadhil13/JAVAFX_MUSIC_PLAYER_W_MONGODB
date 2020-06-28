package music.Controller;

import Model.Album;
import Model.Singer;
import Model.User;
import com.mongodb.Mongo;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class SingerPageController implements Initializable {

    @FXML
    private TableView<Album> tableView;
    @FXML
    private TableColumn<Album, ImageView> photoColoumn;

    @FXML
    private TableColumn<Album, String> nameColoumn;

    @FXML
    private TableColumn<Album, Date> releaseDate;

    @FXML
    private Label singerNameLabel;

    @FXML
    private ImageView singerImageView;


    private Singer singer;
    private MongoUtils mongoUtils;
    private User user;
    private PlayerUtils playerUtils;



    public void initThis(Singer singer , MongoUtils mongoUtils , User user , PlayerUtils playerUtils){
        this.mongoUtils = mongoUtils;
        this.singer = singer;
        this.user = user;
        this.playerUtils = playerUtils;

        singerImageView.setImage(singer.getTheImageView().getImage());
        singerNameLabel.setText(singer.getName());
        String singerUsername = singer.getUsername();
        List<Album> albumList = mongoUtils.getAlbumBySinger(singerUsername);
        for(int i=0;i<albumList.size();i++){
            albumList.get(i).setTheImageView();
        }
        ObservableList<Album> singerObservableList = FXCollections.observableList(albumList);
        nameColoumn.setCellValueFactory(itemData->new ReadOnlyStringWrapper(itemData.getValue().getTitle()));
        photoColoumn.setCellValueFactory(itemData->new ReadOnlyObjectWrapper(itemData.getValue().getTheImageView()));
        releaseDate.setCellValueFactory(itemData->new ReadOnlyObjectWrapper(itemData.getValue().getReleaseDate()));
        tableView.setItems(singerObservableList);

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getButton().equals(MouseButton.PRIMARY)) {
                    if (event.getClickCount() == 2) {
                        FXMLLoader loader = new FXMLLoader();
                        try{
                            loader.setLocation(getClass().getResource("/View/AlbumPage.fxml"));

                            Parent parent = loader.load();

                            Scene scene = new Scene(parent);

                            AlbumPageController albumPageController = loader.getController();
                            albumPageController.initThis(mongoUtils,tableView.getSelectionModel().getSelectedItem() , user , playerUtils);
                            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

                            window.setScene(scene);
                            window.show();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public void back(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader();
        try{
            loader.setLocation(getClass().getResource("/View/MainMenu.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            MainMenuController mainMenuController = loader.getController();
            mainMenuController.initData(user,mongoUtils , playerUtils);


            Stage window = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void initData(MongoUtils mongoUtils , User user){
        this.mongoUtils = mongoUtils;
        this.user = user;
    }
}

package music.Controller;

import Model.Album;
import Model.Singer;
import Model.User;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import util.LoadingAnimation;

import javax.swing.text.html.ImageView;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchAlbumController implements Initializable {

    public Label progressLabel;
    @FXML
    private TextField searchField;

    @FXML
    private TableView<Album> tableView;

    @FXML
    private TableColumn<Album, ImageView> photoColoumn;

    @FXML
    private TableColumn<Album, String> TitleColoumn;

    @FXML
    private TableColumn<Album, String> artistColoumn;
    private MongoUtils mongoUtils;
    private User user;
    private PlayerUtils playerUtils;
    private Task<List<Album>> searchTask;
    private LoadingAnimation loadingAnimation;

    @FXML
    void back(ActionEvent event) {
        cancelAsync();
        FXMLLoader loader = new FXMLLoader();
        try{
            loader.setLocation(getClass().getResource("/View/SearchMenu.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            SearchMenuController searchMenuController = loader.getController();
            searchMenuController.initData(mongoUtils,user,playerUtils);


            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    void search(ActionEvent event) {
        cancelAsync();
        tableView.setItems(null);
        searchTask = new Task<List<Album>>() {
            @Override
            protected List<Album> call() throws Exception {
                loadingAnimation.stopAnimation();
                List<Album> albumList = mongoUtils.searchByAlbum(searchField.getText());

                for(int i=0;i<albumList.size();i++){
                    albumList.get(i).setTheImageView();
                    String nameArtist = mongoUtils.getSingerName(albumList.get(i).getSingerID());
                    albumList.get(i).setTheSingerName(nameArtist);
                }
                return  albumList;
            }
        };

        searchTask.setOnSucceeded(event1 -> {
            ObservableList<Album> albumObservableList = FXCollections.observableList(searchTask.getValue());
            TitleColoumn.setCellValueFactory(itemData->new ReadOnlyStringWrapper(itemData.getValue().getTitle()));
            photoColoumn.setCellValueFactory(itemData->new ReadOnlyObjectWrapper(itemData.getValue().getTheImageView()));
            artistColoumn.setCellValueFactory(itemData->new ReadOnlyObjectWrapper(itemData.getValue().getTheSingerName()));
            tableView.setItems(albumObservableList);
        });

        ExecutorService executorService
                = Executors.newFixedThreadPool(1);
        executorService.execute(searchTask);
        executorService.shutdown();
        loadingAnimation.startLoadingTask();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getButton().equals(MouseButton.PRIMARY)) {
                    if (event.getClickCount() == 2) {
                        cancelAsync();
                        FXMLLoader loader = new FXMLLoader();
                        try{
                            loader.setLocation(getClass().getResource("/View/AlbumPage.fxml"));

                            Parent parent = loader.load();

                            Scene scene = new Scene(parent);

                            AlbumPageController albumPageController = loader.getController();
                            albumPageController.initThis(mongoUtils,
                                    tableView.getSelectionModel().getSelectedItem() , user , playerUtils);
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

    public void initData(MongoUtils mongoUtils , User user , PlayerUtils playerUtils){
        this.mongoUtils = mongoUtils;
        this.user = user;
        this.playerUtils = playerUtils;

        loadingAnimation = new LoadingAnimation(progressLabel,"Searching Album");

    }

    private void cancelAsync(){
        if(searchTask!=null && searchTask.isRunning()){
            searchTask.cancel();
        }
        loadingAnimation.stopAnimation();
    }
}

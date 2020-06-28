package music.Controller;

import Model.Album;
import Model.Singer;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import util.LoadingAnimation;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditorSingerPageController implements Initializable {
    public ComboBox modeDropDown;
    public Label warnLabel;
    public Label progressLabel;
    public Button mRefreshBtn;
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

    private MongoUtils mongoUtils;
    private Singer singer;

    ObservableList<String> options =
            FXCollections.observableArrayList(
                    "Show Album Music",
                    "Edit",
                    "Delete"

            );
    private Task<List<Album>> getAlbumListTask;
    private String singerUsername;
    private LoadingAnimation loadingAnimation;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        modeDropDown.setItems(options);
        modeDropDown.setValue(options.get(0));

    }


    private void getAlbumList(){
        getAlbumListTask = new Task<List<Album>>(){

            @Override
            protected List<Album> call() throws Exception {
                return mongoUtils.getAlbumBySinger(singerUsername);
            }
        };

        getAlbumListTask.setOnSucceeded(event -> {
            afterTask();
            List<Album> albumList = getAlbumListTask.getValue();
            for(int i=0;i<albumList.size();i++){
                albumList.get(i).setTheImageView();
            }
            ObservableList<Album> albumObservableList = FXCollections.observableList(albumList);
            nameColoumn.setCellValueFactory(itemData->new ReadOnlyStringWrapper(itemData.getValue().getTitle()));
            photoColoumn.setCellValueFactory(itemData->new ReadOnlyObjectWrapper(itemData.getValue().getTheImageView()));
            releaseDate.setCellValueFactory(itemData->new ReadOnlyObjectWrapper(itemData.getValue().getReleaseDate()));
            tableView.setItems(albumObservableList);
        });

        ExecutorService executorService
                = Executors.newFixedThreadPool(1);
        executorService.execute(getAlbumListTask);
        executorService.shutdown();
        tableView.setItems(null);
        beforeTask();
    }

    public void initThis(Singer singer , MongoUtils mongoUtils){
        this.mongoUtils = mongoUtils;
        this.singer = singer;


        loadingAnimation = new LoadingAnimation(progressLabel , "Loading Singer's Album");


        singer.setTheImageView();
        singerImageView.setImage(singer.getTheImageView().getImage());
        singerNameLabel.setText(singer.getName());
        singerUsername = singer.getUsername();
        getAlbumList();


        tableView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                warnLabel.setText("");
                if(event.getButton().equals(MouseButton.PRIMARY)){
                    if(event.getClickCount() == 2){
                        String mode = modeDropDown.getValue().toString();
                        switch (mode){
                            case "Show Album Music":
                                gotoMusicEditor(event);
                                break;
                            case "Edit":
                                gotoEditAlbum(event);
                                break;
                            case "Delete":
                                Alert alert = new Alert(AlertType.CONFIRMATION);
                                alert.setTitle("Confirmation Dialog");
                                alert.setHeaderText("Look, a Confirmation Dialog");
                                alert.setContentText("Are you sure to delete "
                                        + tableView.getSelectionModel().getSelectedItem().getTitle() + " ?");

                                Optional<ButtonType> result = alert.showAndWait();
                                if (result.get() == ButtonType.OK){
                                    deleteAlbum();
                                    initThis(singer,mongoUtils);
                                }
                                break;
                            default:
                                warnLabel.setText("PLEASE CHOOSE MODE");
                                break;
                        }
                    }
                }

            }
        });



    }

    public void back(ActionEvent actionEvent) {
        cancelAsyncTask();
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

    public void addAlbum(ActionEvent actionEvent) {
        cancelAsyncTask();
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(getClass().getResource("/View/AddAlbum.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            AddAlbumController addAlbumController = loader.getController();
            addAlbumController.initData(mongoUtils,singer);

            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void gotoMusicEditor(MouseEvent actionEvent){
        cancelAsyncTask();
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(getClass().getResource("/View/EditorAlbumPage.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            EditorAlbumPageController editorAlbumPageController = loader.getController();
            editorAlbumPageController.initData(mongoUtils,singer,tableView.getSelectionModel().getSelectedItem());

            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void gotoEditAlbum(MouseEvent actionEvent){
        cancelAsyncTask();
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(getClass().getResource("/View/EditAlbum.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            EditAlbumController editAlbumController = loader.getController();
            Album album = tableView.getSelectionModel().getSelectedItem();
            editAlbumController.initData(mongoUtils,singer,album);

            Stage window = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteAlbum(){
        System.out.println("deleteing album");
        String albumName = tableView.getSelectionModel().getSelectedItem().getTitle();
        Task<Boolean> deleteAlbumTask= new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return mongoUtils.deleteAlbum(tableView.getSelectionModel().getSelectedItem().getCode());
            }
        };
        deleteAlbumTask.setOnSucceeded(event -> {
            System.out.println("Delete Success");
            if(deleteAlbumTask.getValue()){
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Delete ALBUM INFORMATION");
                alert.setHeaderText("SUCCESS DELETE ALBUM \n Title :" + albumName);
                alert.showAndWait();
                getAlbumList();
            }else{
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Delete ALBUM INFORMATION");
                alert.setHeaderText("FAIL TO DELETE SELECTED ALBUM");
                alert.showAndWait();
            }
        });

        ExecutorService executorService
                = Executors.newFixedThreadPool(1);
        executorService.execute(deleteAlbumTask);
        executorService.shutdown();

    }

    public void refreshList(ActionEvent actionEvent) {
        getAlbumList();
    }

    private void cancelAsyncTask(){
        if(getAlbumListTask !=null && getAlbumListTask.isRunning()){
            getAlbumListTask.cancel();
        }
        loadingAnimation.stopAnimation();
    }

    private void beforeTask(){
        loadingAnimation.stopAnimation();
        mRefreshBtn.setVisible(true);
    }

    private void afterTask(){
        loadingAnimation.stopAnimation();
        mRefreshBtn.setVisible(false);
    }
}

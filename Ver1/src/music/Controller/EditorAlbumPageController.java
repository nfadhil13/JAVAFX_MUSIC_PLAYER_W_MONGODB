package music.Controller;

import Model.Album;
import Model.Music;
import Model.Singer;
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
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import util.LoadingAnimation;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditorAlbumPageController implements Initializable {
    public Label warnLabel;
    public Label progressLabel;
    public Button mRefreshBtn;
    private Singer singer;
    private Album album;
    private MongoUtils mongoUtils;
    @FXML
    private TableView<Music> tableView;

    @FXML
    private TableColumn<Music, String> trackTitleColoumn;

    @FXML
    private TableColumn<Music, String> genreColoumn;

    @FXML
    private ImageView albumImageView;

    @FXML
    private Label albumTitleLabel;

    @FXML
    private ComboBox<String> modeComboBox;

    ObservableList<String> options =
            FXCollections.observableArrayList(
                    "Edit",
                    "Delete"

            );
    private Task<List<Music>> loadingMusicListTask;
    private String thisPageAlbumId;
    private LoadingAnimation loadingAnimation;
    private ObservableList<Music> musicObservableList;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        modeComboBox.setItems(options);
        modeComboBox.setValue(options.get(0));

    }



    @FXML
    void addMusic(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(getClass().getResource("/View/AddMusic.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            AddMusicController addMusicController = loader.getController();
            addMusicController.initData(mongoUtils,singer,album);

            Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void back(ActionEvent event) {
        cancelAsyncTask();
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(getClass().getResource("/View/EditorSingerPage.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            EditorSingerPageController editorSingerPageController = loader.getController();
            editorSingerPageController.initThis(singer,mongoUtils);

            Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void getAlbumMusics(String albumId) throws IOException {

        loadingMusicListTask = new Task<List<Music>>(){

            @Override
            protected List<Music> call() throws Exception {
                return mongoUtils.getMusicByAlbum(albumId);
            }
        };

        loadingMusicListTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                loadingAnimation.stopAnimation();
                mRefreshBtn.setVisible(true);
                musicObservableList = FXCollections.observableList(loadingMusicListTask.getValue());
                trackTitleColoumn.setCellValueFactory(dataItem-> new ReadOnlyStringWrapper(dataItem.getValue().getTitle()));
                genreColoumn.setCellValueFactory(dataItem->new ReadOnlyStringWrapper(dataItem.getValue().getGenre()));
                tableView.setItems(musicObservableList);
            }
        });

        ExecutorService executorService
                = Executors.newFixedThreadPool(1);
        executorService.execute(loadingMusicListTask);
        executorService.shutdown();
        loadingAnimation.startLoadingTask();
        mRefreshBtn.setVisible(false);
        tableView.setItems(null);

    }
    public void initData(MongoUtils mongoUtils , Singer singer ,Album album) throws IOException {
        this.mongoUtils = mongoUtils;
        this.singer = singer;
        this.album= album;

        albumImageView.setImage(album.getTheImageView().getImage());
        albumTitleLabel.setText(album.getTitle());

        loadingAnimation = new LoadingAnimation(progressLabel,"Loading Album List");

        thisPageAlbumId = album.getCode();
        getAlbumMusics(thisPageAlbumId);


        tableView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                warnLabel.setText("");
                if(event.getButton().equals(MouseButton.PRIMARY)){
                    if(event.getClickCount() == 2){
                        String mode = modeComboBox.getValue().toString();
                        switch (mode){
                            case "Edit":
                                gotoEditSong(event);
                                break;
                            case "Delete":
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setTitle("Confirmation Dialog");
                                alert.setHeaderText("Look, a Confirmation Dialog");
                                alert.setContentText("Are you sure to delete "
                                        + tableView.getSelectionModel().getSelectedItem().getTitle() + " ?");

                                Optional<ButtonType> result = alert.showAndWait();
                                if (result.get() == ButtonType.OK){
                                    deleteSong();
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


    private void deleteSong(){
        String musicTitle = tableView.getSelectionModel().getSelectedItem().getTitle();
        Task<Boolean> deleteSongTask= new Task<>(){
                @Override
            protected Boolean call() throws Exception {
                return mongoUtils.deleteMusic(tableView.getSelectionModel().getSelectedItem().getCode(),
                        tableView.getSelectionModel().getSelectedItem().getMusicCode());
            }
        };
        deleteSongTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                if(deleteSongTask.getValue()){
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Delete Song INFORMATION");
                    alert.setHeaderText("SUCCESS DELETE Song with \n Title :" + musicTitle);
                    alert.showAndWait();
                }else{
                    Alert.AlertType alertAlertType;
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Delete Music INFORMATION");
                    alert.setHeaderText("FAIL TO DELETE SELECTED Song");
                    alert.showAndWait();
                }
            }
        });

        ExecutorService executorService
                = Executors.newFixedThreadPool(1);
        executorService.execute(deleteSongTask);
        executorService.shutdown();


    }
    private void cancelAsyncTask(){
        if(loadingMusicListTask!=null && loadingMusicListTask.isRunning()){
            loadingMusicListTask.cancel();
        }
        loadingAnimation.stopAnimation();
    }
    private void gotoEditSong(MouseEvent event){
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(getClass().getResource("/View/EditMusic.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            EditMusicController editMusicController = loader.getController();
            editMusicController.initData(mongoUtils,singer,album,tableView.getSelectionModel().getSelectedItem());

            Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshList(ActionEvent actionEvent) throws IOException {
        getAlbumMusics(thisPageAlbumId);
    }
}

package music.Controller;

import Model.Album;
import Model.Music;
import Model.Singer;
import Model.User;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import util.LoadingAnimation;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlbumPageController implements Initializable {

    @FXML
    public ImageView albumImageView;
    public Label albumTitleLabel;
    public Label mProgressText;
    @FXML
    private TableView<Music> tableView;

    @FXML
    private TableColumn<Music, String> trackTitleColoumn;


    @FXML
    private TableColumn<Music, String> genreColoumn;

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);


    ButtonType buttonTypePlayNow = new ButtonType("Play Now");
    ButtonType buttonTypeQueue = new ButtonType("Add To Play Queue");
    ButtonType buttonTypePlaylist = new ButtonType("Add To Playlist");
    ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);


    private MongoUtils mongoUtils;
    private Album album;
    private User user;
    private PlayerUtils playerUtils;
    private Task<List<Music>> gettingAlbumTask;
    private LoadingAnimation loadingAnimation;
    private Task<Music> gettingMusicNowTask;
    private boolean isAlreadyQueued=false;

    @FXML
    void back(ActionEvent event) {
        loadingAnimation.stopAnimation();
        if(gettingAlbumTask != null && gettingAlbumTask.isRunning()){
            gettingAlbumTask.cancel();
        }
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        alert.setTitle("Confirmation");
        alert.setHeaderText("Look, At Dialog");
        alert.setContentText("Choose your option.");
        alert.getButtonTypes().setAll(buttonTypePlaylist,buttonTypePlayNow,buttonTypeQueue,buttonTypeCancel);
        tableView.setOnMousePressed(event -> {
            if(event.getButton().equals(MouseButton.PRIMARY)){
                if(event.getClickCount() == 2) {
                    Optional<ButtonType> result = alert.showAndWait();
                    if(result.get() == buttonTypePlayNow){
                        try {
                            playMusicNow();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else if(result.get() == buttonTypeQueue){
                        try {
                            if(!isAlreadyQueued && !playerUtils.isSomethingPlaying()){
                                playMusicNow();
                                System.out.println("playing");
                            }else{
                                queuqeMusic();
                                System.out.println("queueing");
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else if(result.get() == buttonTypePlaylist){

                    }
                }
            }

        });
    }

    public void playMusicNow() throws IOException {
        isAlreadyQueued = true;
        Music music = tableView.getSelectionModel().getSelectedItem();
        try {
            playerUtils.playNow(music , true);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

    }

    public void addPlaylist(){
        System.out.println("Going to Playlist");
    }

    public void queuqeMusic() throws IOException {
        Music music = tableView.getSelectionModel().getSelectedItem();
        try {
            playerUtils.queueMusic(music);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void initThis(MongoUtils mongoUtils , Album album , User user , PlayerUtils playerUtils) throws IOException {
        this.mongoUtils = mongoUtils;
        this.album = album;
        this.user = user;
        this.playerUtils = playerUtils;

        loadingAnimation = new LoadingAnimation(mProgressText,"Loading Album Detail");

        albumImageView.setImage(album.getTheImageView().getImage());
        albumImageView.setFitWidth(100);
        albumImageView.setFitHeight(100);

        albumTitleLabel.setText(album.getTitle());


        gettingAlbumTask = new Task<>() {

            @Override
            protected List<Music> call() throws Exception {
                return mongoUtils.getMusicByAlbum(album.getCode());
            }
        };

        gettingAlbumTask.setOnSucceeded(event -> {
            loadingAnimation.stopAnimation();
            List<Music> musicList = gettingAlbumTask.getValue();
            ObservableList<Music> singerObservableList = FXCollections.observableList(musicList);
            trackTitleColoumn.setCellValueFactory(itemData->new ReadOnlyStringWrapper(itemData.getValue().getTitle()));
            genreColoumn.setCellValueFactory(itemData->new ReadOnlyStringWrapper(itemData.getValue().getGenre()));
            tableView.setItems(singerObservableList);
        });

        gettingAlbumTask.setOnFailed(event -> {

        });

        ExecutorService executorService
                = Executors.newFixedThreadPool(1);
        executorService.execute(gettingAlbumTask);
        executorService.shutdown();
        loadingAnimation.startLoadingTask();
    }

    private void cancelAsycnTask(){
        if(gettingMusicNowTask!=null && gettingMusicNowTask.isRunning()){
            gettingMusicNowTask.cancel();
        }
        loadingAnimation.stopAnimation();
    }
}

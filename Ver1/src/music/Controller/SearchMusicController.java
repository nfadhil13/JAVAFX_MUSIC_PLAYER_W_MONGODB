package music.Controller;

import Model.Album;
import Model.Music;
import Model.User;
import com.mongodb.Mongo;
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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import util.LoadingAnimation;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchMusicController implements Initializable {

    public ProgressIndicator progressBar;
    public Label progressLabel;
    @FXML
    private TextField searchField;

    @FXML
    private TableView<Music> tableView;

    @FXML
    private TableColumn<Music, String> trackTitleColoumn;


    @FXML
    private TableColumn<Music, String> artistNameColoumn;

    @FXML
    private TableColumn<Music, String> GenreColoumn;
    private MongoUtils mongoUtils;
    private User user;
    private PlayerUtils playerUtils;

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);


    ButtonType buttonTypePlayNow = new ButtonType("Play Now");
    ButtonType buttonTypeQueue = new ButtonType("Add To Play Queue");
    ButtonType buttonTypePlaylist = new ButtonType("Add To Playlist");
    ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    private LoadingAnimation loadingAnimation;
    private Task<Music> gettingMusicNowTask;
    private boolean isAlreadyQueued=false;

    @FXML
    void back(ActionEvent event) {
        loadingAnimation.stopAnimation();
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(getClass().getResource("/View/SearchMenu.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            SearchMenuController searchMenuController = loader.getController();
            searchMenuController.initData(mongoUtils, user,playerUtils);


            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void search(ActionEvent event) throws IOException {
        Task<List<Music>> searchTask = new Task<List<Music>>() {
            @Override
            protected List<Music> call() throws Exception {
                List<Music> musicList = mongoUtils.searchByMusic(searchField.getText());
                for (int i = 0; i < musicList.size(); i++) {
                    String nameArtist = mongoUtils.getSingerName(musicList.get(i).getSingerID());
                    musicList.get(i).setTheSingerName(nameArtist);
                }
                return musicList;
            }
        };

        searchTask.setOnSucceeded(event12 -> {
            progressBar.setVisible(false);
            List<Music> musicList = searchTask.getValue();
            ObservableList<Music> albumObservableList = FXCollections.observableList(musicList);
            trackTitleColoumn.setCellValueFactory(itemData -> new ReadOnlyStringWrapper(itemData.getValue().getTitle()));
            artistNameColoumn.setCellValueFactory(itemData -> new ReadOnlyObjectWrapper(itemData.getValue().getTheSingerName()));
            GenreColoumn.setCellValueFactory(itemData -> new ReadOnlyObjectWrapper(itemData.getValue().getGenre()));
            tableView.setItems(albumObservableList);
        });

        searchTask.setOnRunning(event1 -> {
            progressBar.setVisible(true);
        });

        ExecutorService executorService
                = Executors.newFixedThreadPool(1);
        executorService.execute(searchTask);
        executorService.shutdown();

    }




    @Override
    public void initialize(URL location, ResourceBundle resources) {
        alert.setTitle("Confirmation");
        alert.setHeaderText("Look, At Dialog");
        alert.setContentText("Choose your option.");
        progressBar.setVisible(false);
        alert.getButtonTypes().setAll(buttonTypePlaylist,buttonTypePlayNow,buttonTypeQueue,buttonTypeCancel);
        tableView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
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

            }
        });
    }
    public void playMusicNow() throws IOException {
        Music music = tableView.getSelectionModel().getSelectedItem();
        try {
            playerUtils.playNow(music,true);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
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
    public void addPlaylist(){
        System.out.println("Going to Playlist");
    }
    public void initData(MongoUtils mongoUtils,User user ,PlayerUtils playerUtils){
        this.mongoUtils = mongoUtils;
        this.user =user;
        this.playerUtils = playerUtils;
        loadingAnimation = new LoadingAnimation(progressLabel,"Loading Album Detail");
    }

    private void cancelAsycnTask(){
        if(gettingMusicNowTask!=null && gettingMusicNowTask.isRunning()){
            gettingMusicNowTask.cancel();
        }
        loadingAnimation.stopAnimation();
    }
}

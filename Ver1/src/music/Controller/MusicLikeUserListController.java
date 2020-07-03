package music.Controller;

import Model.Music;
import Model.User;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import util.LoadingAnimation;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicLikeUserListController implements Initializable {
    public TableView tableView;
    public TableColumn<String,String> usernameColoumn;
    public Label musicTitleLabel;
    public Label progressLabel;
    private User user;
    private MongoUtils mongoUtils;
    private Music music;
    private PlayerUtils playerUtils;
    private LoadingAnimation loadingAnimation;
    private Task<List<String>> gettingTask;


    public void initData(MongoUtils mongoUtils , User user , PlayerUtils playerUtils , Music music){
        this.mongoUtils = mongoUtils;
        this.user = user;
        this.playerUtils = playerUtils;
        this.music = music;

        loadingAnimation = new LoadingAnimation(progressLabel,"Getting User Who Like This music");

        gettingTask = new Task<List<String>>(){

            @Override
            protected List<String> call() throws Exception {
                return mongoUtils.getUserWhoseLikeMusic(music.getCode());
            }
        };

        musicTitleLabel.setText(music.getTitle() + " by " + music.getTheSingerName());

        gettingTask.setOnSucceeded(event -> {
            loadingAnimation.stopAnimation();
            List<String> userList = gettingTask.getValue();
            ObservableList<String> musicObservableList = FXCollections.observableList(userList);
            usernameColoumn.setCellValueFactory(itemData -> new ReadOnlyStringWrapper(itemData.getValue()));
            tableView.setItems(musicObservableList);
        });


        ExecutorService executorService
                = Executors.newFixedThreadPool(1);
        executorService.execute(gettingTask);
        executorService.shutdown();
        loadingAnimation.startLoadingTask();

    }

    public void back(ActionEvent actionEvent) {
        loadingAnimation.stopAnimation();
        if(gettingTask!=null && gettingTask.isRunning()){
            gettingTask.cancel();
        }
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(getClass().getResource("/View/SearchMenu.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            SearchMenuController searchMenuController = loader.getController();
            searchMenuController.initData(mongoUtils, user, playerUtils);


            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}

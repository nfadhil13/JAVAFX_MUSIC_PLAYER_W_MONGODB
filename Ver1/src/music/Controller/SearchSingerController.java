package music.Controller;


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
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import util.LoadingAnimation;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchSingerController implements Initializable {


    public Label progressLabel;
    @FXML
    private TextField searchField;

    @FXML
    private TableView<Singer> tableView;

    @FXML
    private TableColumn<Singer, ImageView> photoColoumn;

    @FXML
    private TableColumn<Singer,String> nameColoumn;

    private MongoUtils mongoUtils;
    private User user;


    ObservableList<String> options =
            FXCollections.observableArrayList(
                    "By Artist",
                    "By Album",
                    "By Song"
            );
    private PlayerUtils playerUtils;
    private Task<List<Singer>> searchTask;
    private LoadingAnimation loadingAnimation;

    @FXML
    @SuppressWarnings("unchecked")
    void search(ActionEvent event) {
        if(!searchField.getText().isEmpty()){
            cancleAsyncTask();
            tableView.setItems(null);
            searchTask = new Task<>() {

                @Override
                protected List<Singer> call() throws Exception {
                    Thread.sleep(5000);
                    String searchKey = searchField.getText();
                    return mongoUtils.searchBySinger(searchKey);
                }
            };

            searchTask.setOnSucceeded(event1 -> {
                loadingAnimation.stopAnimation();
                List<Singer> singerList = searchTask.getValue();
                for (int i = 0; i < singerList.size(); i++) {
                    singerList.get(i).setTheImageView();
                }
                ObservableList<Singer> singerObservableList = FXCollections.observableList(singerList);
                nameColoumn.setCellValueFactory(itemData -> new ReadOnlyStringWrapper(itemData.getValue().getName()));
                photoColoumn.setCellValueFactory(itemData -> new ReadOnlyObjectWrapper(itemData.getValue().getTheImageView()));
                tableView.setItems(singerObservableList);
            });
            ExecutorService executorService
                    = Executors.newFixedThreadPool(1);
            executorService.execute(searchTask);
            executorService.shutdown();
            loadingAnimation.startLoadingTask();
        }

    }

    private void cancleAsyncTask() {
        if(searchTask!=null && searchTask.isRunning()){
            searchTask.cancel();
        }
        loadingAnimation.stopAnimation();
    }

    public void searchBySinger(String searchKey){

    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getButton().equals(MouseButton.PRIMARY)) {
                    if (event.getClickCount() == 2) {
                        cancleAsyncTask();
                        FXMLLoader loader = new FXMLLoader();
                        try{
                            loader.setLocation(getClass().getResource("/View/SingerPage.fxml"));

                            Parent parent = loader.load();

                            Scene scene = new Scene(parent);

                            SingerPageController singerPageController = loader.getController();
                            singerPageController.initThis(tableView.getSelectionModel().getSelectedItem(),mongoUtils , user
                                    , playerUtils);

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

    public void initData(MongoUtils mongoUtils , User user, PlayerUtils playerUtils){
        this.mongoUtils = mongoUtils;
        this.user = user;
        this.playerUtils = playerUtils;

        loadingAnimation = new LoadingAnimation(progressLabel,"Searching singer");
    }

    public void back(ActionEvent actionEvent) {
        gotoLogInScene(actionEvent);
    }

    public void gotoLogInScene(ActionEvent actionEvent){
        cancleAsyncTask();
        FXMLLoader loader = new FXMLLoader();
        try{
            loader.setLocation(getClass().getResource("/View/MainMenu.fxml"));

            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            MainMenuController mainMenuController = loader.getController();
            mainMenuController.initData(user, mongoUtils,playerUtils);


            Stage window = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();

            window.setScene(scene);
            window.show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}

//package music.Controller;
//
//import Model.Music;
//import javafx.application.Platform;
//import javafx.beans.InvalidationListener;
//import javafx.beans.Observable;
//import javafx.fxml.Initializable;
//import javafx.scene.control.Button;
//import javafx.scene.control.Slider;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.util.Duration;
//
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.ResourceBundle;
//
//public class Controller implements Initializable {
//    public Slider slider;
//    public ImageView image;
//    public Button playButton;
//    PlayerController pl;
//    ArrayList<Music> music;
//    MongoUtils mg;
//    Duration duration;
//
//    public void initData(MongoUtils mongoUtils , PlayerController pl){
//        this.mg=mongoUtils;
//        this.pl = pl;
//        Image PlayButtonImage = new Image(getClass().getResourceAsStream("../../../res/play.png"));
//        Image PauseButtonImage = new Image(getClass().getResourceAsStream("../../../res/pause.png"));
//        ImageView imageViewPlay = new ImageView(PlayButtonImage);
//        ImageView imageViewPause = new ImageView(PauseButtonImage);
//        try {
//            List<Music> musicList =mg.searchByMusic("Mikroskos");
//            Music music = musicList.get(0);
//            music.setTheMusicByte(mg.getMusicFile(music.getMusicCode()));
//            pl.queueMusic(music.getTheMusicByte());
//            playButton.setGraphic(imageViewPlay);
//
//            pl.mediaPlayer.currentTimeProperty().addListener(new InvalidationListener()
//            {
//                public void invalidated(Observable ov) {
//                    updateValues();
//                }
//            });
//
//
//            slider.valueProperty().addListener(new InvalidationListener() {
//                public void invalidated(Observable ov) {
//                    if (slider.isValueChanging()) {
//                        // multiply duration by percentage calculated by slider position
//                        pl.mediaPlayer.seek(duration.multiply(slider.getValue() / 100.0));
//                    }
//                }
//            });
//
//
//        } catch (Exception e) {
//            System.out.println("Fail");
//            System.exit(0);
//        }
//    }
//
//    @Override
//    public void initialize(URL location, ResourceBundle resources) {
//
//    }
//
//    protected void updateValues() {
//        if ( slider != null) {
//            Platform.runLater(new Runnable() {
//                public void run() {
//                    Duration currentTime = pl.mediaPlayer.getCurrentTime();
//                    slider.setDisable(duration.isUnknown());
//                    if (slider.isDisabled()
//                            && duration.greaterThan(Duration.ZERO)
//                            && slider.isValueChanging()) {
//                        slider.setValue(currentTime.divide(duration).toMillis()
//                                * 100.0);
//                    }
//                }
//            });
//        }
//    }
//
//
//}
//
//
//

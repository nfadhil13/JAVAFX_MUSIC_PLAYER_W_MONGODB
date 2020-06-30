package music.Controller;

import Model.Music;
import com.mongodb.client.MongoCollection;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.fxml.Initializable;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import javax.sound.sampled.*;

public class PlayerUtils implements Initializable {



    final boolean repeat = false;
    boolean stopRequested = false;
    boolean atEndOfMedia = false;

    Media media;
    MediaPlayer mediaPlayer;

    // current status of clip
    String status = "STOPPED";
    private int current=-1;
    private StringProperty currentTitlePlaying = new SimpleStringProperty("");
    private BooleanProperty isReady = new SimpleBooleanProperty(false);
    private Task<Music> gettingMusicNowTask;
    private MongoUtils mongoUtils;
    private String currentFilePath;
    private List<Music> musicQueue;
    private boolean isFirstTime;

    public PlayerUtils(MongoUtils mongoUtils) {
        this.mongoUtils = mongoUtils;
        musicQueue = new ArrayList<>();
    }
    public PlayerUtils(){

    }

    public void PlayerController(){
    }

    private void playMusic(String filePath , boolean isPlayNow)
            throws UnsupportedAudioFileException,
            IOException, LineUnavailableException
    {

        try{
            System.out.println(filePath);
            File file = new File(filePath);
            media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
            status ="PLAYING";
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Failed to play Music");
            System.exit(0);
        }
        mediaPlayer.setOnEndOfMedia(()->{
            mediaPlayer.stop();
            mediaPlayer.dispose();
            status ="STOPPED";
            try {
                nextPlay();
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        });

        mediaPlayer.setOnReady(() -> {
            isReady.set(true);
        });

    }

    public void playOnce(String filePath)
            throws UnsupportedAudioFileException,
            IOException, LineUnavailableException
    {

        try{
            System.out.println(filePath);
            File file = new File(filePath);
            media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
            status ="PLAYING";
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Failed to play Music");
            System.exit(0);
        }
        mediaPlayer.setOnEndOfMedia(()->{
            mediaPlayer.stop();
            mediaPlayer.seek(Duration.seconds(0.0));
        });

        mediaPlayer.setOnReady(() -> {
            isReady.set(true);
        });

    }
    private void deleteFile(){
        File file = new File(currentFilePath);
        if(file.exists()){
            file.delete();
            System.out.println(file.getAbsolutePath() + " deleted");
        }
    }
    public void nextPlay() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if(status.equals("PLAYING")){
            mediaPlayer.stop();
            mediaPlayer.dispose();
            status = "STOPPED";
            deleteFile();
        }
        if(!musicQueue.isEmpty()){
         if(current==-1) {
             if(musicQueue.size()>1){
                 current=1;
                executeQueueMusic(current);
             }else{
                 current=0;
                 executeQueueMusic(current);
             }
         }else{
            if(current+1<musicQueue.size()){
                current++;
                executeQueueMusic(current);
            }else{
                current=0;
                executeQueueMusic(current);
            }
         }
        }
    }

    private void executeQueueMusic(int index) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        Music currentMusic = musicQueue.get(index);
        playNow(currentMusic,true);
    }

    public void playPrev() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if(!musicQueue.isEmpty()){
            if(status.equals("PLAYING")){
                mediaPlayer.stop();
                mediaPlayer.dispose();
                status = "STOPPED";
                deleteFile();
            }
            if(current==-1) {
                if(musicQueue.size()>1){
                    current=1;
                    executeQueueMusic(current);
                }else{
                    current=0;
                    executeQueueMusic(current);
                }
            }else{
                if(current-1>=0){
                    current--;
                    executeQueueMusic(current);
                }else{
                    current = musicQueue.size()-1;
                    executeQueueMusic(current);
                }

            }
        }else if(status.equals("PLAYING")){
            mediaPlayer.seek(Duration.seconds(0.0));
        }
    }

    public void playNow(Music music , boolean isPlayNow) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        canclePlayNowTask();
        if(!isFirstTime) isFirstTime = true;
        if (status.equals("PLAYING")) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            status = "STOPPED";
        }
        gettingMusicNowTask = new Task<Music>() {
            @Override
            protected Music call() throws Exception {
                String singerName = mongoUtils.getSingerName(music.getSingerID());
                System.out.println(music.getTitle() + "Ini di music playnow");
                music.setTheSingerName(singerName);
                music.setTheMusicByte(mongoUtils.getMusicFile(music.getMusicCode()));
                return music;
            }
        };

        gettingMusicNowTask.setOnSucceeded(event -> {
            Music updatedMusic = gettingMusicNowTask.getValue();
            String songNameAndSinger = updatedMusic.getTitle()
                    + " by "
                    + updatedMusic.getTheSingerName();
            if(currentFilePath!=null){
                deleteFile();
            }
            currentFilePath = generateTempFile(updatedMusic.getTheMusicByte());
            try {
                playMusic(currentFilePath,isPlayNow);
                currentTitlePlaying.set(songNameAndSinger);
                if(!isPlayNow)queueMusic(music);
                updatedMusic.setTheMusicByte(null);
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        });

        ExecutorService executorService
                = Executors.newFixedThreadPool(1);
        executorService.execute(gettingMusicNowTask);
        executorService.shutdown();

    }

    public void queueMusic(Music music) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        musicQueue.add(music);
    }







    public String generateTempFile(byte[] byteArray){
        try {
            String filePath="";
            File file = File.createTempFile("temp", ".mp3");
            file.deleteOnExit();
            System.out.println(file.getAbsolutePath());
            filePath = file.getAbsolutePath();

            FileOutputStream fileoutputstream = new FileOutputStream(file);
            fileoutputstream.write(byteArray);
            fileoutputstream.close();
            return filePath;
        }catch(Exception e){
            e.printStackTrace();
            return "";
        }
    }

    public boolean isSomethingPlaying(){
        return isFirstTime;
    }

    // Method to play the audio
    public void play()
    {
        mediaPlayer.play();
        status ="PLAYING";
    }

    // Method to pause the audio
    public void pause() throws IOException {

        mediaPlayer.pause();
        status="PAUSED";

    }

    public String getStatus() {
        return status;
    }

    // Method to restart the audio
    public void restart() throws IOException, LineUnavailableException,
            UnsupportedAudioFileException
    {
        mediaPlayer.stop();
        mediaPlayer.play();
        status = "PLAYING";
    }

    // Method to stop the audio
    public void stop() throws UnsupportedAudioFileException,
            IOException, LineUnavailableException
    {
        mediaPlayer.stop();
        status = "STOPPED";
    }



    public StringProperty getCurrentTitlePlaying() {
        return currentTitlePlaying;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }


    public BooleanProperty isReady() {
        return isReady;
    }

    private void canclePlayNowTask(){

    }

    public void playOnce(Music music) {

        gettingMusicNowTask = new Task<Music>() {
            @Override
            protected Music call() throws Exception {
                String singerName = mongoUtils.getSingerName(music.getSingerID());
                System.out.println(music.getTitle() + "Ini di music playnow");
                music.setTheSingerName(singerName);
                music.setTheMusicByte(mongoUtils.getMusicFile(music.getMusicCode()));
                return music;
            }
        };

        gettingMusicNowTask.setOnSucceeded(event -> {
            Music updatedMusic = gettingMusicNowTask.getValue();
            String songNameAndSinger = updatedMusic.getTitle()
                    + " by "
                    + updatedMusic.getTheSingerName();
            try {
                playOnceExecute(generateTempFile(updatedMusic.getTheMusicByte()));
                updatedMusic.setTheMusicByte(null);
            }catch(Exception e){

            }
        });

        ExecutorService executorService
                = Executors.newFixedThreadPool(1);
        executorService.execute(gettingMusicNowTask);
        executorService.shutdown();



    }

    private void playOnceExecute(String filePath){
        try{
            System.out.println(filePath);
            File file = new File(filePath);
            media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
            status ="PLAYING";
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Failed to play Music");
            System.exit(0);
        }
        mediaPlayer.setOnEndOfMedia(()->{
            mediaPlayer.seek(Duration.seconds(0.0));
        });

        mediaPlayer.setOnStopped(()->{
            File file = new File(filePath);
            file.delete();
        });

        mediaPlayer.setOnReady(() -> {
            isReady.set(true);
        });
    }

    public List<Music> getMusicQueue() {
        return musicQueue;
    }

    public void deleteFromQueue(int index){
        musicQueue.remove(index);
    }

    public void clearQueue(){
        musicQueue.clear();
    }

    public void playNowFromQueue(int index){
        try {
            current=index;
            executeQueueMusic(index);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}

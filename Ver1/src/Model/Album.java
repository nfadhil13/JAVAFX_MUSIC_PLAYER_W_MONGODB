package Model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Album {
    private String code;
    private String title;
    private Date releaseDate;
    private String singerID;
    private String singerName;
    private byte[] image;
    private ImageView imageView;


    public Album(){

    }


    // Constructor for inputing data to database
    public Album(String code ,String title , Date releaseDate , String singerID , byte[] image){
        this.code = code;
        this.title = title;
        this.releaseDate = releaseDate;
        this.singerID = singerID;
        this.image = image;
    }

    public ImageView getTheImageView() {
        return imageView;
    }

    public String getTheSingerName() {
        return singerName;
    }

    public void setTheSingerName(String singerName) {
        this.singerName = singerName;
    }

    public void setTheImageView() {
        Image currentImage = new Image(new ByteArrayInputStream(image));
        this.imageView = new ImageView(currentImage);
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
    }
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getSingerID() {
        return singerID;
    }

    public void setSingerID(String singerID) {
        this.singerID = singerID;
    }


    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}

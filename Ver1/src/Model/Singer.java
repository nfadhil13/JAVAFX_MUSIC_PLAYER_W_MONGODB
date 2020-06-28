package Model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;

public class Singer {

    private String email;
    private String username;
    private String password;
    private String name;
    private byte[] image;
    private String Description;
    private ImageView imageView;

    public Singer(){

    }

    public Singer(String email , String username , String password  ,String name , byte[] image){
        this.email = email;
        this.username = username;
        this.password = password;
        this.name = name;
        this.image = image;



    }

    public ImageView getTheImageView() {
        return imageView;
    }

    public void setTheImageView() {
        Image currentImage = new Image(new ByteArrayInputStream(image));
        this.imageView = new ImageView(currentImage);
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }


}

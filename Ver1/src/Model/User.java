package Model;

import java.util.List;

public class User {

    private String email;
    private String username;
    private String password;
    private List<Playlist> playlistList;

    public User(){

    }

    public User(String email , String username , String password){
        this.email=email;
        this.password = password;
        this.username = username;
        this.playlistList = null;
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


    public List<Playlist> getPlaylistList() {
        return playlistList;
    }

    public void setPlaylistList(List<Playlist> playlistList) {
        this.playlistList = playlistList;
    }
}

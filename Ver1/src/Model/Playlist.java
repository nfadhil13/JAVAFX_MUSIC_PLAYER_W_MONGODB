package Model;

import java.util.List;

public class Playlist {
    private String code;
    private String name;
    private List<String> songIds;

    public Playlist(){

    }

    public Playlist(String code , String userID , String name , List<String> songIds){
        this.code = code;
        this.name = name;
        this.songIds = songIds;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSongIds() {
        return songIds;
    }

    public void setSongIds(List<String> songIds) {
        this.songIds = songIds;
    }
}

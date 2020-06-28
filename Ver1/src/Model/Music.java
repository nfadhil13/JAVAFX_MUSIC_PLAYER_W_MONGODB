package Model;

import org.bson.Document;

public class Music {

    private String code;
    private String title;
    private byte[] musicByte;
    private String genre;
    private String musicCode;
    private String albumID;
    private String singerID;
    private String singerName;
    public Music(){

    }

    public Music(String id , String title , String Genre , String musicCode , String singerID , String albumID){
        this.code = id;
        this.title = title;
        this.musicCode= musicCode;
        this.genre = Genre;
        this.albumID = albumID;
        this.singerID = singerID;

    }

    public String getTheSingerName() {
        return singerName;
    }

    public void setTheSingerName(String singerName) {
        this.singerName = singerName;
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

    public void setTitle(String title) {
        this.title = title;
    }

    public byte[] getTheMusicByte() {
        return musicByte;
    }

    public void setTheMusicByte(byte[] musicByte) {
        this.musicByte = musicByte;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getMusicCode() {
        return musicCode;
    }

    public void setMusicCode(String musicCode) {
        this.musicCode = musicCode;
    }

    public String getAlbumID() {
        return albumID;
    }

    public void setAlbumID(String albumID) {
        this.albumID = albumID;
    }

    public String getSingerID() {
        return singerID;
    }

    public void setSingerID(String singerID) {
        this.singerID = singerID;
    }
}


package music.Controller;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import Model.*;
import com.mongodb.*;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.gridfs.GridFSDBFile;
import javafx.scene.image.Image;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.*;

import javax.imageio.ImageIO;

public class MongoUtils {
    MongoDatabase database;
    MongoCollection<Music> collection;
    MongoCollection<User> userCollection;
    MongoCollection<Singer> singerCollection;
    MongoCollection<Album> albumCollection;
    GridFSBucket musicBucket;
    public MongoUtils() {
        // Creating Credentials
        MongoCredential credential;
        credential = MongoCredential.createCredential("sampleUser", "myDb",
                "password".toCharArray());
        System.out.println("Connected to the database successfully");
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClient mongo = new MongoClient("localhost", MongoClientOptions.builder().codecRegistry(pojoCodecRegistry).build());
        // Accessing the database
        database = mongo.getDatabase("myDb");
        database = database.withCodecRegistry(pojoCodecRegistry);
        System.out.println("Credentials ::"+ credential);
        collection = database.getCollection("Music", Music.class);
        userCollection = database.getCollection("User" , User.class);
        singerCollection = database.getCollection("Singer" , Singer.class);
        albumCollection = database.getCollection("Album" , Album.class);
        musicBucket = GridFSBuckets.create(database, "musicFiles");;
    }


    public boolean createDataMusic(String title , String Genre , String filepath ,
                                   String singerId , String albumId) {
        String id = new ObjectId().toString();
        try {
            File file = new File(filepath);
            InputStream streamToUploadFrom = new FileInputStream(file);
            // Create some custom options

            GridFSUploadOptions options = new GridFSUploadOptions()
                    .chunkSizeBytes(358400);

            ObjectId fieldId = musicBucket.uploadFromStream(file.getName(), streamToUploadFrom,options);
            Music music = new Music(id , title , Genre , fieldId.toString() , singerId , albumId);
            collection.insertOne(music);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
    public ArrayList<Music> getMusic() throws IOException {
        ArrayList<Music> resultList = new ArrayList<>();
        FindIterable<Music>  musicIterable = collection.find();
        for (Music music : musicIterable) {
            resultList.add(music);
        }
        return resultList;
    }

    public List<Music> searchByMusic(String searchKey) throws IOException {
        try{
            List<Music> resultList;
            Pattern pattern = Pattern.compile(".*"+Pattern.quote(searchKey)+".*", Pattern.CASE_INSENSITIVE);
            resultList = collection.find(
                    regex("title",pattern)).into(new ArrayList<>());
            return resultList;
        }catch(Exception e){
            return null;
        }
    }

    public List<Music> searchByMusicId(String musicCode) throws IOException {
        try{
            List<Music> musicList = new ArrayList<>();
            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.put("musicCode", musicCode);
            FindIterable<Music> musicFindIterable =     collection.find(searchQuery);
            for(Music music : musicFindIterable){
                musicList.add(music);
            }
            return musicList;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public List<Music> getMusicByAlbum(String albumId) throws IOException {
        try{
            List<Music> musicList = new ArrayList<>();
            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.put("albumID", albumId);
            FindIterable<Music> musicFindIterable = collection.find(searchQuery);
            for(Music music : musicFindIterable){
                musicList.add(music);
            }
            return musicList;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public byte[] getMusicFile(String musicCode) throws IOException{

        try {
            ObjectId id = new ObjectId(musicCode);
            System.out.println(id.toString());
            GridFSDownloadStream downloadStream = musicBucket.openDownloadStream(id);
            int fileLength = (int) downloadStream.getGridFSFile().getLength();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.out.println(fileLength);
            byte[] bytesToWriteTo = new byte[fileLength];
            int data = downloadStream.read();
            while(data >= 0){
                outputStream.write((char) data);
                data = downloadStream.read();
            }
            bytesToWriteTo = outputStream.toByteArray();
            downloadStream.close();
            return bytesToWriteTo;
        }catch(Exception e){
            System.out.println("Not Found");
            return new byte[0];
        }


    }

    public boolean updateMusic(String code , String title , String genre){
        try{
            List<Bson> bsonList = new ArrayList<>();
            bsonList.add(set("title",title));
            bsonList.add(set("genre",genre));
            UpdateResult updateResult = collection
                    .updateMany(Filters.eq("code",code),bsonList);
            if(updateResult.getModifiedCount()>0){
                return true;
            }
            return false;
        }catch (Exception e){
            return false;
        }
    };



    public boolean deleteMusic(String code , String musicId){
        try{
            DeleteResult del = collection.deleteOne(eq("code", code));
            if(del.getDeletedCount()==1){
                ObjectId musicObjectID = new ObjectId(musicId);
                musicBucket.delete(musicObjectID);
                return true;
            }
            return false;
        }catch (Exception e){
            return false;
        }

    }


    public String createDataUser(String email,String username , String password){
        try{
            System.out.println("lOOKING FOR EMAIL :" + email + " &"  + " username :" + username);
            // Query or Looking for identic email and username
            FindIterable<User> findEmail = userCollection.find(eq("email",email));

            MongoCursor<User> cursorEmail = findEmail.iterator();

            if(cursorEmail.hasNext()){
                return "EMAIL IS EXIST";
            }else{
                FindIterable<User> findUsername =  userCollection.find(eq("username", username));
                MongoCursor<User> cursorUsername = findUsername.iterator();
                if(cursorUsername.hasNext()){
                    return "USERNAME IS EXIST";
                }else{
                    User user = new User(email,username,password);
                    userCollection.insertOne(user);
                    return "CONGRATZ YOUR SIGNED UP SUCCESS";
                }

            }
        }catch(Exception e){
            e.printStackTrace();
            return "FAILED TO READ DATABASE";
        }
    }

    public boolean updateUserPassword(String username , String password){
        try{
            userCollection.updateOne(Filters.eq("username",username), set("password",password));
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public User logInUser(String username){
        try{
            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.put("username", username);
            FindIterable<User> userFindIterable = userCollection.find(searchQuery);
            return userFindIterable.first();
        }catch (Exception e){
            return null ;
        }

    }

    public boolean deleteUser(String username){
        try{
            DeleteResult del = userCollection.deleteOne(eq("username", username));
            if(del.getDeletedCount()==1){
                return true;
            }
            /*
            Hapus Playlist yang bersangkutan
             */
            return false;
        }catch (Exception e){
            return false;
        }
    }

    public String createSinger(String email , String username , String name , String password , String filepath){
        try {

            System.out.println("lOOKING FOR EMAIL :" + email + " &"  + " username :" + username);
            // Query or Looking for identic email and username
            FindIterable<Singer> findEmail = singerCollection.find(eq("email",email));

            MongoCursor<Singer> cursorEmail = findEmail.iterator();

            if(cursorEmail.hasNext()){
                return "EMAIL IS EXIST";
            }else{
                FindIterable<Singer> findUsername =  singerCollection.find(eq("username", username));
                MongoCursor<Singer> cursorUsername = findUsername.iterator();
                if(cursorUsername.hasNext()){
                    return "USERNAME IS EXIST";
                }else{
                    byte[] imageByte = extractBytes(filepath);
                    Singer singer = new Singer(email,username,password,name,imageByte);
                    singerCollection.insertOne(singer);
                    return "CONGRATZ YOUR SIGNED UP SUCCESS";
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to Input Artist";
        }
    }

    public Singer logInSinger(String username){
        try{
            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.put("username", username);
            FindIterable<Singer> singerFindIterable = singerCollection.find(searchQuery);
            return singerFindIterable.first();
        }catch (Exception e){
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Singer> searchBySinger(String searchKey){
        try{
            List<Singer> listSinger;
            Pattern pattern = Pattern.compile(".*"+Pattern.quote(searchKey)+".*", Pattern.CASE_INSENSITIVE);
            listSinger = singerCollection.find(
                    regex("name",pattern)).into(new ArrayList<>());
            return listSinger;

        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

    }

        public String getSingerName(String singerID){
            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.put("username", singerID);
            FindIterable<Singer> singerFindIterable = singerCollection.find(searchQuery);
            return singerFindIterable.first().getName();
        }




    public boolean updateSinger(String username, String filepath , boolean isTrue){
        try{

            byte[] imageByte = extractBytes(filepath);


            UpdateResult updateResult =
                    singerCollection.updateOne(eq("username", username), combine(
                            set("image", imageByte)));
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateSinger(String username , String password){
        try{
            UpdateResult updateResult =
                    singerCollection.updateOne(eq("username", username), combine(
                            set("password",password )));
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteSinger(String username){
        try{
            DeleteResult del = singerCollection.deleteOne(eq("username", username));
            if(del.getDeletedCount()==1){
                //Delete the album of this singer
                BasicDBObject searchQuery = new BasicDBObject();
                searchQuery.put("singerID", username);
                albumCollection.deleteMany(searchQuery);

                //Delete music of the singer
                FindIterable<Music> musicFindIterable = collection.find(searchQuery);
                for(Music music : musicFindIterable){
                    ObjectId musicObjectID = new ObjectId(music.getMusicCode());
                    musicBucket.delete(musicObjectID);
                }
                collection.deleteMany(searchQuery);
                return true;
            }
            return false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean createAlbum(String title , String singerID , String filepath){
        String id = new ObjectId().toString();
        try{
            byte[] imageByte = extractBytes(filepath);
            Date date = new Date();
            Album album = new Album(id,title,date,singerID,imageByte);
            albumCollection.insertOne(album);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public List<Album> getAlbumBySinger(String singerID){
        try{
            List<Album> albumList = new ArrayList<>();
            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.put("singerID", singerID);
            FindIterable<Album> albumFindIterable = albumCollection.find(searchQuery);
            for(Album album : albumFindIterable){
                albumList.add(album);
            }
            return albumList;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public List<Album> searchByAlbum(String searchKey){
        try{
            List<Album> listAlbum;
            Pattern pattern = Pattern.compile(".*"+Pattern.quote(searchKey)+".*", Pattern.CASE_INSENSITIVE);
            listAlbum = albumCollection.find(
                    regex("title",pattern)).into(new ArrayList<>());
            return listAlbum;

        }catch(Exception e){
            return null;
        }

    }

    public boolean updateAlbum(String code ,String title , String filepath){
        try{
            byte[] image = extractBytes(filepath);
            UpdateResult updateResult =
                    albumCollection.updateOne(eq("code", code), combine(
                            set("title",title),
                            set("image", image)));
            return true;

        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }


    public boolean updateAlbum(String code ,String title){
        try{
            UpdateResult updateResult =
                    albumCollection.updateOne(eq("code", code), combine(
                            set("title",title)));
            return true;

        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteAlbum(String code){
        try{
            DeleteResult del = albumCollection.deleteOne(eq("code", code));
            DeleteResult delMusic = collection.deleteMany(eq("albumID", code));
            return true;
        }catch (Exception e){
            return false;
        }
    }


    private static byte[] extractBytes(String imgPath) throws IOException {
        File file = new File(imgPath);
        BufferedImage originalImage = ImageIO.read(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(originalImage, "png", baos);
        byte[] imageInByte = baos.toByteArray();
        return imageInByte;
    }

    public boolean addPlaylist(String username,List<Playlist> playlist ){
        try{
            UpdateResult result = userCollection
                    .updateOne(Filters.eq("username",username), set("playlist",playlist));
            if(result.getModifiedCount()==1){
                return true;
            }
            return false;
        }catch (Exception e){
            return false;
        }

    }

    public boolean updatePlaylist(String username,List<Playlist> playlists){
        return addPlaylist(username,playlists);
    }

    public boolean deletePlaylist(String username,List<Playlist> playlistList){
        return addPlaylist(username,playlistList);
    }






}

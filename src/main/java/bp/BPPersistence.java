/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bp;

import org.bson.Document;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kaza_
 */
public class BPPersistence {
    
    public static final String FILTERS = "com.mongodb.client.model.Filters.*";
    public static final String UPDATES = "com.mongodb.client.model.Updates.*";
    
    private static ResourceBundle rb = ResourceBundle.getBundle("config");
    
    MongoDatabase database;
    
    public void initiliaze() throws Exception {
        try {   
            System.out.println(">> " + rb.getString("databaseUrl"));
            //Create client for connection
            MongoClient mongoClient = MongoClients.create(rb.getString("databaseUrl"));
            
            System.out.println(">> " + rb.getString("databaseName"));
            //Connect to DB
            database = (MongoDatabase) mongoClient.getDatabase(rb.getString("databaseName"));
            System.out.println("Name: " + database.getName() );           

            MongoCollection<Document> collection = database.getCollection(rb.getString("databaseCollection"));
            for (String name : database.listCollectionNames()) {
                System.out.println(name);
            }
        }
        catch(Exception e) {
            Logger.getLogger(BPPersistence.class.getName()).log(Level.WARNING, null, e.getMessage());
            throw new Exception("Cannot connect to database.");
        }
    }
    
    /**
    public MongoCollection<Document> accessCollecton() throws Exception {
        try {
            //MongoCollection instances are immutable.
            MongoCollection<Document> collection = database.getCollection("customers");
            return collection;
        }
        catch(Exception e) {
            throw new Exception("Cannot retrieve a collection from the db.");
        }        
    }
    
    public FindIterable<Document> accessCollectonByUser(String email) throws Exception {
        FindIterable<Document> findIt = null;
        try {
            MongoCollection<Document> collection = database.getCollection("customers");
            Bson bsonFilter = Filters.eq("email", email);                        
            findIt = collection.find(bsonFilter);
            //collection.find(Filters.eq("email", email)).forEach(printBlock);
        }
        catch(Exception e) {
            e.printStackTrace(); 
            System.out.println(">>> " + e.getMessage());
            throw new Exception("Cannot retrieve collection By User.");
        } 
        return findIt;
    }
    */
    public boolean insertDocument(Document doc) throws Exception {
        try {
            MongoCollection<Document> collection = database.getCollection(rb.getString("databaseCollection"));
            collection.insertOne(doc);
            return true;
        }
        catch(Exception e) {
            throw new Exception("Cannot insert document into collection in db.");
        }        
    }
    
}

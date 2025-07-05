package dal;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import models.Message;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class MongoMessageRepository implements IMessageRepository {
    private final String uri = "mongodb://localhost:27017";
    private final String databaseName = "ToDoApp";
    private final MongoClient mongoClient;

    public MongoMessageRepository() {
        this.mongoClient = MongoClients.create(uri);
    }

    private MongoCollection<Document> getMessageCollection() {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        return database.getCollection("Messages");
    }

    private Document toDocument(Message message) {
        Document doc = new Document();
        if (message.getId() != null) {
            doc.append("_id", message.getId());
        }
        doc.append("username", message.getUsername());
        doc.append("content", message.getContent());
        // Convert LocalDateTime to Date for MongoDB storage
        Date timestamp = Date.from(message.getTimestamp().atZone(ZoneId.systemDefault()).toInstant());
        doc.append("timestamp", timestamp);
        return doc;
    }

    private Message fromDocument(Document doc) {
        ObjectId id = doc.getObjectId("_id");
        String username = doc.getString("username");
        String content = doc.getString("content");

        // Convert Date back to LocalDateTime
        Date timestampDate = doc.getDate("timestamp");
        LocalDateTime timestamp = timestampDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        return new Message(id, username, content, timestamp);
    }

    @Override
    public void add(Message message) {
        Document doc = toDocument(message);
        getMessageCollection().insertOne(doc);
        // Set the generated ID back to the message object
        message.setId(doc.getObjectId("_id"));
    }

    @Override
    public void remove(ObjectId id) {
        getMessageCollection().deleteOne(eq("_id", id));
    }

    @Override
    public void update(Message message) {
        Date timestamp = Date.from(message.getTimestamp().atZone(ZoneId.systemDefault()).toInstant());

        getMessageCollection().updateOne(
                eq("_id", message.getId()),
                combine(
                        set("username", message.getUsername()),
                        set("content", message.getContent()),
                        set("timestamp", timestamp)
                )
        );
    }

    @Override
    public List<Message> findAll() {
        List<Message> messages = new ArrayList<>();
        // Sort by timestamp ascending (oldest first)
        var cursor = getMessageCollection().find().sort(Sorts.ascending("timestamp"));

        for (Document doc : cursor) {
            messages.add(fromDocument(doc));
        }

        return messages;
    }

    @Override
    public List<Message> findRecent(int limit) {
        List<Message> messages = new ArrayList<>();
        // Sort by timestamp descending (newest first) and limit
        var cursor = getMessageCollection().find()
                .sort(Sorts.descending("timestamp"))
                .limit(limit);

        for (Document doc : cursor) {
            messages.add(fromDocument(doc));
        }

        // Reverse to get chronological order (oldest first)
        messages.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
        return messages;
    }

    @Override
    public void deleteOldMessages(int keepCount) {
        // Get total count
        long totalCount = getMessageCollection().countDocuments();

        if (totalCount > keepCount) {
            // Find the timestamp of the (totalCount - keepCount)th oldest message
            var oldestToDelete = getMessageCollection().find()
                    .sort(Sorts.descending("timestamp"))
                    .skip(keepCount - 1)
                    .limit(1)
                    .first();

            if (oldestToDelete != null) {
                Date cutoffDate = oldestToDelete.getDate("timestamp");
                // Delete all messages older than the cutoff
                getMessageCollection().deleteMany(
                        new Document("timestamp", new Document("$lt", cutoffDate))
                );
            }
        }
    }

    @Override
    public Message findById(ObjectId id) {
        Document doc = getMessageCollection().find(eq("_id", id)).first();
        return doc != null ? fromDocument(doc) : null;
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
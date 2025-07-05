package dal;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class MongoRepository implements IRepository {
    private final String uri = "mongodb://localhost:27017";
    private final String databaseName = "ToDoApp";
    private final MongoClient mongoClient;
    private final String username;

    public MongoRepository(String username) {
        this.mongoClient = MongoClients.create(uri);
        this.username = username;
    }

    private MongoCollection<Document> getTodoCollection() {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        return database.getCollection("Todo");
    }

    private Document toDocument(TodoTask task) {
        Document doc = new Document();
        if (task.getId() != null) {
            doc.append("_id", task.getId());
        }
        doc.append("username", username);
        doc.append("name", task.getName());
        doc.append("description", task.getDescription());
        doc.append("content", task.getContent());
        doc.append("status", task.getStatus().getDisplayName()); // NEW: Save status
        return doc;
    }

    @Override
    public String getCurrentUser() {
        return this.username;
    }

    @Override
    public void add(TodoTask task) {
        task.setUsername(username);
        // Ensure status is set to default if null
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.NOT_STARTED);
        }
        getTodoCollection().insertOne(toDocument(task));
    }

    @Override
    public void remove(ObjectId id) {
        getTodoCollection().deleteOne(and(eq("_id", id), eq("username", username)));
    }

    @Override
    public void update(TodoTask task) {
        getTodoCollection().updateOne(
                and(eq("_id", task.getId()), eq("username", username)),
                combine(
                        set("name", task.getName()),
                        set("description", task.getDescription()),
                        set("content", task.getContent()),
                        set("status", task.getStatus().getDisplayName()) // NEW: Update status
                )
        );
    }

    public List<TodoTask> findAll() {
        List<TodoTask> tasks = new ArrayList<>();
        var cursor = getTodoCollection().find(eq("username", username));

        for (Document doc : cursor) {
            // NEW: Load status from database
            String statusString = doc.getString("status");
            TaskStatus status = TaskStatus.fromString(statusString);

            TodoTask task = new TodoTask(
                    doc.getString("name"),
                    doc.getString("description"),
                    doc.getString("content"),
                    status // NEW: Pass status to constructor
            );
            task.setId(doc.getObjectId("_id"));
            task.setUsername(username);
            tasks.add(task);
        }

        return tasks;
    }
}

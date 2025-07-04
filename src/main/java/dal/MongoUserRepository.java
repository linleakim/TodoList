package dal;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

public class MongoUserRepository implements IUserRepository {
    private final MongoCollection<Document> users;

    public MongoUserRepository() {
        var client = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase db = client.getDatabase("ToDoApp");
        this.users = db.getCollection("Users");
    }

    @Override
    public boolean register(String username, String password) {
        if (users.find(eq("username", username)).first() != null)
            return false;

        Document user = new Document("username", username)
                .append("password", password);
        users.insertOne(user);
        return true;
    }

    @Override
    public boolean login(String username, String password) {
        Document user = users.find(eq("username", username)).first();
        return user != null && user.getString("password").equals(password);
    }
}


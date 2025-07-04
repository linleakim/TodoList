package dal;

import org.bson.types.ObjectId;

public class TodoTask {
    private String name;
    private String description;
    private String content;
    private ObjectId _id;
    private String username; // 

    public TodoTask(String name, String description, String content) {
        this.name = name;
        this.description = description;
        this.content = content;
    }

    public ObjectId getId() { return _id; }
    public void setId(ObjectId id) { this._id = id; }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getContent() { return content; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}

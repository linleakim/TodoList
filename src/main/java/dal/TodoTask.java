package dal;

import org.bson.types.ObjectId;

public class TodoTask {
    private String name;
    private String description;
    private String content;
    private ObjectId _id;
    private String username;
    private TaskStatus status;

    public TodoTask(String name, String description, String content) {
        this.name = name;
        this.description = description;
        this.content = content;
        this.status = TaskStatus.NOT_STARTED; // NEW: Default status
    }

    public TodoTask(String name, String description, String content, TaskStatus status) {
        this.name = name;
        this.description = description;
        this.content = content;
        this.status = status != null ? status : TaskStatus.NOT_STARTED;
    }

    public ObjectId getId() { return _id; }
    public void setId(ObjectId id) { this._id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; } // NEW: Setter for editing

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; } // NEW: Setter for editing

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; } // NEW: Setter for editing

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    // NEW: Status getters and setters
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    @Override
    public String toString() {
        return name + " [" + status + "]"; // Show status in list
    }
}
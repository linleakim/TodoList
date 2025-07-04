package dal;
import org.bson.types.ObjectId;

import java.util.List;

public interface IRepository {
    String getCurrentUser();
    void add(TodoTask task);
    void remove(ObjectId id);
    void update(TodoTask task);
    List<TodoTask> findAll();
}

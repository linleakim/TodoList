package dal;

import models.Message;
import org.bson.types.ObjectId;
import java.util.List;

public interface IMessageRepository {
    void add(Message message);
    List<Message> findAll();
    List<Message> findRecent(int limit);
    void deleteOldMessages(int keepCount);
    Message findById(ObjectId id);
    void remove(ObjectId id);
    void update(Message message);
}

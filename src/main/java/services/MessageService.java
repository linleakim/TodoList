package services;

import dal.IMessageRepository;
import models.Message;
import java.util.List;

public class MessageService {
    private final IMessageRepository messageRepository;

    public MessageService(IMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message sendMessage(String username, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        Message message = new Message(username, content.trim());
        messageRepository.add(message);
        return message;
    }

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    public List<Message> getRecentMessages(int limit) {
        return messageRepository.findRecent(limit);
    }

    public void deleteOldMessages(int keepCount) {
        messageRepository.deleteOldMessages(keepCount);
    }

    public Message getLatestMessage() {
        return messageRepository.getLatestMessage();
    }
}

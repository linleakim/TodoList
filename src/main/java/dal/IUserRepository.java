package dal;

public interface IUserRepository {
    boolean register(String username, String password);
    boolean login(String username, String password);
}
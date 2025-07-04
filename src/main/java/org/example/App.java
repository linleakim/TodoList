package org.example;

import dal.IUserRepository;
import dal.MongoRepository;
import dal.MongoUserRepository;
import ui.LoginFrame;
import ui.TodoSplitApp;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        IUserRepository userRepo = new MongoUserRepository();
        SwingUtilities.invokeLater(() -> new LoginFrame(userRepo).setVisible(true));
    }
}


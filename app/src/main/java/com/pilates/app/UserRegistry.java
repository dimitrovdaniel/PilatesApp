package com.pilates.app;

import com.pilates.app.model.UserSession;

public class UserRegistry {
    private static UserRegistry instance = new UserRegistry();
    private UserSession userSession;

    private UserRegistry() {
    }

    public void saveUser(final UserSession userSession) {
        this.userSession = userSession;
    }


    public UserSession getUser() {
        if (userSession == null) {
            throw new RuntimeException("User session is not exist");
        }
        return userSession;
    }

    public static UserRegistry getInstance() {
        if (instance == null) {
             instance = new UserRegistry();
        }

        return instance;
    }

}

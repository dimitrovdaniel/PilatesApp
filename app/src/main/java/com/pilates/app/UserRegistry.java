package com.pilates.app;

import android.os.Build;
import android.os.Handler;
import android.os.Message;

import com.pilates.app.model.UserSession;
import com.pilates.app.model.dto.UserItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.RequiresApi;


public class UserRegistry {
    private static UserRegistry instance = new UserRegistry();
    private final Map<String, String> trainersById = new ConcurrentHashMap<>();

    private UserSession userSession;
    private Handler handler;

    private UserRegistry() {

    }


    public void setHandler(Handler handler) {
        this.handler = handler;
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


    public List<UserItem> getTrainerItems() {
        final List<UserItem> userItems = new ArrayList<>();
        trainersById.entrySet().forEach(entry -> userItems.add(new UserItem(entry.getValue(), entry.getKey())));
        return userItems;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void putAllTrainers(final Map<String, String> trainees) {
        System.out.println("PUTTING ALL TRAINEES");
        trainersById.putAll(trainees);
        trainersById.entrySet().forEach(entry -> addToTrainerList(new UserItem(entry.getValue(), entry.getKey())));
    }

    public void putTrainer(final String id, final String username) {
        trainersById.put(id, username);
        System.out.println("PUT TRAINEE: " + username + " with id: " + id);
        addToTrainerList(new UserItem(username, id));
    }

    private void addToTrainerList(final UserItem userItem) {
        if (handler != null) {
            final Message message = handler.obtainMessage(1, userItem);
            handler.sendMessage(message);
        }
    }

    public void remove() {

    }

}

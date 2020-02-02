package com.pilates.app;

import android.os.Build;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.RequiresApi;

import com.pilates.app.model.UserSession;
import com.pilates.app.model.dto.UserDto;
import com.pilates.app.model.dto.UserItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UserRegistry {
    private static UserRegistry instance = new UserRegistry();
    private final Map<String, String> trainersById = new HashMap<>();
    private final Map<String, String> traineesById = new HashMap<>();

    private UserSession userSession;
    private UserDto dto;
    private Handler handler;

    private UserRegistry() {

    }


    public void setHandler(Handler handler) {
        this.handler = handler;
    }


    public void saveUser(final UserSession userSession) {
        this.userSession = userSession;
    }

    public void saveDto(final UserDto dto) {
        this.dto = dto;
    }

    public UserDto getDto() {
        return dto;
    }


    public UserSession getUser() {
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

    public void removeTrainer(final String id) {
        trainersById.remove(id);
    }

    public void addTrainee(final String id, final String username) {
        traineesById.put(id, username);
    }

    public void removeTrainee(final String id) {
        traineesById.remove(id);
    }

    public int getTraineeCount() {
        return traineesById.size();
    }

}

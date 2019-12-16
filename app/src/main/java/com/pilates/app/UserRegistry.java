package com.pilates.app;

import android.app.Activity;
import android.content.Context;
import android.widget.ArrayAdapter;

import com.pilates.app.listeners.DataChangedListener;
import com.pilates.app.model.UserSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserRegistry {
    private static UserRegistry instance;
    private final ConcurrentHashMap<String, String> traineesById = new ConcurrentHashMap<>();
    private ArrayAdapter<String> traineeAdapter;
    private final List<String> traineeNames = new ArrayList<>();

    private UserSession userSession;
    private DataChangedListener listener;

    private UserRegistry() {

    }

    public void setListener(DataChangedListener listener) {
        this.listener = listener;
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


    public List<String> getTraineeNames() {
        traineeNames.addAll(traineesById.values());
        return traineeNames;
    }

    public void putAllTrainees(final Map<String, String> trainees) {
        traineesById.putAll(trainees);
        if (this.traineeAdapter != null) {
            this.traineeAdapter.clear();
            this.traineeAdapter.addAll(getTraineeNames());
            System.out.println("[USER REGISTRY] called pre change");
            this.listener.changed();
        }
    }

    public void putTrainee(final String id, final String username) {
        traineesById.put(id, username);
        if (this.traineeAdapter != null) {
            this.traineeAdapter.add(username);
            System.out.println("[USER REGISTRY] called pre change");
            this.listener.changed();
        }
    }

    public void remove() {

    }

    public void setAdapter(final ArrayAdapter<String> traineeAdapter) {
        this.traineeAdapter = traineeAdapter;
    }

}

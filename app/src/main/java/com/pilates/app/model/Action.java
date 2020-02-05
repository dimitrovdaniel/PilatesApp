package com.pilates.app.model;

import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;

public class Action {
    private ActionType type;
    private ActionBody body;
    private LocalDateTime time;

    public Action(ActionType type) {
        this.type = type;
    }

    public Action(ActionType type, ActionBody body) {
        this.type = type;
        this.body = body;
    }

    public Action(ActionType type, LocalDateTime time) {
        this.type = type;
        this.time = time;
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this, Action.class);
    }


    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public ActionBody getBody() {
        return body;
    }

    public void setBody(ActionBody body) {
        this.body = body;
    }
}

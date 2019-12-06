package com.pilates.app.model;

import com.google.gson.GsonBuilder;

import java.util.Map;


public class ActionBody {
    //ws id
    private String id;
    private String name;
    private UserRole role;
    private String offer;
    private String answer;
    private Candidate candidate;
    //wsid, name
    private Map<String, String> registeredUsers;

    /*default*/ ActionBody(final Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.role = builder.role;
        this.offer = builder.offer;
        this.answer = builder.answer;
        this.candidate = builder.candidate;
        this.registeredUsers = builder.registeredUsers;
    }

    public static Builder newBuilder() {
        return new Builder();
    }



    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this, ActionBody.class);
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = offer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public Map<String, String> getRegisteredUsers() {
        return registeredUsers;
    }

    public void setRegisteredUsers(Map<String, String> registeredUsers) {
        this.registeredUsers = registeredUsers;
    }

    public static final class Builder {

        private String id;
        private String name;
        private UserRole role;
        private String offer;
        private String answer;
        private Candidate candidate;
        private Map<String, String> registeredUsers;

        /* default */ Builder() {}

        public Builder withId(final String id) {
            this.id = id;
            return this;
        }

        public Builder withName(final String name) {
            this.name = name;
            return this;
        }

        public Builder withOffer(final String offer) {
            this.offer = offer;
            return this;
        }

        public Builder withAnswer(final String answer) {
            this.answer = answer;
            return this;
        }

        public Builder withIceCandidate(final Candidate candidate) {
            this.candidate = candidate;
            return this;
        }
        public Builder withRegisteredUsers(final Map<String, String> users) {
            this.registeredUsers = users;
            return this;
        }

        public Builder withRole(final UserRole role) {
            this.role = role;
            return this;
        }

        public ActionBody build() {
            return new ActionBody(this);
        }

    }
}

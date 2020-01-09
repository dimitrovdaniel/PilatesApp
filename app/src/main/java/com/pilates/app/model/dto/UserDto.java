package com.pilates.app.model.dto;

import com.google.gson.GsonBuilder;

import com.pilates.app.model.UserRole;


public class UserDto {

    private String username;
    private String email;
    private String password;
    private UserRole role;

    public UserDto(final Builder builder) {
        this.username = builder.username;
        this.password = builder.password;
        this.email = builder.email;
        this.role = builder.role;
    }

    public static Builder newBuilder() {
        return new Builder();
    }
    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this, UserDto.class);
    }


    public static final class Builder {
        private String username;
        private String password;
        private String email;
        private UserRole role;

        public Builder withUsername(final String username) {
            this.username = username;
            return this;
        }

        public Builder withEmail(final String email) {
            this.email = email;
            return this;
        }
        public Builder withPassword(final String password) {
            this.password = password;
            return this;
        }
        public Builder withRole(final UserRole role) {
            this.role = role;
            return this;
        }

        public UserDto build() {
            return new UserDto(this);
        }
    }
}

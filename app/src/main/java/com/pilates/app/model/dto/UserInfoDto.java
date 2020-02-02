package com.pilates.app.model.dto;

import com.pilates.app.model.UserRole;

public class UserInfoDto {

    private String id;
    private String name;
    private UserRole role;

    public UserInfoDto(String id, String name, UserRole role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UserRole getRole() {
        return role;
    }
}

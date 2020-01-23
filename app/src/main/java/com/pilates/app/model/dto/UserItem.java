package com.pilates.app.model.dto;

import java.util.Objects;

public class UserItem {
    private final String name;
    private final String infoId;

    public UserItem(String name, String infoId) {
        this.name = name;
        this.infoId = infoId;
    }

    public String getName() {
        return name;
    }

    public String getInfoId() {
        return infoId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserItem userItem = (UserItem) o;
        return Objects.equals(name, userItem.name) &&
                Objects.equals(infoId, userItem.infoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, infoId);
    }
}

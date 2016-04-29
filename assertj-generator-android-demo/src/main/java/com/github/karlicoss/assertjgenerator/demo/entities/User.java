package com.github.karlicoss.assertjgenerator.demo.entities;

import java.util.List;

public class User {

    private final String name;

    private final boolean premium;

    private final List<String> permissions;

    public User(String name, boolean premium, List<String> permissions) {
        this.name = name;
        this.premium = premium;
        this.permissions = permissions;
    }

    public String getName() {
        return name;
    }

    public boolean isPremium() {
        return premium;
    }

    public List<String> getPermissions() {
        return permissions;
    }
}

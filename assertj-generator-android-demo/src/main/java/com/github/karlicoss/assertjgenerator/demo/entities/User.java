package com.github.karlicoss.assertjgenerator.demo.entities;

import android.os.Bundle;

import java.util.ArrayList;
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

    // only to ensure plugin classloads Android classes properly
    public Bundle packInBundle() {
        Bundle bundle = new Bundle(3);
        bundle.putString("name", name);
        bundle.putBoolean("premium", premium);
        bundle.putStringArrayList("permissions", new ArrayList<>(permissions));
        return bundle;
    }
}

package com.example.quizapp;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private static final String PREFS = "quiz_prefs";
    private static final String KEY_USERS = "users_list";
    private static final String KEY_CURRENT = "username";
    private static final int MAX_USERS = 4;

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public UserManager(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /**
     * Load all saved usernames (most recent last).
     */
    public List<String> loadUsers() {
        String json = prefs.getString(KEY_USERS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(json, type);
    }

    /**
     * Persist the full user list.
     */
    private void saveUsers(List<String> users) {
        prefs.edit()
                .putString(KEY_USERS, gson.toJson(users))
                .apply();
    }

    /**
     * Add (or bump) a username into the list, evicting oldest if > MAX_USERS,
     * then set them as current.
     */
    public void addUserAndSetCurrent(String username) {
        List<String> users = loadUsers();
        if (users.contains(username)) {
            users.remove(username);
        } else if (users.size() >= MAX_USERS) {
            users.remove(0);
        }
        users.add(username);
        saveUsers(users);
        setCurrent(username);
    }

    /**
     * Mark a saved user as the current active user.
     */
    public void setCurrent(String username) {
        prefs.edit()
                .putString(KEY_CURRENT, username)
                .apply();
    }

    /**
     * Fetch the current active username.
     */
    public String getCurrent() {
        return prefs.getString(KEY_CURRENT, null);
    }

    /**
     * Check if this username exists in the saved list.
     */
    public boolean exists(String username) {
        return loadUsers().contains(username);
    }

    /** Save email for a specific user */
    public void setEmail(String username, String email) {
        prefs.edit()
                .putString("email_" + username, email)
                .apply();
    }

    /** Get email for the current user */
    public String getCurrentEmail() {
        String user = getCurrent();
        return user == null
                ? null
                : prefs.getString("email_" + user, null);
    }

    /** Save plan for a specific user ("Free", "Pro", "Premium") */
    public void setPlan(String username, String plan) {
        prefs.edit()
                .putString("plan_" + username, plan)
                .apply();
    }

    /** Get the plan for the current user, defaulting to "Free" */
    public String getCurrentPlan() {
        String user = getCurrent();
        if (user == null) return "Free";
        String plan = prefs.getString("plan_" + user, null);
        return plan != null ? plan : "Free";
    }
}

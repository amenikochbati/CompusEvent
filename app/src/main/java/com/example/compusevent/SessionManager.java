package com.example.compusevent;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "CampusEventsSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_ROLE = "userRole";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_ROLE, user.getRole());
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public User getUser() {
        if (!isLoggedIn()) return null;
        int id = pref.getInt(KEY_USER_ID, -1);
        String name = pref.getString(KEY_USER_NAME, "");
        String email = pref.getString(KEY_USER_EMAIL, "");
        String role = pref.getString(KEY_USER_ROLE, User.ROLE_USER);
        return new User(id, name, email, "", role);
    }

    public boolean isAdmin() {
        User user = getUser();
        return user != null && user.isAdmin();
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}

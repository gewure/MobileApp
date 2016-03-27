package com.example.f00.mobileapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;

/**
 * Created by mcavero on 3/27/16.
 */
public class LoginManager {

    static final String STATE = "status";
    static final String USER_NAME = "user_name";
    static final String USER_EMAIL = "user_email";
    static final String USER_ID_TOKEN = "user_id_token";
    static final String CGM_REG_TOKEN = "cgm_reg_token";

    public static SharedPreferences getSharedPreferences(Context ctx)
    {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public void setState(Context ctx, boolean state) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putBoolean(STATE, state);
        editor.commit();
    }

    public void setName(Context ctx, String name) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(USER_NAME, name);
        editor.commit();
    }

    public void setEmail(Context ctx, String email) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(USER_EMAIL, email);
        editor.commit();
    }

    public void setIdToken(Context ctx, String idToken) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(USER_ID_TOKEN, idToken);
        editor.commit();
    }

    public void setRegToken(Context ctx, String regToken) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(CGM_REG_TOKEN, regToken);
        editor.commit();
    }

    public boolean isLogged(Context ctx) {
       return getSharedPreferences(ctx).getBoolean(STATE,false);
    }

    public String getName(Context ctx) {
        return getSharedPreferences(ctx).getString(USER_NAME, "bad_name");
    }

    public String getEmail(Context ctx) {
        return getSharedPreferences(ctx).getString(USER_EMAIL,"bad_email");
    }

    public String getIdToken(Context ctx) {
        return getSharedPreferences(ctx).getString(USER_ID_TOKEN,"bad_token");
    }

    public String getRegToken(Context ctx) {
        return getSharedPreferences(ctx).getString(CGM_REG_TOKEN,"bad_cgm");
    }

}

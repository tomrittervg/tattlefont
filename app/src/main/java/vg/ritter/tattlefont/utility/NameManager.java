package vg.ritter.tattlefont.utility;

import android.content.Context;
import android.content.SharedPreferences;

public class NameManager {
    private static final String PREF_NAME = "TattleFontData";
    private static final String KEY_NAME = "name";

    // Save the user's name
    public static void saveName(Context context, String name) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_NAME, name);
        editor.apply();
    }

    // Retrieve the saved user's name
    public static String getSavedName(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_NAME, null);
    }
}


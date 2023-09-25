package vg.ritter.tattlefont.utility;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class UUIDManager {
    private static final String PREFS_NAME = "TattleFontData";
    private static final String UUID_KEY = "UUID";

    public static String getUUID(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String uuidString = preferences.getString(UUID_KEY, null);
        String uuid;

        if (uuidString == null) {
            uuid = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(UUID_KEY, uuid);
            editor.apply();
        } else {
            uuid = uuidString;
        }

        return uuid;
    }
}
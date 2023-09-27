package vg.ritter.tattlefont;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.LocaleList;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import vg.ritter.tattlefont.utility.NameManager;
import vg.ritter.tattlefont.utility.UUIDManager;

public class JSONSubmission {
    JSONObject submission;

    public JSONSubmission(Context context, WeirdFontLogger weirdFontLogger, HashSet<FontDetails> fonts) throws Exception {
        this.submission = JSONSubmission.GetObject(context);
        this.submission.put("brand", Build.BRAND);
        this.submission.put("device", Build.DEVICE);
        this.submission.put("hardware", Build.HARDWARE);
        this.submission.put("manufacturer", Build.MANUFACTURER);
        this.submission.put("model", Build.MODEL);
        this.submission.put("product", Build.PRODUCT);
        this.submission.put("release_version", Build.VERSION.RELEASE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.submission.put("security_patch", Build.VERSION.SECURITY_PATCH);
            this.submission.put("base_os", Build.VERSION.BASE_OS);
        } else {
            this.submission.put("security_patch", "too-low-version");
            this.submission.put("base_os", "too-low-version");
        }

        Configuration config = context.getResources().getConfiguration();
        this.submission.put("current_locale", config.locale.toString());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            LocaleList supportedLocales = LocaleList.getDefault();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < supportedLocales.size(); i++) {
                Locale locale = supportedLocales.get(i);
                sb.append(locale.toString());
                sb.append(",");
            }
            this.submission.put("all_locales", sb.toString());
        } else {
            this.submission.put("all_locales", "too-low-version");
        }


        JSONArray fontArr = new JSONArray();

        for (FontDetails fontDetails : fonts) {
            fontArr.put(fontDetails.toJson());
        }

        this.submission.put("fonts", fontArr);
        this.submission.put("errors", weirdFontLogger.toJson());
    }

    public static JSONObject GetObject(Context context) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uuid", UUIDManager.getUUID(context));
        obj.put("name", NameManager.getSavedName(context));
        return obj;
    }

    @NonNull
    @Override
    public String toString() {
        return this.submission.toString();
    }
}

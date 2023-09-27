package vg.ritter.tattlefont;

import android.graphics.fonts.Font;
import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FontFinder {
    public static int GetNumberOfFonts() {
        return GetAPIFonts().size() + GetSystemFonts().size();
    }


    static List<FontDetails> ReadAllFonts(WeirdFontLogger weirdFontLogger) throws Exception {
        Set<FontDetails> fonts = new HashSet<>();

        for(String path : GetSystemFonts()) {
            fonts.add(ReadFont("/system", path, weirdFontLogger));
        }
        for(String path : GetAPIFonts()) {
            fonts.add(ReadFont("getAvailFonts", path, weirdFontLogger));
        }

        ArrayList<FontDetails> fontList = new ArrayList<FontDetails>();
        fontList.addAll(fonts);
        return fontList;
    }

    public static FontDetails ReadFont(String src, String path, WeirdFontLogger weirdFontLogger) throws Exception {
        FontDetails f;
        try {
            return new FontDetails(src, path);
        } catch (Exception e) {
            weirdFontLogger.Add(path, e);
        }
        return null;
    }

    private static List<String> cachedSystemFonts;
    private static List<String> cachedAPIFonts;
    public static List<String> GetSystemFonts() {
        if (cachedSystemFonts == null) {
            String path = "/system/fonts";
            File file = new File(path);
            File ff[] = file.listFiles();
            cachedSystemFonts = new ArrayList<String>();
            for (File f :ff) {
                cachedSystemFonts.add(f.getAbsolutePath());
            }
        }
        return cachedSystemFonts;
    }

    public static List<String> GetAPIFonts() {
        if (cachedAPIFonts == null) {
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                cachedAPIFonts = new ArrayList<String>();
            } else {
                cachedAPIFonts = new ArrayList<>();
                java.util.Set<Font> apiFonts = android.graphics.fonts.SystemFonts.getAvailableFonts();
                for (Font f : apiFonts) {
                    cachedAPIFonts.add(f.getFile().getAbsolutePath());
                }
            }
        }
        return cachedAPIFonts;
    }
}

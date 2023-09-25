package vg.ritter.tattlefont;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import vg.ritter.tattlefont.utility.Pair;

public class WeirdFontLogger implements Iterable<Pair<String, String>>, Serializable {
    List<Pair<String, String>> weirdFonts;

    public WeirdFontLogger() {
        weirdFonts = new ArrayList<>();
    }

    public int Size() {
        return this.weirdFonts.size();
    }
    void Add(String path, Exception e) throws NoSuchAlgorithmException {
        String hash = calculateFileHash(path);
        for(Pair<String, String> pair : this.weirdFonts) {
            if(pair.b.equals(hash)) {
                // We already know about this file.
                return;
            }
        }
        weirdFonts.add(new Pair<String, String>(path, hash));
    }

    @Override
    public Iterator<Pair<String, String>> iterator() {
        return new FontIterator();
    }

    private class FontIterator implements Iterator<Pair<String, String>>, Serializable {
        private int currentIndex = 0;

        @Override
        public boolean hasNext() {
            return currentIndex < weirdFonts.size();
        }

        @Override
        public Pair<String, String> next() {
            if (hasNext()) {
                return weirdFonts.get(currentIndex++);
            }
            throw new NoSuchElementException();
        }
    }
    public JSONArray toJson() throws JSONException {
        JSONArray jsonArray = new JSONArray();

        for (Pair<String, String> error : weirdFonts) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("path", error.a);
            jsonObject.put("hash", error.b);
            jsonArray.put(jsonObject);
        }

        return jsonArray;
    }

    private String calculateFileHash(String filePath) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = new byte[0];
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                digest = md.digest(Files.readAllBytes(Paths.get(filePath)));
            } else {
                FileInputStream fis = new FileInputStream(filePath);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
                fis.close();
                digest = md.digest();
            }

        StringBuilder hashBuilder = new StringBuilder();
        for (byte b : digest) {
            hashBuilder.append(String.format("%02X", b));
        }

            return hashBuilder.toString();
        } catch (IOException e) {
            return e.toString();
        }
    }
}

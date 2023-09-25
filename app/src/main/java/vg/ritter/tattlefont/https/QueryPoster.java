package vg.ritter.tattlefont.https;

import android.content.Context;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import vg.ritter.tattlefont.FontDetails;
import vg.ritter.tattlefont.WeirdFontLogger;
import vg.ritter.tattlefont.utility.OnPostExecuteCallback;
import vg.ritter.tattlefont.utility.Pair;

public class QueryPoster extends AbstractPoster<Pair<String, List<String>>> {

    private final WeirdFontLogger fontErrors;

    public QueryPoster(Context context, OnPostExecuteCallback postExecute, ProgressBar progressBar, WeirdFontLogger fontErrors) throws NoSuchAlgorithmException, KeyManagementException {
        super(context, postExecute, progressBar);
        this.fontErrors = fontErrors;
    }

    @Override
    protected Pair<String, List<String>> doInBackground(String... params) {
        try {
            JSONArray jsonArray = new JSONArray();

            // Calculate the hash for each file and add it to the JSON array
            for (Pair<String, String> font : fontErrors) {
                jsonArray.put(font.b);
            }

            // Create a JSON object to hold the JSON array
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("errors", jsonArray);

            // Convert the JSON object to a string
            String jsonData = jsonObject.toString();

            URL url = new URL(hostname + "/query");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method to POST
            connection.setRequestMethod("POST");

            // Set the content type to JSON
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            // Enable input/output streams for reading/writing data
            connection.setDoOutput(true);
            connection.setDoInput(true);

            // Create an output stream to write the JSON data
            OutputStream os = new BufferedOutputStream(connection.getOutputStream());
            DataOutputStream dos = new DataOutputStream(os);

            dos.writeBytes(jsonData);
            dos.flush();
            dos.close();
            os.close();

            onProgressUpdate(50);

            // Get the response code to handle success or error
            int responseCode = connection.getResponseCode();
            String response = readResponse(connection);
            onProgressUpdate(100);
            connection.disconnect();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                JSONObject responseJson = new JSONObject(response);
                List<String> hashResults = processResponse(responseJson);
                return new Pair<String, List<String>>(null, hashResults);
            } else {
                connection.disconnect();
                return new Pair<String, List<String>>(response, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new Pair<String, List<String>>("Encountered a local error, please screenshot and tell Tom:\n" + e.toString(), null);
        }
    }

    private String readResponse(HttpURLConnection connection) throws IOException {
        InputStream is = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }

    private List<String> processResponse(JSONObject responseJson) throws JSONException {
        List<String> trueHashes = new ArrayList<>();
        Iterator<String> keys = responseJson.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            boolean result = responseJson.getBoolean(key);

            // Check if the value associated with the key is true
            if (result) {
                trueHashes.add(key);
            }
        }
        return trueHashes;
    }
}
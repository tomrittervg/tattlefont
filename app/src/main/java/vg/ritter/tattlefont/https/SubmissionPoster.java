package vg.ritter.tattlefont.https;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import vg.ritter.tattlefont.JSONSubmission;
import vg.ritter.tattlefont.utility.OnPostExecuteCallback;

public class SubmissionPoster extends AbstractPoster<String> {
    private final JSONSubmission submission;

    public SubmissionPoster(Context context, OnPostExecuteCallback postExecuteCallback, ProgressBar progressBar, JSONSubmission submission) throws NoSuchAlgorithmException, KeyManagementException {
        super(context, postExecuteCallback, progressBar);
        this.submission = submission;
    }

    @Override
    protected String doInBackground(String... params) {
        URL url = null;
        try {
            url = new URL(hostname + "/submit");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return "Encountered a IOException error in SubmissionPoster openConnection, please screenshot and tell Tom:\n" + e.toString();
        }
        try {
            // Set the request method to POST
            connection.setRequestMethod("POST");

            // Set the content type to JSON
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            // Enable input/output streams for reading/writing data
            connection.setDoOutput(true);
            connection.setDoInput(true);

            // Write the JSON data to the output stream
            OutputStream os = connection.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            osw.write(submission.toString());
            osw.flush();
            osw.close();
            os.close();

            int responseCode = connection.getResponseCode();
            if(responseCode == 200) {
                onProgressUpdate(50);
                String serverResponse = getString(connection.getInputStream());
                onProgressUpdate(100);
                connection.disconnect();
                return serverResponse;
            } else {
                String serverResponse = getString(connection.getErrorStream());
                return "Encountered a Server Error (" + responseCode + ") in SubmissionPoster:\n" + serverResponse;
            }
        }catch (ProtocolException e) {
            return "Encountered a ProtocolException error in SubmissionPoster, please screenshot and tell Tom:\n" + e.toString();
        } catch (FileNotFoundException e) {
            String serverResponse = getString(connection.getErrorStream());
            e.printStackTrace();
            return "Encountered a FileNotFoundException in SubmissionPoster, please screenshot and tell Tom:\n" + serverResponse;
        } catch (IOException e) {
            e.printStackTrace();
            return "Encountered an IOException error in SubmissionPoster, please screenshot and tell Tom:\n" + e.toString();
        }
    }
    String getString(InputStream is) {
        if(is == null) {
            return "";
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
        } catch (IOException _e) {
            _e.printStackTrace();
            return "Encountered an IOException in getString in SubmissionPoster, please screenshot and tell Tom:\n" + _e.toString();
        }
        return response.toString();
    }
}

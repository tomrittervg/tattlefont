package vg.ritter.tattlefont.https;

import android.content.Context;
import android.widget.ProgressBar;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import vg.ritter.tattlefont.Utility;
import vg.ritter.tattlefont.utility.OnPostExecuteCallback;
import vg.ritter.tattlefont.utility.Pair;

public class FontPoster extends AbstractPoster<String> {
    String FilePath;
    public String FileHash;
    public Exception exception;
    public FontPoster(Context context, OnPostExecuteCallback postExecute, ProgressBar progressBar, Pair<String, Exception> error) throws Exception {
        super(context, postExecute, progressBar);
        this.FilePath = error.a;
        this.FileHash = Utility.CalculateFileHash(this.FilePath);
        this.exception = error.b;
    }

    @Override
    protected String doInBackground(String... params) {
        File f = new File(FilePath);
        String destination = hostname + "/font";
        try {
            if(!f.exists()) {
                throw new FileNotFoundException(f.getAbsolutePath() + " does not exist");
            } else if(f.isDirectory()) {
                throw new FileNotFoundException(f.getAbsolutePath() + " is a directory");
            }
            URL url = new URL(destination);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method to POST
            connection.setRequestMethod("POST");

            // Set the content type to binary data
            connection.setRequestProperty("Content-Type", "application/octet-stream");

            // Enable input/output streams for reading/writing data
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setFixedLengthStreamingMode(f.length());

            // Create an output stream to write the file
            OutputStream os = new BufferedOutputStream(connection.getOutputStream());
            DataOutputStream dos = new DataOutputStream(os);

            // Read the file and write it to the output stream
            FileInputStream fis = new FileInputStream(FilePath);
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0; // Initialize a variable to track the total bytes read

            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                int progress = (int) ((totalBytesRead * 100) / f.length());
                onProgressUpdate(progress);
            }

            // Close the streams
            fis.close();
            dos.flush();
            dos.close();
            os.close();

            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Extract the string you want from the response
            String serverResponse = response.toString();
            connection.disconnect();
            return serverResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}

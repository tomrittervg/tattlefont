package vg.ritter.tattlefont.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import vg.ritter.tattlefont.FontDetails;
import vg.ritter.tattlefont.JSONSubmission;
import vg.ritter.tattlefont.R;
import vg.ritter.tattlefont.WeirdFontLogger;

public class SubmissionStatus extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submission_status);

        try {
            HashSet<FontDetails> fonts = (HashSet<FontDetails>) getIntent().getSerializableExtra("fonts");
            WeirdFontLogger weirdFontLogger = (WeirdFontLogger) getIntent().getSerializableExtra("errors");

            if (fonts == null) {
                processResult("Received a null 'fonts' object in SubmissionStatus.", true);
                return;
            } else if (weirdFontLogger == null) {
                processResult("Received a null 'weirdFontLogger' object in SubmissionStatus.", true);
                return;
            }


            JSONSubmission submission = new JSONSubmission(SubmissionStatus.this, weirdFontLogger, fonts);
            String submissionData = submission.toString();
            saveCompressedSubmission(submissionData);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveCompressedSubmission(String data) {
        try {
            String timestamp = new SimpleDateFormat("yyMMdd-HHmmss").format(new Date());
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File zipFile = new File(downloadsDir, "font-info-" + timestamp + ".zip");

            try (FileOutputStream fos = new FileOutputStream(zipFile);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                ZipEntry entry = new ZipEntry("submission.json");
                zos.putNextEntry(entry);
                zos.write(data.getBytes());
                zos.closeEntry();
            }
            processResult("Saved submission to: " + zipFile.getAbsolutePath(), true);
        } catch (Exception e) {
            processResult("Error saving submission: " + e.getMessage(), true);
        }
    }

    private final StringBuilder results = new StringBuilder();
    private void processResult(String result, Boolean fireIntent) {
        if (result != null && result.length() > 0) {
            if (fireIntent) {
                Intent intent = new Intent(this, ResponseScreen.class);
                intent.putExtra("message", this.results.toString() + "\n" + result);
                startActivity(intent);
                finish();
            } else {
                this.results.append("Got a minor(?) error: ");
                this.results.append(result);
                this.results.append("\n");
            }
        }
    }
}

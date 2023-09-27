package vg.ritter.tattlefont.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import vg.ritter.tattlefont.FontDetails;
import vg.ritter.tattlefont.JSONSubmission;
import vg.ritter.tattlefont.R;
import vg.ritter.tattlefont.Submission;
import vg.ritter.tattlefont.WeirdFontLogger;
import vg.ritter.tattlefont.https.FontPoster;
import vg.ritter.tattlefont.https.QueryPoster;
import vg.ritter.tattlefont.https.SubmissionPoster;
import vg.ritter.tattlefont.utility.OnPostExecuteCallback;
import vg.ritter.tattlefont.utility.Pair;

public class SubmissionStatus extends Activity {

    // Initialize UI elements
    private ProgressBar queryProgressBar;
    private ProgressBar submissionProgressBar;
    private LinearLayout fontProgressBarList;
    private ArrayAdapter<ProgressBar> fontProgressBarAdapter;
    private List<FontPoster> fontPosters; // List of FontPoster instances

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submission_status);
        queryProgressBar = findViewById(R.id.queryProgressBar);
        submissionProgressBar = findViewById(R.id.submissionProgressBar);
        fontProgressBarList = findViewById(R.id.fontProgressBarList);
        fontPosters = new ArrayList<>();

        try {
            HashSet<FontDetails> fonts = (HashSet<FontDetails>) getIntent().getSerializableExtra("fonts");
            WeirdFontLogger weirdFontLogger = (WeirdFontLogger) getIntent().getSerializableExtra("errors");

            if(fonts == null) {
                processResult("Received a null 'fonts' object in Submissionstatus.", true);
            } else if(weirdFontLogger == null) {
                processResult("Received a null 'weirdFontLogger' object in Submissionstatus.", true);
            }

            ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(this, android.R.style.Widget_ProgressBar_Horizontal);
            for (Pair<String, Exception> error : weirdFontLogger) {
                ProgressBar progressBar = new ProgressBar(contextThemeWrapper, null, android.R.attr.progressBarStyleHorizontal);
                progressBar.setMax(100);
                progressBar.setProgress(0);
                fontProgressBarList.addView(progressBar);

                if(error.b.getClass() != FileNotFoundException.class) {
                    fontPosters.add(new FontPoster(this, new OnPostExecuteCallback<String>() {
                        @Override
                        public void onPostExecute(String result) {
                            processResult(result, false);
                        }
                    }, progressBar, error));
                }
            }

            QueryPoster queryPoster = new QueryPoster(this, new OnPostExecuteCallback<Pair<String, List<String>>>() {
                @Override
                public void onPostExecute(Pair < String, List < String >> pairResult) {
                    processResult(pairResult.a, true);
                    List<String> neededFonts = pairResult.b;

                    // Schedule al the font posts
                    for(FontPoster fontPoster : fontPosters) {
                        if(neededFonts.contains(fontPoster.FileHash)) {
                            fontPoster.execute();
                        } else {
                            fontPoster.ProgressBar.setProgress(100);
                        }
                    }

                    // Start SubmissionPoster
                    try {
                        JSONSubmission submission = new JSONSubmission(SubmissionStatus.this, weirdFontLogger, fonts);
                        SubmissionPoster submissionPoster = new SubmissionPoster(SubmissionStatus.this, new OnPostExecuteCallback<String>() {
                            @Override
                            public void onPostExecute(String result) {
                                processResult(result, true);
                            }
                        }, submissionProgressBar, submission);
                        submissionPoster.execute();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }, queryProgressBar, weirdFontLogger);
            queryPoster.execute();


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private final StringBuilder results = new StringBuilder();
    private void processResult(String result, Boolean fireIntent) {
        if(result != null && result.length() > 0) {
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

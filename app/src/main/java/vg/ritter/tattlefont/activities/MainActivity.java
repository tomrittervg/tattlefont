package vg.ritter.tattlefont.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.LocaleList;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import vg.ritter.tattlefont.FontDetails;
import vg.ritter.tattlefont.FontFinder;
import vg.ritter.tattlefont.R;
import vg.ritter.tattlefont.WeirdFontLogger;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private ArrayAdapter<FontDetails> adapter;
    private ProgressBar progressBar;
    private TextView resultsText;
    private Button submitButton;
    WeirdFontLogger weirdFontLogger;
    Set<FontDetails> fonts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = findViewById(R.id.fontList);
        progressBar = findViewById(R.id.progressBar);
        resultsText = findViewById(R.id.resultsText);

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);

        int maxProgress = FontFinder.GetNumberOfFonts();
        progressBar.setMax(maxProgress); // Set the maximum value of the progress bar

        weirdFontLogger = new WeirdFontLogger();
        fonts = new HashSet<>();
        new LoadFontsTask(maxProgress).execute();
    }

    private class LoadFontsTask extends AsyncTask<Void, Integer, Set<FontDetails>> {
        private final int maxProgress;
            LoadFontsTask(int maxProgress) {
            this.maxProgress = maxProgress;
        }

        @Override
        protected Set<FontDetails> doInBackground(Void... voids) {
            int numProcessed = 0;

            try {
                for (String path : FontFinder.GetSystemFonts()) {
                    FontDetails font = FontFinder.ReadFont("/system", path, weirdFontLogger);
                    if (font != null) {
                        fonts.add(font);
                    }
                    numProcessed++;
                    publishProgress(numProcessed); // Publish the progress
                }
                for (String path : FontFinder.GetAPIFonts()) {
                    FontDetails font = FontFinder.ReadFont("getAvailFonts", path, weirdFontLogger);
                    if (font != null) {
                        fonts.add(font);
                    }
                    numProcessed++;
                    publishProgress(numProcessed); // Publish the progress
                }
            }
            catch(Exception e) {
                throw new RuntimeException(e);
            }

            return fonts;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int progress = values[0];
            progressBar.setProgress(progress); // Update the progress bar with the current progress
        }

        @Override
        protected void onPostExecute(Set<FontDetails> fontSet) {
            super.onPostExecute(fontSet);

            // Convert the Set to a List and sort it by FullName
            List<FontDetails> fontList = new ArrayList<>(fontSet);
            Collections.sort(fontList, (font1, font2) -> font1.FullName.compareTo(font2.FullName));

            // Add the sorted fontList to the adapter
            adapter.addAll(fontList);

            Intent intent = new Intent(MainActivity.this, SubmissionStatus.class);
            intent.putExtra("errors", weirdFontLogger);
            intent.putExtra("fonts", (Serializable) fonts);
            startActivity(intent);
        }
    }

}
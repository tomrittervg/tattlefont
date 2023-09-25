package vg.ritter.tattlefont.activities;



import android.content.Intent;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.widget.TextView;

        import androidx.annotation.Nullable;
        import androidx.appcompat.app.AppCompatActivity;

import vg.ritter.tattlefont.R;

public class ResponseScreen extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.response_screen);

        String message = getIntent().getStringExtra("message");

        TextView messageTextView = findViewById(R.id.messageTextView);
        messageTextView.setText(message);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate back to the previous activity
                finish();
            }
        });
    }
}

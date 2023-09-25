package vg.ritter.tattlefont.activities;


        import android.content.Intent;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.TextView;
        import androidx.appcompat.app.AppCompatActivity;

        import vg.ritter.tattlefont.R;
        import vg.ritter.tattlefont.utility.NameManager;
        import vg.ritter.tattlefont.utility.UUIDManager;

public class SplashScreen extends AppCompatActivity {

    private TextView uuidTextView;
    private EditText nameEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        String savedName = NameManager.getSavedName(SplashScreen.this);
        if(savedName != null) {
            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(intent);
            finish(); // Finish the current activity
        }

        uuidTextView = findViewById(R.id.uuidTextView);
        nameEditText = findViewById(R.id.nameEditText);
        saveButton = findViewById(R.id.saveButton);

        // Obtain a UUID from the UUIDManager (You'll need to implement this)
        String uuid = UUIDManager.getUUID(this);
        uuidTextView.setText("UUID: " + uuid);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save the entered name using NameManager (You'll need to implement this)
                String name = nameEditText.getText().toString();
                NameManager.saveName(SplashScreen.this, name);

                // Start the MainActivity
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
                finish(); // Finish the current activity
            }
        });
    }
}

package com.example.cfft;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CompareActivity extends Activity {

    private EditText editText1;
    private EditText editText2;
    private Button compareButton;
    private TextView resultTextView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);

        // Initialize UI components
        editText1 = findViewById(R.id.editText1);
        editText2 = findViewById(R.id.editText2);
        compareButton = findViewById(R.id.compareButton);
        resultTextView = findViewById(R.id.resultTextView);

        // Set onClickListener for the button
        compareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compareData();
            }
        });
    }

    private void compareData() {
        String text1 = editText1.getText().toString();
        String text2 = editText2.getText().toString();

        if (text1.isEmpty() || text2.isEmpty()) {
            Toast.makeText(this, "Please enter data in both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Compare the two texts
        if (text1.equals(text2)) {
            resultTextView.setText("The data is identical.");
        } else {
            resultTextView.setText("The data is different.");
        }
    }
    /**
     * 离线SDK合成
     * @param view
     */
    public void offlineSDK(View view) {
        startActivity(new Intent(this,SynthActivity.class));
    }

}

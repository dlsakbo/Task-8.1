package com.example.chatbot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge content
        EdgeToEdge.enable(this);
        // Set the layout resource for this activity
        setContentView(R.layout.activity_main);

        // Get the EditText view by its ID
        EditText nameInputEditText = findViewById(R.id.editText2);
        // Set an OnClickListener on the button to handle click events
        findViewById(R.id.btn_go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new intent to start the ChatActivity
                Intent chatIntent = new Intent(WelcomeActivity.this, ChatActivity.class);
                // Put the text from the EditText into the intent as an extra
                chatIntent.putExtra("name", nameInputEditText.getText().toString());
                // Start the ChatActivity
                startActivity(chatIntent);
            }
        });
    }
}

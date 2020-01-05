package com.pilates.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PostTrainerRegisterActivity extends AppCompatActivity {
    private final UserRegistry userRegistry = UserRegistry.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_trainer_register);

        final Button startStreamButton = findViewById(R.id.startTraineeStream);

        final TextView nameTV = findViewById(R.id.trainerNameTV);
        final String trainerNamePrefix = nameTV.getText().toString();

        nameTV.setText(trainerNamePrefix + userRegistry.getUser().getName());


        startStreamButton.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
    }
}

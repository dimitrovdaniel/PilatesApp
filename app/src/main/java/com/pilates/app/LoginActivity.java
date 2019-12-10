package com.pilates.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.pilates.app.model.UserRole;
import com.pilates.app.model.UserSession;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    Button loginButton;
    EditText username;
    RadioGroup roleRadioGroup;
    UserRole role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.submitButton);
        username = findViewById(R.id.username);
        roleRadioGroup = findViewById(R.id.role_radio_group);

        roleRadioGroup.setOnCheckedChangeListener((radioGroup, id) -> {
            if (id == R.id.radioButtonTrainee) {
                role = UserRole.TRAINEE;
                final RadioButton radioButtonTrainer = findViewById(R.id.radioButtonTrainer);
                radioButtonTrainer.setChecked(false);
            } else {
                role = UserRole.TRAINER;
                final RadioButton radioButtonTrainee = findViewById(R.id.radioButtonTrainee);
                radioButtonTrainee.setChecked(false);
            }
        });


        loginButton.setOnClickListener(view -> {
            final String username = this.username.getText().toString();

            final UserSession userSession = new UserSession(username, role);

            UserRegistry.getInstance().saveUser(userSession);

            startActivity(new Intent(this, MainActivity.class));
        });
    }

}

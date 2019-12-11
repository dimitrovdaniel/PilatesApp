package com.pilates.app;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.pilates.app.model.Action;
import com.pilates.app.model.ActionBody;
import com.pilates.app.model.ActionType;
import com.pilates.app.model.UserRole;
import com.pilates.app.model.UserSession;
import com.pilates.app.ws.SignalingWebSocket;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.CompletableFuture;

public class LoginActivity extends AppCompatActivity {
    Button loginButton;
    EditText username;
    RadioGroup roleRadioGroup;
    UserRole role;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        CompletableFuture.runAsync(SignalingWebSocket::getInstance);

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
            final int checkedRadioButtonId = roleRadioGroup.getCheckedRadioButtonId();
            final RadioButton radioButton = findViewById(checkedRadioButtonId);
            final String roleText = radioButton.getText().toString();
            final String username = this.username.getText().toString();

            final UserSession userSession = new UserSession(username, role);

            UserRegistry.getInstance().saveUser(userSession);

            final ActionBody body = ActionBody.newBuilder().withName(username).withRole(UserRole.fromString(roleText)).build();
            SignalingWebSocket.getInstance().sendMessage(new Action(ActionType.REGISTER, body));

            if (role == UserRole.TRAINEE) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                startActivity(new Intent(this, TraineesActivity.class));
            }
        });
    }

}

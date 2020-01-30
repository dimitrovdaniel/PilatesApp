package com.pilates.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.pilates.app.model.Action;
import com.pilates.app.model.ActionBody;
import com.pilates.app.model.ActionType;
import com.pilates.app.model.UserRole;
import com.pilates.app.model.UserSession;
import com.pilates.app.model.dto.UserDto;
import com.pilates.app.ws.SignalingWebSocket;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class LoginActivity extends AppCompatActivity {
    private Button loginButton;
    private EditText username;
    private RadioGroup roleRadioGroup;
    private UserRole role = UserRole.TRAINER;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        CompletableFuture.runAsync(SignalingWebSocket::getInstance);



        RequestQueue requestQueue = Volley.newRequestQueue(this);

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

            UserDto dto = UserDto.newBuilder()
                    .withEmail("someEmail@gmail.com")
                    .withPassword("abcd")
                    .withRole(UserRole.fromString(roleText))
                    .withUsername(username).build();




            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    "http://18.203.172.206:8080/streaming/api/v1/user/save",  // aws signaling ip
//                    "http://192.168.33.31:8080/streaming/api/v1/user/save", // local work signaling ip
//                    "http://192.168.100.5:8080/streaming/api/v1/user/save", // local home signaling ip
                    response -> {
                        System.out.println("RESPOOOOOOONSE: " + response);

                        final UserSession userSession = new UserSession(response, username, role);
                        UserRegistry.getInstance().saveUser(userSession);

                        final ActionBody body = ActionBody.newBuilder().withInfoId(response).withName(username).withRole(UserRole.fromString(roleText)).build();
                        SignalingWebSocket.getInstance().sendMessage(new Action(ActionType.REGISTER, body));
                        runOnUiThread(() -> {
                            System.out.println("USER ROLE: " + role);
                            if (Objects.equals(role, UserRole.TRAINER)) {
                                hideKeyboard(getActivity());
                                startActivity(new Intent(this, MainActivity.class));
                            } else {
                                startActivity(new Intent(this, PostTraineeRegisterActivity.class));
                            }
                        });
                    },
                    error -> {
                        error.printStackTrace();
                        System.out.println("ERROR ON HTTP" + error.getMessage());
                    }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    String s = dto.toString();
                    System.out.println("SENDING REQUEST: " + s);
                    return s.getBytes(StandardCharsets.UTF_8);
                }
            };

            requestQueue.add(stringRequest);


        });


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

    }

    private Activity getActivity() {
        return this;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}

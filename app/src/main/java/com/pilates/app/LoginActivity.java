package com.pilates.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.pilates.app.model.HttpRequest;
import com.pilates.app.model.UserSession;
import com.pilates.app.model.dto.UserDto;
import com.pilates.app.model.dto.UserInfoDto;
import com.pilates.app.util.DefaultOperations;
import com.pilates.app.ws.SignalingWebSocket;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class LoginActivity extends AppCompatActivity implements
        AdapterView.OnClickListener,
        Response.Listener<String>, Response.ErrorListener {
    private final UserRegistry registry = UserRegistry.getInstance();
    private final Gson jsonConverter = new Gson();
    private RequestQueue requestQueue;
    private UserDto dto;

    private Button loginButton;
    private EditText etEmail;
    private EditText etPassword;
    private TextView tvSignUp;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        CompletableFuture.runAsync(SignalingWebSocket::getInstance);

        requestQueue = Volley.newRequestQueue(this);
        loginButton = findViewById(R.id.loginBtn);
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        tvSignUp = findViewById(R.id.sign_up);

        tvSignUp.setOnClickListener(v -> startActivity(new Intent(this, RegistrationActivity.class)));


        loginButton.setOnClickListener(this);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

    }

    private boolean validate(final String email, final String password) {
        if (Objects.isNull(email) || Objects.equals(email, "")) {
            Toast.makeText(LoginActivity.this, "Email required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (Objects.isNull(password) || Objects.equals(password, "")) {
            Toast.makeText(LoginActivity.this, "Password required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {

        final String email = this.etEmail.getText().toString();
        final String password = this.etPassword.getText().toString();

        if (validate(email, password)) {

            dto = UserDto.newBuilder()
                    .withEmail(email)
                    .withPassword(password)
                    .build();

            registry.saveDto(dto);

//            "http://18.203.172.206:8081/provider/api/v1/user/login",  // aws signaling ip
//            "http://192.168.33.31:8081/provider/api/v1/user/login", // local work signaling ip
//            "http://192.168.100.5:8081/provider/api/v1/user/login", // local home signaling ip
//            "http://192.168.99.1:8081/provider/api/v1/user/login", // local home signaling ip

            HttpRequest httpRequest = new HttpRequest(Request.Method.POST,
                    "http://18.203.172.206:8081/provider/api/v1/user/login",
                    this, this, dto);

            requestQueue.add(httpRequest);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        error.printStackTrace();
        System.out.println("ERROR WHILE LOGIN" + error.getMessage());
    }

    @Override
    public void onResponse(final String infoString) {
        final UserInfoDto infoDto = jsonConverter.fromJson(infoString, UserInfoDto.class);
        final UserSession userSession = new UserSession(infoDto.getId(), infoDto.getName(), infoDto.getRole());
        registry.saveUser(userSession);
        DefaultOperations.loginRegisterFlow(this);
    }
}

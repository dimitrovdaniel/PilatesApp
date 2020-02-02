package com.pilates.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.pilates.app.model.HttpRequest;
import com.pilates.app.model.UserRole;
import com.pilates.app.model.UserSession;
import com.pilates.app.model.dto.UserDto;
import com.pilates.app.util.DefaultOperations;

public class RegistrationActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        AdapterView.OnClickListener,
        Response.Listener<String>, Response.ErrorListener {


    private final UserRegistry registry = UserRegistry.getInstance();
    private RequestQueue requestQueue;
    private UserDto dto;

    private EditText etEmail;
    private EditText etUsername;
    private EditText etPassword;
    private Spinner roleSpinner;
    private Button regButton;
    private TextView signIn;
    private UserRole role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        requestQueue = Volley.newRequestQueue(this);
        etEmail = findViewById(R.id.singUpEmail);
        etUsername = findViewById(R.id.singUpUsername);
        etPassword = findViewById(R.id.signUpPassword);
        roleSpinner = findViewById(R.id.signUpRole);
        regButton = findViewById(R.id.registerBtn);
        signIn = findViewById(R.id.sign_in);

        roleSpinner.setOnItemSelectedListener(this);

        regButton.setOnClickListener(this);

        signIn.setOnClickListener(view -> startActivity(new Intent(this, LoginActivity.class)));


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selected = parent.getSelectedItem().toString();
        System.out.println("Selected: " + selected);
        role = UserRole.fromString(selected);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        role = UserRole.TRAINER;
    }

    @Override
    public void onClick(View v) {
        final String username = etUsername.getText().toString();
        final String email = etEmail.getText().toString();
        final String password = etPassword.getText().toString();


        dto = UserDto.newBuilder()
                .withEmail(email)
                .withPassword(password)
                .withRole(role)
                .withUsername(username)
                .build();


        registry.saveDto(dto);

//        "http://18.203.172.206:8080/streaming/api/v1/user/save",  // aws signaling ip
//        "http://192.168.33.31:8080/streaming/api/v1/user/save", // local work signaling ip
//        "http://192.168.100.5:8081/provider/api/v1/user/register", // local home signaling ip
//        "http://192.168.99.1:8081/provider/api/v1/user/register", // local home signaling ip

        final HttpRequest httpRequest = new HttpRequest(Request.Method.POST,
                "http://18.203.172.206:8081/provider/api/v1/user/register",
                this, this, dto);

        requestQueue.add(httpRequest);
        System.out.println(username + " " + email + " " + password + " " + role);
    }

    @Override
    public void onErrorResponse(final VolleyError error) {
        error.printStackTrace();
        System.out.println("ERROR WHILE REGISTERING" + error.getMessage());
    }

    @Override
    public void onResponse(final String infoID) {
        final UserDto dto = registry.getDto();
        final UserSession userSession = new UserSession(infoID, dto.getUsername(), dto.getRole());
        registry.saveUser(userSession);
        DefaultOperations.loginRegisterFlow(this);
    }
}

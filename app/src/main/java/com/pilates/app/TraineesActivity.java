package com.pilates.app;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.pilates.app.listeners.DataChangedListener;
import com.pilates.app.model.Action;
import com.pilates.app.model.ActionBody;
import com.pilates.app.model.ActionType;
import com.pilates.app.model.UserSession;
import com.pilates.app.ws.SignalingWebSocket;

import java.util.List;

public class TraineesActivity extends AppCompatActivity {

    private final UserRegistry userRegistry = UserRegistry.getInstance();
    private final List<String> traineeNames = userRegistry.getTraineeNames();
    private String traineeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainees);
        final ListView traineesList = findViewById(R.id.traineesList);
        final Button callButton = findViewById(R.id.callToTraineeButton);

        final ArrayAdapter<String> adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, traineeNames);

        userRegistry.setListener(new DataChangedListener() {
            @Override
            public void changed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
        userRegistry.setAdapter(adapter);
        traineesList.setAdapter(adapter);

        traineesList.setOnItemClickListener((parent, view, position, id) -> {
            traineeName = traineeNames.get(position);
            System.out.println("Trainee name: " + traineeName);
        });

        callButton.setOnClickListener(v -> {

            if (traineeName == null) {
                throw new RuntimeException("No trainee name");
            }

            final UserSession user = UserRegistry.getInstance().getUser();
            user.setCalleeName(traineeName);
//            final ActionBody body = ActionBody.newBuilder().withName(traineeName).build();
//            SignalingWebSocket.getInstance().sendMessage(new Action(ActionType.CONNECT_TO, body));

            startActivity(new Intent(this, MainActivity.class));
        });

    }
}

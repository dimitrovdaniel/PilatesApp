package com.pilates.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.pilates.app.model.UserSession;
import com.pilates.app.model.dto.UserItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class PostTraineeRegisterActivity extends AppCompatActivity {

    private final UserRegistry userRegistry = UserRegistry.getInstance();
    private UserItem trainerItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_trainee_register);


        final ListView trainersList = findViewById(R.id.trainersList);
        final Button callButton = findViewById(R.id.callToTraineeButton);

        final TextView nameTV = findViewById(R.id.traineeNameTV);
        final String trainerNamePrefix = nameTV.getText().toString();

        nameTV.setText(trainerNamePrefix + userRegistry.getUser().getName());

        final ArrayAdapter<UserItem> adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, userRegistry.getTrainerItems());


        userRegistry.setHandler(new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                System.out.println("[TRAINEE LISTENER] called pre runonuithread");

                UserItem value = (UserItem) msg.obj;
                adapter.add(value);
                adapter.notifyDataSetChanged();
                trainersList.invalidateViews();

                System.out.println("[TRAINEE LISTENER] called for change");
            }
        });

        trainersList.setAdapter(adapter);

        trainersList.setOnItemClickListener((parent, view, position, id) -> {
            trainerItem = adapter.getItem(position);
            System.out.println("Trainee name: " + trainerItem);


            for (int i = 0; i < trainersList.getChildCount(); i++) {
                if(position == i ){
                    trainersList.getChildAt(i).setBackgroundColor(Color.BLUE);
                }else{
                    trainersList.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
                }
            }
        });

        callButton.setOnClickListener(v -> {

            if (trainerItem == null) {
                throw new RuntimeException("No trainer name");
            }

            final UserSession user = UserRegistry.getInstance().getUser();
            user.setConnectorName(trainerItem.getName());
            user.setConnectorId(trainerItem.getInfoId());

            startActivity(new Intent(this, MainActivity.class));
        });

    }
}

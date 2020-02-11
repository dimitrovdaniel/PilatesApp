package com.pilates.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.pilates.app.model.Action;
import com.pilates.app.model.ActionType;
import com.pilates.app.service.PeerConnectionClient;
import com.pilates.app.ws.SignalingWebSocket;

public class StartClassActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_class);

        PeerConnectionClient peerConnectionClient = PeerConnectionClient.getInstance();
        peerConnectionClient.initPeerConnectionFactory(this);
        peerConnectionClient.initPeerConnection(peerConnectionClient.initLocalMediaStream());

        findViewById(R.id.start_class_btn).setOnClickListener(v -> {
            SignalingWebSocket.getInstance().sendMessage(new Action(ActionType.INIT_TRAINER));
            startActivity(new Intent(this, MainActivity.class));
        });
    }
}

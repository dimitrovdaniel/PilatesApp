package com.pilates.app.adapter;

import android.util.Log;

import com.pilates.app.UserRegistry;
import com.pilates.app.model.Action;
import com.pilates.app.model.ActionBody;
import com.pilates.app.model.ActionType;
import com.pilates.app.model.UserRole;
import com.pilates.app.model.UserSession;
import com.pilates.app.ws.SignalingWebSocket;

import org.webrtc.PeerConnection;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.util.Objects;

/**
 * Created by chao on 2019/1/29.
 */

public class SdpAdapter implements SdpObserver {


    private static SdpAdapter instance = new SdpAdapter();
    private org.webrtc.PeerConnection peerConnection;
    private String tag;


    public SdpAdapter() {
        this.tag = "SDP ADAPTER ";
    }

    public static SdpAdapter getInstance() {
        if (instance == null) {
            instance = new SdpAdapter();
        }

        return instance;
    }

    private void log(String s) {
        Log.d(tag, s);
    }

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        log(sessionDescription.description);


        System.out.println("OFFER CREATED: " + sessionDescription.description);

        peerConnection.setLocalDescription(this, sessionDescription);
        final String description = sessionDescription.description;
        final UserSession user = UserRegistry.getInstance().getUser();
        final String trainerName = user.getTrainerName();

        final ActionBody body = ActionBody.newBuilder().withOffer(description).build();
        final Action action = new Action(ActionType.OFFER, body);

        SignalingWebSocket.getInstance().sendMessage(action);

        UserRole role = user.getRole();
        if(Objects.equals(role, UserRole.TRAINER)) {
            SignalingWebSocket.getInstance().sendMessage(new Action(ActionType.INIT_TRAINER));

        } else if (Objects.equals(role, UserRole.TRAINEE)) {
            final ActionBody traineeBody = ActionBody.newBuilder().withName(trainerName).build();
            SignalingWebSocket.getInstance().sendMessage(new Action(ActionType.CONNECT_TO, traineeBody));
        }
    }

    @Override
    public void onSetSuccess() {
        log("onSetSuccess ");
    }

    @Override
    public void onCreateFailure(String s) {
        log("onCreateFailure " + s);
    }

    @Override
    public void onSetFailure(String s) {
        log("onSetFailure " + s);
    }

    public void setPeerConnection(PeerConnection peerConnection) {
        this.peerConnection = peerConnection;
    }

}

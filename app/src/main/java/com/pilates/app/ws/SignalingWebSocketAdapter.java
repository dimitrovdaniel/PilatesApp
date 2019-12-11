package com.pilates.app.ws;

import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.pilates.app.SdpAdapter;
import com.pilates.app.UserRegistry;
import com.pilates.app.model.Action;
import com.pilates.app.model.ActionBody;
import com.pilates.app.model.ActionType;
import com.pilates.app.model.Candidate;

import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SignalingWebSocketAdapter extends WebSocketAdapter {

    private static SignalingWebSocketAdapter instance = new SignalingWebSocketAdapter();
    private final UserRegistry userRegistry = UserRegistry.getInstance();

    private PeerConnection peerConnection;

    private SignalingWebSocketAdapter() { }

    public static SignalingWebSocketAdapter getInstance() {
        if (instance == null) {
            instance = new SignalingWebSocketAdapter();
        }

        return instance;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        System.out.println("Connected");
//        final ActionBody android = ActionBody.newBuilder().withName("Android").withRole(UserRole.TRAINER).build();
//        final Action action = new Action(ActionType.REGISTER, android);
//
//        System.out.println("SENDING MESSAGE: " + action.toString());
//        websocket.sendText(action.toString());

    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
        System.out.println("Connection error");
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        System.out.println("RECEIVED: " + text);

        Gson gson = new Gson();
        final Action action = gson.fromJson(text, Action.class);
        final ActionType type = action.getType();
        final ActionBody body = action.getBody();

        System.out.println("Converted: " + action.toString());
//        JSONObject jsonObject = new JSONObject(text);
//        final JSONObject body = jsonObject.getJSONObject("body");
//        final String type = jsonObject.getString("type");


        if (Objects.equals(type, ActionType.ICE_EXCHANGE)) {
            Candidate candidate = body.getCandidate();
            getPeerConnection().addIceCandidate(new IceCandidate(candidate.getSdpMid(), candidate.getSdpMLineIndex(), candidate.getCandidate()));

        } else if (Objects.equals(type, ActionType.ANSWER)) {
            getPeerConnection().setRemoteDescription(new SdpAdapter("localSetRemote"),
                    new SessionDescription(SessionDescription.Type.ANSWER, body.getAnswer()));

        } else if (Objects.equals(type, ActionType.TRAINEES)) {
            final Map<String, String> trainees = body.getRegisteredUsers();
            userRegistry.putAllTrainees(trainees);
        } else if (Objects.equals(type, ActionType.ADD_TRAINEE)) {
            final String id = body.getId();
            final String name = body.getName();
            userRegistry.putTrainee(id, name);
        }
//        System.out.println("AFTER MAPPING TYPE : " + type);
//        System.out.println("AFTER MAPPING BODY : " + body);
//        JSONObject registeredUsers = body.getJSONObject("registeredUsers");
//        System.out.println("AFTER MAPPING USERS: " + registeredUsers);
//
//
//        Iterator<String> keys = registeredUsers.keys();
//        while (keys.hasNext()) {
//            String next = keys.next();
//            System.out.println("KEY: " + next);
//            System.out.println("VALUE: " + registeredUsers.getString(next));
//        }

    }


    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        System.out.println("Disconnected");
    }


    public void setPeerConnection(final PeerConnection peerConnection) {
        this.peerConnection = peerConnection;
    }

    private PeerConnection getPeerConnection() {
        if (this.peerConnection == null) {
            throw new RuntimeException("Peer connection can not be null");
        }

        return this.peerConnection;
    }

}

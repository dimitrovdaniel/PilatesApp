package com.pilates.app.ws;

import android.os.Build;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;
import com.pilates.app.handler.SdpAdapter;
import com.pilates.app.UserRegistry;
import com.pilates.app.model.Action;
import com.pilates.app.model.ActionBody;
import com.pilates.app.model.ActionType;
import com.pilates.app.model.Candidate;
import com.pilates.app.model.ClassInitData;
import com.pilates.app.model.UserSession;

import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.RequiresApi;

import static com.pilates.app.util.Constant.HandlerMessage.CLASS_INITIALIZED;
import static com.pilates.app.util.Constant.HandlerMessage.HANDLE_CONNECTION_ESTABLISHED;
import static com.pilates.app.util.Constant.HandlerMessage.HANDLE_ON_HOLD;
import static com.pilates.app.util.Constant.HandlerMessage.HANDLE_SWITCHED;
import static com.pilates.app.util.Constant.HandlerMessage.HANDLE_TRAINEE_LEAVED;

public class SignalingWebSocketAdapter extends WebSocketAdapter {

    private static SignalingWebSocketAdapter instance = new SignalingWebSocketAdapter();
    private final UserRegistry userRegistry = UserRegistry.getInstance();
    private final Gson gson = new Gson();
    private final AtomicBoolean failedConnection = new AtomicBoolean(false);

    private PeerConnection peerConnection;
    private Handler mainUIHandler;

    private SignalingWebSocketAdapter() {
    }

    public static SignalingWebSocketAdapter getInstance() {
        if (instance == null) {
            instance = new SignalingWebSocketAdapter();
        }

        return instance;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        System.out.println("Connected");
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
        System.out.println("Connection error");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        System.out.println("RECEIVED: " + text);

        final Action action = gson.fromJson(text, Action.class);
        final ActionType type = action.getType();
        final ActionBody body = action.getBody();

        System.out.println("Converted: " + action.toString());

        if (Objects.equals(type, ActionType.ICE_EXCHANGE)) {

            Candidate candidate = body.getCandidate();
            IceCandidate ice = new IceCandidate(candidate.getSdpMid(), candidate.getSdpMLineIndex(), candidate.getCandidate());
            getPeerConnection().addIceCandidate(ice);

            UserSession user = userRegistry.getUser();

            user.addRemoteCandidate(ice);

        } else if (Objects.equals(type, ActionType.ANSWER)) {
            SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER, body.getAnswer());
            SdpAdapter instance = SdpAdapter.getInstance();
            getPeerConnection().setRemoteDescription(instance, sdp);
            UserSession user = userRegistry.getUser();
            user.setAnswer(sdp);

        } else if (Objects.equals(type, ActionType.INITIALIZED)) {
            final LocalDateTime startTime = body.getStartTime();
            final LocalDateTime endTime = body.getEndTime();
            final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

            ClassInitData data = new ClassInitData();
            data.currentSeconds = Duration.between(now, endTime).get(ChronoUnit.SECONDS);
            data.totalSeconds = Duration.between(startTime, endTime).get(ChronoUnit.SECONDS);

            final Message message = mainUIHandler.obtainMessage(CLASS_INITIALIZED, data);
            mainUIHandler.sendMessage(message);
        } else if (Objects.equals(type, ActionType.TRAINERS)) {
            final Map<String, String> trainees = body.getTrainers();
            userRegistry.putAllTrainers(trainees);

        } else if (Objects.equals(type, ActionType.ADD_TRAINER)) {
            final String id = body.getInfoId();
            final String name = body.getName();
            userRegistry.putTrainer(id, name);

        } else if (Objects.equals(type, ActionType.REMOVE_TRAINER)) {
            final String id = body.getInfoId();
            userRegistry.removeTrainer(id);

        } else if (Objects.equals(type, ActionType.ADD_TRAINEE)) {
            final String id = body.getId();
            final String name = body.getName();
            userRegistry.addTrainee(id, name);

        } else if (Objects.equals(type, ActionType.REMOVE_TRAINEE)) {
            final String id = body.getId();
            userRegistry.removeTrainee(id);

        } else if (Objects.equals(type, ActionType.CALL_IN_PROGRESS)) {

            final String connectorId = body.getInfoId();
            final String connectorName = body.getName();
            final UserSession user = userRegistry.getUser();

            final LocalDateTime startTime = body.getStartTime();
            final LocalDateTime endTime = body.getEndTime();

            final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            final Message message = mainUIHandler.obtainMessage(HANDLE_CONNECTION_ESTABLISHED, Duration.between(now, endTime).get(ChronoUnit.MILLIS));

            user.setConnectorId(connectorId);
            user.setConnectorName(connectorName);

            mainUIHandler.sendMessage(message);

        } else if (Objects.equals(type, ActionType.TRAINEE_LEAVED)) {
            mainUIHandler.sendEmptyMessage(HANDLE_TRAINEE_LEAVED);

        } else if (Objects.equals(type, ActionType.ON_HOLD)) {
            mainUIHandler.sendEmptyMessage(HANDLE_ON_HOLD);

        } else if (Objects.equals(type, ActionType.SWITCHED)) {
            mainUIHandler.sendEmptyMessage(HANDLE_SWITCHED);

        } else if (Objects.equals(type, ActionType.PING)) {
            SignalingWebSocket.getInstance().sendMessage(new Action(ActionType.PONG, LocalDateTime.now(ZoneOffset.UTC)));
        }

    }


    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        System.out.println("WS WS WS WS Disconnected");
    }


    public void setPeerConnection(final PeerConnection peerConnection) {
        System.out.println("SETTING NEW PEER CONNECTION");
        this.peerConnection = peerConnection;
    }

    private PeerConnection getPeerConnection() {
        if (this.peerConnection == null) {
            throw new RuntimeException("Peer connection can not be null");
        }

        return this.peerConnection;
    }

    @Override
    public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
        if (Objects.equals(newState, WebSocketState.CLOSED)) {
            failedConnection.set(true);
        } else if (Objects.equals(newState, WebSocketState.OPEN)) {
            connectionEstablished();
        }
    }

    public boolean isConnectionFailed() {
        return failedConnection.get();
    }

    public void connectionEstablished() {
        failedConnection.set(false);
    }

    public void setMainUIHandler(Handler mainUIHandler) {
        this.mainUIHandler = mainUIHandler;
    }
}

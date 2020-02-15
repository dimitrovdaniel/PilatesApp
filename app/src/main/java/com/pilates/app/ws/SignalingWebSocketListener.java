package com.pilates.app.ws;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;
import com.pilates.app.model.MediaStats;
import com.pilates.app.service.PeerConnectionClient;
import com.pilates.app.UserRegistry;
import com.pilates.app.model.Action;
import com.pilates.app.model.ActionBody;
import com.pilates.app.model.ActionType;
import com.pilates.app.model.Candidate;
import com.pilates.app.model.ClassInitData;
import com.pilates.app.model.UserSession;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.RequiresApi;

import static com.pilates.app.util.Constant.HandlerMessage.CLASS_INITIALIZED;
import static com.pilates.app.util.Constant.HandlerMessage.HANDLE_CONNECTION_ESTABLISHED;
import static com.pilates.app.util.Constant.HandlerMessage.HANDLE_MEDIA_STATS;
import static com.pilates.app.util.Constant.HandlerMessage.HANDLE_ON_HOLD;
import static com.pilates.app.util.Constant.HandlerMessage.HANDLE_SWITCHED;
import static com.pilates.app.util.Constant.HandlerMessage.HANDLE_TRAINEE_LEAVED;

public class SignalingWebSocketListener extends WebSocketAdapter {

    private static SignalingWebSocketListener instance = new SignalingWebSocketListener();
    private PeerConnectionClient peerConnectionClient = PeerConnectionClient.getInstance();
    private final UserRegistry userRegistry = UserRegistry.getInstance();
    private final Gson gson = new Gson();
    private final AtomicBoolean failedConnection = new AtomicBoolean(true);

//    private PeerConnectionClient peerConnection;
    private Handler mainUIHandler;

    private SignalingWebSocketListener() {
    }

    public static SignalingWebSocketListener getInstance() {
        if (instance == null) {
            instance = new SignalingWebSocketListener();
        }

        return instance;
    }

    private void log(String s) {
        Log.i("[WS] ", s);
    }


    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        log("Web socket connection established");
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
        log("Web socket connection error " + exception.getMessage());

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        log("Received message " + text);

        final Action action = gson.fromJson(text, Action.class);
        final ActionType type = action.getType();
        final ActionBody body = action.getBody();

        switch (type) {
            case ICE_EXCHANGE:

                final Candidate candidate = body.getCandidate();
                peerConnectionClient.addIceCandidate(candidate);
                break;

            case ANSWER:

                final String answer = body.getAnswer();
                peerConnectionClient.setRemoteDescription(answer);
                break;

            case INITIALIZED:
                final LocalDateTime classStartTime = body.getStartTime();
                final LocalDateTime classEndTime = body.getEndTime();
                final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

                final ClassInitData data = new ClassInitData();
                data.currentSeconds = Duration.between(now, classEndTime).get(ChronoUnit.SECONDS);
                data.totalSeconds = Duration.between(classStartTime, classEndTime).get(ChronoUnit.SECONDS);

                final Message classInitializedMessage = mainUIHandler.obtainMessage(CLASS_INITIALIZED, data);
                mainUIHandler.sendMessage(classInitializedMessage);
                break;

            case TRAINERS:
                final Map<String, String> trainees = body.getTrainers();
                userRegistry.putAllTrainers(trainees);
                break;

            case ADD_TRAINER:
                final String trainerId = body.getInfoId();
                final String trainerName = body.getName();
                userRegistry.putTrainer(trainerId, trainerName);
                break;

            case REMOVE_TRAINER:
                userRegistry.removeTrainer(body.getInfoId());
                break;

            case ADD_TRAINEE:
                final String traineeId = body.getInfoId();
                final String traineeName = body.getName();
                userRegistry.addTrainee(traineeId, traineeName);
                break;

            case REMOVE_TRAINEE:
                userRegistry.removeTrainee(body.getInfoId());
                break;

            case CALL_IN_PROGRESS:
                final String connectorId = body.getInfoId();
                final String connectorName = body.getName();
                final UserSession user = userRegistry.getUser();

                final LocalDateTime callStartTime = body.getStartTime();
                final LocalDateTime callShouldEndTime = body.getEndTime();

                final LocalDateTime currentTime = LocalDateTime.now(ZoneOffset.UTC);
                final Message handleConnectionEstablishedMessage = mainUIHandler.obtainMessage(HANDLE_CONNECTION_ESTABLISHED,
                        Duration.between(currentTime, callShouldEndTime).get(ChronoUnit.MILLIS));

                user.setConnectorId(connectorId);
                user.setConnectorName(connectorName);

                mainUIHandler.sendMessage(handleConnectionEstablishedMessage);
                break;

            case TRAINEE_LEAVED:
                mainUIHandler.sendEmptyMessage(HANDLE_TRAINEE_LEAVED);
                break;

            case ON_HOLD:
                mainUIHandler.sendEmptyMessage(HANDLE_ON_HOLD);
                break;
            case SWITCHED:
                mainUIHandler.sendEmptyMessage(HANDLE_SWITCHED);
                break;
            case STATS:
                final MediaStats mediaStats = body.getMediaStats();
                final Message mediaStatsMessage = mainUIHandler.obtainMessage(HANDLE_MEDIA_STATS, mediaStats);
                mainUIHandler.sendMessage(mediaStatsMessage);
                break;
            default:
                 log("No such action " + action);
                 break;

        }

    }


    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        log("Web socket disconnected");
    }


    @Override
    public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
        if (Objects.equals(newState, WebSocketState.CLOSED)) {
            failedConnection.set(true);
        } else if (Objects.equals(newState, WebSocketState.OPEN)) {
            failedConnection.set(false);
        }
    }

    public boolean isConnectionFailed() {
        return failedConnection.get();
    }

    public void setMainUIHandler(Handler mainUIHandler) {
        this.mainUIHandler = mainUIHandler;
    }
}

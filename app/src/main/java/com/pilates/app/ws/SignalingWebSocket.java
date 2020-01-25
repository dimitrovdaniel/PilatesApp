package com.pilates.app.ws;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketState;
import com.pilates.app.model.Action;
import com.pilates.app.model.ActionType;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SignalingWebSocket {
    private static SignalingWebSocket instance = new SignalingWebSocket();

    private final WebSocketFactory factory = new WebSocketFactory();
    private AtomicBoolean connected = new AtomicBoolean(false);
    private WebSocket ws;

    private SignalingWebSocket() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            System.out.println("HEYYYY IM THREAD WHICH WORKS EVERY 5 sec");
            if (connected.get()) {
                System.out.println("SENDING MESSAGE");
//                sendMessage(new Action(ActionType.PING));
            } else {
                if (Objects.nonNull(ws)) {
                    WebSocketState state = ws.getState();
                    System.out.println("WS STATE: " + state);
                }
                System.out.println("NOT CONNECTED");
//
//                initWs();
            }

        }, 0, 5, TimeUnit.SECONDS);

        initWs();

    }

    private void initWs() {
        try {
//            ws = factory.createSocket("ws://18.203.172.206:8080/streaming/callone"); // aws signaling ip
            ws = factory.createSocket("ws://192.168.33.31:8080/streaming/callone");  // local
            ws.addListener(SignalingWebSocketAdapter.getInstance());
            ws = ws.connect();
            connected.set(true);
        } catch (Exception e) {
            System.out.println("Could not establish web socket connection: " + e.getMessage());
        }
    }


    public static SignalingWebSocket getInstance() {
        if (instance == null) {
            instance = new SignalingWebSocket();
        }

        return instance;
    }

    public void sendMessage(final Action action) {
        System.out.println("SENDING MESSAGE: " + action.toString());
        ws.sendText(action.toString());
    }

}

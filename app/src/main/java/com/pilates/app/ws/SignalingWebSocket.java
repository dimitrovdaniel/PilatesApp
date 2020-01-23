package com.pilates.app.ws;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.pilates.app.model.Action;

public class SignalingWebSocket {
    private static SignalingWebSocket instance = new SignalingWebSocket();

    private final WebSocketFactory factory = new WebSocketFactory();
    private WebSocket ws;

    private SignalingWebSocket() {
        try {
            ws = factory.createSocket("ws://18.203.172.206:8080/streaming/callone");
//            ws = factory.createSocket("ws://192.168.33.31:8080/streaming/callone");
            ws.addListener(SignalingWebSocketAdapter.getInstance());
            ws = ws.connect();
        } catch (Exception e) {
            throw new RuntimeException("Could not establish web socket connection", e);
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

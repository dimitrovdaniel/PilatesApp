package com.pilates.app;

import android.content.Context;
import android.os.Handler;

import com.pilates.app.adapter.PeerConnectionAdapter;
import com.pilates.app.adapter.SdpAdapter;
import com.pilates.app.ws.SignalingWebSocketAdapter;

import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;

import java.util.ArrayList;
import java.util.List;

public class PeerConnection {

    private static PeerConnection instance = new PeerConnection();
    private final PeerConnectionAdapter peerConnectionAdapter = PeerConnectionAdapter.getInstance();
    private final SignalingWebSocketAdapter signalingWebSocketAdapter = SignalingWebSocketAdapter.getInstance();
    private final SdpAdapter sdpAdapter = SdpAdapter.getInstance();
    private PeerConnectionFactory peerConnectionFactory;
    private org.webrtc.PeerConnection peerConnection;
    private Handler handler;
    private boolean init = false;

    private PeerConnection() {

    }


    public static PeerConnection getInstance() {
        if (instance == null) {
            instance = new PeerConnection();
        }

        return instance;
    }

    //1
    public void initPeerConnectionFactory(final EglBase.Context eglBaseContext, final Context applicationContext, final Handler handler) {


        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions
                .builder(applicationContext)
                .createInitializationOptions());

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();


        DefaultVideoEncoderFactory defaultVideoEncoderFactory =
                new DefaultVideoEncoderFactory(eglBaseContext, true, true);
        DefaultVideoDecoderFactory defaultVideoDecoderFactory =
                new DefaultVideoDecoderFactory(eglBaseContext);

        this.peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(defaultVideoEncoderFactory)
                .setVideoDecoderFactory(defaultVideoDecoderFactory)
                .createPeerConnectionFactory();


        this.handler = handler;
//        }
    }


    //2
    public void createPeerConnection(final MediaStream mediaStream) {

        final List<org.webrtc.PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(org.webrtc.PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());
        peerConnectionAdapter.setHandler(handler);

        this.peerConnection = peerConnectionFactory.createPeerConnection(iceServers, peerConnectionAdapter);

        signalingWebSocketAdapter.setPeerConnection(peerConnection);
        signalingWebSocketAdapter.setMainUIHandler(handler);

        this.peerConnection.addStream(mediaStream);

        sdpAdapter.setPeerConnection(this.peerConnection);

        MediaConstraints constraints = new MediaConstraints();
        constraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        constraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        this.peerConnection.createOffer(sdpAdapter, constraints);



    }


    public PeerConnectionFactory getPeerConnectionFactory() {
        return peerConnectionFactory;
    }


}

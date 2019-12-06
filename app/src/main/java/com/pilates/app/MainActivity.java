package com.pilates.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.pilates.app.model.Action;
import com.pilates.app.model.ActionBody;
import com.pilates.app.model.ActionType;
import com.pilates.app.model.Candidate;
import com.pilates.app.model.UserRole;
import com.pilates.app.model.UserSession;

import org.webrtc.Camera1Enumerator;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity {
    WebSocketFactory factory = new WebSocketFactory();
    UserSession userSession = new UserSession("ANDROID", UserRole.NO_ROLE);
    WSAdapter adapter = new WSAdapter();
    WebSocket ws;

    PeerConnectionFactory peerConnectionFactory;
    PeerConnection peerConnection;
    SurfaceViewRenderer localView;
    SurfaceViewRenderer remoteView;
    MediaStream mediaStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        EglBase.Context eglBaseContext = EglBase.create().getEglBaseContext();

        // create PeerConnectionFactory
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions
                .builder(this)
                .createInitializationOptions());
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        DefaultVideoEncoderFactory defaultVideoEncoderFactory =
                new DefaultVideoEncoderFactory(eglBaseContext, true, true);
        DefaultVideoDecoderFactory defaultVideoDecoderFactory =
                new DefaultVideoDecoderFactory(eglBaseContext);
        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(defaultVideoEncoderFactory)
                .setVideoDecoderFactory(defaultVideoDecoderFactory)
                .createPeerConnectionFactory();

        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext);
        // create VideoCapturer
        VideoCapturer videoCapturer = createCameraCapturer(true);
        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(surfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());
        videoCapturer.startCapture(480, 640, 30);

        localView = findViewById(R.id.svLocalView);
        localView.setMirror(true);
        localView.init(eglBaseContext, null);

        // create VideoTrack
        VideoTrack videoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);
//        // display in localView
        videoTrack.addSink(localView);


        remoteView = findViewById(R.id.svRemoteView);
        remoteView.setMirror(false);
        remoteView.init(eglBaseContext, null);


        mediaStream = peerConnectionFactory.createLocalMediaStream("mediaStreamLocal");
        mediaStream.addTrack(videoTrack);

        adapter.setUserSession(userSession);

        call();

    }


    private void call() {

        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());
        peerConnection = peerConnectionFactory.createPeerConnection(iceServers, new PeerConnectionAdapter("localconnection") {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                System.out.println("SENDING ICE: " + iceCandidate);

                final Candidate candidate = Candidate.newBuilder()
                        .withCandidate(iceCandidate.sdp)
                        .withSdpMid(iceCandidate.sdpMid)
                        .withSdpMLineIndex(iceCandidate.sdpMLineIndex).build();
                final ActionBody body = ActionBody.newBuilder().withIceCandidate(candidate).build();


                ws.sendText(new Action(ActionType.ICE_EXCHANGE, body).toString());
//                userSession.addLocalCandidate(candidate);

//                adapter.sendText(new Action(ActionType.ICE_EXCHANGE, body));
                //send ice to signaling
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                System.out.println("STREAM TO LOCAL VIEW ADDED");
                VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
                runOnUiThread(() -> remoteVideoTrack.addSink(remoteView));
            }
        });

        peerConnection.addStream(mediaStream);


        peerConnection.createOffer(new SdpAdapter("local offer sdp") {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                ws = createWs("ws://192.168.100.5:8080/streaming/calln");
                adapter.setPeerConnection(peerConnection);
                System.out.println("OFFER CREATED");
                peerConnection.setLocalDescription(new SdpAdapter("local set local"), sessionDescription);
                final String description = sessionDescription.description;
                userSession.setOffer(description);

                final ActionBody body = ActionBody.newBuilder().withName("PC").withOffer(description).build();
                final Action action = new Action(ActionType.CONNECT_TO, body);

                ws.sendText(action.toString());
            }
        }, new MediaConstraints());


    }

    private WebSocket createWs(final String url) {
        try {
            System.out.println("WEB SOCKET ESTABLISHING by url: " + url);
            ws = factory.createSocket(url);

            ws.addListener(adapter);
            System.out.println("WEB SOCKET ESTABLISHED");
            return ws.connect();
        } catch (IOException e) {
            System.out.println("ERROR");
            System.out.println(e.toString());
        } catch (WebSocketException e) {
            e.printStackTrace();
        }
        throw new RuntimeException();
    }

    //todo setRemote description when answer get   runOnUiThread(() -> {
    // peerConnection.setRemoteDescription(new SdpAdapter("localSetRemote"),
    //                new SessionDescription(SessionDescription.Type.ANSWER, data.optString("sdp")));


    //todo ice
    // peerConnection.addIceCandidate(new IceCandidate(
    //                data.optString("id"),
    //                data.optInt("label"),
    //                data.optString("candidate")
    //        ));

    private VideoCapturer createCameraCapturer(boolean isFront) {
        Camera1Enumerator enumerator = new Camera1Enumerator(false);
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        for (String deviceName : deviceNames) {
            if (isFront ? enumerator.isFrontFacing(deviceName) : enumerator.isBackFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }




}

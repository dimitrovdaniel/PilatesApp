package com.pilates.app.service;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.pilates.app.UserRegistry;
import com.pilates.app.model.Action;
import com.pilates.app.model.ActionBody;
import com.pilates.app.model.ActionType;
import com.pilates.app.model.Candidate;
import com.pilates.app.ws.SignalingWebSocket;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

import static com.pilates.app.util.Constant.HandlerMessage.HANDLE_REMOTE_VIDEO;

public class PeerConnectionClient {

    private static PeerConnectionClient instance = new PeerConnectionClient();
    private PeerConnectionListener peerConnectionListener = new PeerConnectionListener();

    private Handler uiHandler;
    private EglBase.Context eglBaseContext;
    private Context applicationContext;
    private PeerConnectionFactory peerConnectionFactory;
    private org.webrtc.PeerConnection peerConnection;

    private VideoCapturer videoCapturer;
    private SurfaceViewRenderer localView;
    private SurfaceViewRenderer remoteView;

    private MediaStream localMediaStream;

    private PeerConnectionClient() {

    }


    public static PeerConnectionClient getInstance() {
        if (instance == null) {
            instance = new PeerConnectionClient();
        }

        return instance;
    }

    //1
    public void initPeerConnectionFactory(final Context applicationContext) {


        this.eglBaseContext = EglBase.create().getEglBaseContext();
        this.applicationContext = applicationContext;
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions
                .builder(applicationContext)
                .createInitializationOptions());

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();


        DefaultVideoEncoderFactory defaultVideoEncoderFactory =
                new DefaultVideoEncoderFactory(this.eglBaseContext, true, true);
        DefaultVideoDecoderFactory defaultVideoDecoderFactory =
                new DefaultVideoDecoderFactory(this.eglBaseContext);

        this.peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(defaultVideoEncoderFactory)
                .setVideoDecoderFactory(defaultVideoDecoderFactory)
                .createPeerConnectionFactory();

    }


    //2
    public void initPeerConnection(final MediaStream mediaStream) {

        final List<org.webrtc.PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(org.webrtc.PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());

        this.peerConnection = peerConnectionFactory.createPeerConnection(iceServers, peerConnectionListener);

        this.peerConnection.addStream(mediaStream);

        MediaConstraints constraints = new MediaConstraints();
        constraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        constraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        this.peerConnection.createOffer(peerConnectionListener, constraints);


    }

    public MediaStream initLocalMediaStream() {


        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext);
        // create VideoCapturer
        videoCapturer = createCameraCapturer(true);
        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
        AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());

        videoCapturer.initialize(surfaceTextureHelper, applicationContext, videoSource.getCapturerObserver());

        // create VideoTrack
        VideoTrack videoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);
        // display in localView

        AudioTrack audioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);
        audioTrack.setEnabled(false);


        localMediaStream = peerConnectionFactory.createLocalMediaStream("mediaStreamLocal");
        localMediaStream.addTrack(videoTrack);
        localMediaStream.addTrack(audioTrack);

        return localMediaStream;
    }


    public VideoCapturer createCameraCapturer(boolean isFront) {
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

    public void attachStreamToViews(final SurfaceViewRenderer localView, final SurfaceViewRenderer remoteView) {
        localView.setMirror(true);
        localView.init(eglBaseContext, null);

        remoteView.setMirror(false);
        remoteView.init(eglBaseContext, null);

        localMediaStream.videoTracks.get(0).addSink(localView);
    }

    //int width, int height, int framerate
    public void startStream(int width, int high, int fps) {
//        videoCapturer.startCapture(480, 640, 30);
        videoCapturer.startCapture(width, high, fps);
    }

    public void stopStream() {
        try {
            videoCapturer.stopCapture();
        } catch (InterruptedException e) {
            Log.i("[STOP STREAM]", "Could not stop initLocalMediaStream cause of: " + e.getMessage());
            Log.d("[STOP STREAM]", "Could not stop initLocalMediaStream cause of: " + e);
        }
    }

    public void resumeStream() {
        videoCapturer.startCapture(480, 640, 30);
    }

    public void setUiHandler(final Handler handler) {
        this.uiHandler = handler;
    }

    public void setRemoteDescription(final String answer) {
        final SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER, answer);
        this.peerConnection.setRemoteDescription(peerConnectionListener, sdp);
        UserRegistry.getInstance().getUser().setAnswer(sdp);
    }

    public void addIceCandidate(final Candidate candidate) {
        final IceCandidate ice = new IceCandidate(candidate.getSdpMid(), candidate.getSdpMLineIndex(), candidate.getCandidate());
        this.peerConnection.addIceCandidate(ice);
    }


    private class PeerConnectionListener implements org.webrtc.PeerConnection.Observer, SdpObserver {

        private void log(String s) {
            Log.i("[PEER CONNECTION LISTENER] ", s);
        }

        @Override
        public void onSignalingChange(org.webrtc.PeerConnection.SignalingState signalingState) {
            log("[Signaling state change] " + signalingState);
        }

        @Override
        public void onIceConnectionChange(org.webrtc.PeerConnection.IceConnectionState iceConnectionState) {
            log("[Ice connection state change] " + iceConnectionState);
        }

        @Override
        public void onStandardizedIceConnectionChange(org.webrtc.PeerConnection.IceConnectionState newState) {
            log("[Standardized Ice connection state change] " + newState);
        }

        @Override
        public void onConnectionChange(org.webrtc.PeerConnection.PeerConnectionState newState) {
            log("[Connection state change] " + newState);
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
            log("[Ice connection receiving change] " + b);

        }

        @Override
        public void onIceGatheringChange(org.webrtc.PeerConnection.IceGatheringState iceGatheringState) {
            log("[Ice gathering state change] " + iceGatheringState);
        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            log("[Ice candidate] " + iceCandidate);
            final Candidate candidate = Candidate.newBuilder()
                    .withCandidate(iceCandidate.sdp)
                    .withSdpMid(iceCandidate.sdpMid)
                    .withSdpMLineIndex(iceCandidate.sdpMLineIndex).build();
            final ActionBody body = ActionBody.newBuilder().withIceCandidate(candidate).build();

            SignalingWebSocket.getInstance().sendMessage(new Action(ActionType.ICE_EXCHANGE, body));
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
            log("[Ice candidates removed ]" + iceCandidates.length);
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            log("[Received remote initLocalMediaStream]");
            final VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);

            final Message message = uiHandler.obtainMessage(HANDLE_REMOTE_VIDEO, remoteVideoTrack);
            uiHandler.sendMessage(message);
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            log("[Remove media initLocalMediaStream]");
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            log("[On data channel]");
        }

        @Override
        public void onRenegotiationNeeded() {
            log("[On negotiation neede]");
        }

        @Override
        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
            log("[On add track] " + mediaStreams.length);
        }

        @Override
        public void onTrack(RtpTransceiver transceiver) {
            log("[On track]");
        }

        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            final String description = sessionDescription.description;

            log("[Offer created successfully] " + description);
            peerConnection.setLocalDescription(peerConnectionListener, sessionDescription);

            final ActionBody body = ActionBody.newBuilder().withOffer(description).build();
            final Action action = new Action(ActionType.OFFER, body);

            SignalingWebSocket.getInstance().sendMessage(action);

//            UserRole role = UserRegistry.getInstance().getUser().getRole();
//            if (Objects.equals(role, UserRole.TRAINER)) {
//                SignalingWebSocket.getInstance().sendMessage(new Action(ActionType.INIT_TRAINER));
//
//            }
////            else if (Objects.equals(role, UserRole.TRAINEE)) {
////                final ActionBody traineeBody = ActionBody.newBuilder().withInfoId(infoId).build();
////                SignalingWebSocket.getInstance().sendMessage(new Action(ActionType.CONNECT_TO, traineeBody));
////            }
        }

        @Override
        public void onSetSuccess() {
            log("[Offer set successfully] ");
        }

        @Override
        public void onCreateFailure(String s) {
            log("[Offer could not created] " + s);
        }

        @Override
        public void onSetFailure(String s) {
            log("[Offer could not set] " + s);
        }
    }
}


package com.pilates.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pilates.app.model.Action;
import com.pilates.app.model.ActionBody;
import com.pilates.app.model.ActionType;
import com.pilates.app.model.UserRole;
import com.pilates.app.model.UserSession;
import com.pilates.app.ws.SignalingWebSocket;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.pilates.app.util.Constant.HandlerMessage.HANDLE_CONNECTION_ESTABLISHED;
import static com.pilates.app.util.Constant.HandlerMessage.HANDLE_ON_HOLD;
import static com.pilates.app.util.Constant.HandlerMessage.HANDLE_REMOTE_VIDEO;
import static com.pilates.app.util.Constant.HandlerMessage.HANDLE_SWITCHED;
import static com.pilates.app.util.Constant.HandlerMessage.HANDLE_TRAINEE_LEAVED;


public class MainActivity extends AppCompatActivity {
    private final UserRegistry userRegistry = UserRegistry.getInstance();
    private com.pilates.app.PeerConnection pc = com.pilates.app.PeerConnection.getInstance();
    private SurfaceViewRenderer localView;
    private SurfaceViewRenderer remoteView;
    private TextView timerView;
    private MediaStream mediaStream;
    private VideoCapturer videoCapturer;
    boolean videoCaptureStopped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EglBase.Context eglBaseContext = EglBase.create().getEglBaseContext();

        timerView = findViewById(R.id.timerView);
        localView = findViewById(R.id.svLocalView);
        remoteView = findViewById(R.id.svRemoteView);

        final UserSession user = userRegistry.getUser();

        final CountDownTimer countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                System.out.println("TIMER: " + millisUntilFinished);
                long seconds = millisUntilFinished / 1000;
                String time = String.format("Remaining: %02d", seconds);
                timerView.setText(time);
            }

            @Override
            public void onFinish() {
                final UserSession user = userRegistry.getUser();
                if (Objects.equals(user.getRole(), UserRole.TRAINER)) {
                    SignalingWebSocket.getInstance().sendMessage(new Action(ActionType.NEXT));
                }
                System.out.println("TIMER FINISHED");
            }
        };


        final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                int what = msg.what;
                if (Objects.equals(what, HANDLE_REMOTE_VIDEO)) {
                    VideoTrack remoteVideoTrack = (VideoTrack) msg.obj;
                    runOnUiThread(() -> remoteVideoTrack.addSink(remoteView));
                } else if (Objects.equals(what, HANDLE_CONNECTION_ESTABLISHED)) {
                    countDownTimer.start();
                }  else if (Objects.equals(what, HANDLE_TRAINEE_LEAVED)) {
                    countDownTimer.cancel();
                    timerView.setText("");
                    SignalingWebSocket.getInstance().sendMessage(new Action(ActionType.NEXT));
                } else if (Objects.equals(what, HANDLE_ON_HOLD)) {
                    countDownTimer.cancel();
                    final String connectorName = user.getConnectorName();
                    timerView.setText("On hold with: " + connectorName);
                } else if (Objects.equals(what, HANDLE_SWITCHED)) {
                    timerView.setText("");
                }
            }
        };

        pc.initPeerConnectionFactory(eglBaseContext, this, handler);

        final PeerConnectionFactory peerConnectionFactory = pc.getPeerConnectionFactory();


        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext);
        // create VideoCapturer
        videoCapturer = createCameraCapturer(true);
        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
        AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());

        videoCapturer.initialize(surfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());
        videoCapturer.startCapture(480, 640, 30);


        localView.setMirror(true);
        localView.init(eglBaseContext, null);

        // create VideoTrack
        VideoTrack videoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);
        // display in localView
        videoTrack.addSink(localView);

        AudioTrack audioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);
        audioTrack.setEnabled(true);

        remoteView.setMirror(false);
        remoteView.init(eglBaseContext, null);


        mediaStream = peerConnectionFactory.createLocalMediaStream("mediaStreamLocal");
        mediaStream.addTrack(videoTrack);
        mediaStream.addTrack(audioTrack);

        pc.createPeerConnection(mediaStream);

        final Button stopButton = findViewById(R.id.stopButton);
        final Button holdButton = findViewById(R.id.holdButton);
        final Button nextButton = findViewById(R.id.nextButton);
        final RelativeLayout trainerButtonsSection = findViewById(R.id.trainerButtonsSection);

        stopButton.setOnClickListener(v -> {

            UserRole role = userRegistry.getUser().getRole();
            SignalingWebSocket.getInstance().sendMessage(new Action(ActionType.STOP_COMMUNICATION, null));
            if (role == UserRole.TRAINEE) {
                startActivity(new Intent(this, PostTraineeRegisterActivity.class));
            } else {
                startActivity(new Intent(this, PostTrainerRegisterActivity.class));
            }

        });


        if (Objects.equals(user.getRole(), UserRole.TRAINER)) {
            trainerButtonsSection.setVisibility(VISIBLE);

            holdButton.setOnClickListener(listener -> {
                holdButton.setVisibility(GONE);
                nextButton.setVisibility(VISIBLE);
                countDownTimer.cancel();
                final String connectorName = user.getConnectorName();
                final String connectorId = user.getConnectorId();
                timerView.setText("On hold with: " + connectorName);
                final ActionBody body = ActionBody.newBuilder().withInfoId(connectorId).build();
                SignalingWebSocket.getInstance().sendMessage(new Action(ActionType.ON_HOLD, body));
            });

            nextButton.setOnClickListener(listener -> {
                nextButton.setVisibility(GONE);
                holdButton.setVisibility(VISIBLE);
                SignalingWebSocket.getInstance().sendMessage(new Action(ActionType.NEXT));
            });
        }

    }


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


    @Override
    protected void onPause() {
        System.out.println("ON PAUSE");
        super.onPause();
        if (videoCapturer != null && !videoCaptureStopped) {
            try {
                videoCapturer.stopCapture();
                videoCaptureStopped = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("STOPPED CAPTURE VIDEO");

        }
    }

    @Override
    protected void onResume() {
        System.out.println("ON RESUME");
        super.onResume();
        if (videoCapturer != null && videoCaptureStopped) {
            videoCapturer.startCapture(480, 640, 30);
            videoCaptureStopped = false;

            System.out.println("STARTING CAPTURE VIDEO");
        }
    }

}

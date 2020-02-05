package com.pilates.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.pilates.app.handler.Timer;
import com.pilates.app.model.Action;
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
    private ProgressBar pbTime;
    private MediaStream mediaStream;
    private VideoCapturer videoCapturer;
    boolean videoCaptureStopped = false;
    private float touchStartX;
    private float touchStartY;

    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EglBase.Context eglBaseContext = EglBase.create().getEglBaseContext();

        ToggleFullscreen();

        pbTime = findViewById(R.id.pbTime);
        localView = findViewById(R.id.svLocalView);
        remoteView = findViewById(R.id.svRemoteView);

        findViewById(R.id.frameBotTrigger).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    touchStartX = event.getX();
                    touchStartY = event.getY();
                } else if (event.getAction() == MotionEvent.ACTION_UP && touchStartY > event.getY()) {
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int height = displayMetrics.heightPixels;

                    FrameLayout fds = findViewById(R.id.frameDisplaySettings);
                    fds.animate().y(height * 0.20f);
                    fds.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (height * 0.90f)));
                }
                return true;
            }
        });

        findViewById(R.id.frameTopTrigger).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final UserSession user = userRegistry.getUser();

                if (Objects.equals(user.getRole(), UserRole.TRAINER)) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        // hold
                        timer.stop();
                        final String connectorName = user.getConnectorName();
                        final String connectorId = user.getConnectorId();
                        //timerView.setText("On hold with: " + connectorName);
//                        final ActionBody body = ActionBody.newBuilder().withInfoId(connectorId).build();
//                        SignalingWebSocket.getInstance().sendMessage(new Action(ActionType.ON_HOLD, body));
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        // release
                        SignalingWebSocket.getInstance().sendMessage(new Action(ActionType.NEXT));
                    }
                }
                return true;
            }
        });

        final UserSession user = userRegistry.getUser();

        timer = new Timer(pbTime);


        final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                int what = msg.what;
                if (Objects.equals(what, HANDLE_REMOTE_VIDEO)) {
                    VideoTrack remoteVideoTrack = (VideoTrack) msg.obj;
                    runOnUiThread(() -> remoteVideoTrack.addSink(remoteView));
                } else if (Objects.equals(what, HANDLE_CONNECTION_ESTABLISHED)) {
                    long remainingTime = (long) msg.obj;
                    timer.start(remainingTime, 1000);
                } else if (Objects.equals(what, HANDLE_TRAINEE_LEAVED)) {
                    timer.stop();
                    pbTime.setProgress(0);
                    SignalingWebSocket.getInstance().sendMessage(new Action(ActionType.NEXT));
                } else if (Objects.equals(what, HANDLE_ON_HOLD)) {
                    timer.stop();
                    final String connectorName = user.getConnectorName();
                    //timerView.setText("On hold with: " + connectorName);
                } else if (Objects.equals(what, HANDLE_SWITCHED)) {
                    pbTime.setProgress(0);
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

        final ImageView stopButton = findViewById(R.id.stopButton);
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


        /*if (Objects.equals(user.getRole(), UserRole.TRAINER)) {
            trainerButtonsSection.setVisibility(VISIBLE);

            holdButton.setOnClickListener(listener -> {
            });

            nextButton.setOnClickListener(listener -> {
                nextButton.setVisibility(GONE);
                holdButton.setVisibility(VISIBLE);
            });
        }*/

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

        ToggleFullscreen();
    }

    public void ToggleFullscreen() {
        //for new api versions.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }
}

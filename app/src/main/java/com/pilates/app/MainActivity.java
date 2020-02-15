package com.pilates.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.pilates.app.controls.SlidingPanel;
import com.pilates.app.controls.TestButton;
import com.pilates.app.controls.listeners.OnSlidingPanelEventListener;
import com.pilates.app.controls.listeners.OnTestButtonListener;
import com.pilates.app.handler.Timer;
import com.pilates.app.handler.listeners.OnTimerCompleteListener;
import com.pilates.app.model.Action;
import com.pilates.app.model.ActionBody;
import com.pilates.app.model.ActionType;
import com.pilates.app.model.ClassInitData;
import com.pilates.app.model.MediaStats;
import com.pilates.app.model.MediaType;
import com.pilates.app.model.UserRole;
import com.pilates.app.model.UserSession;
import com.pilates.app.service.PeerConnectionClient;
import com.pilates.app.ws.SignalingWebSocket;
import com.pilates.app.ws.SignalingWebSocketListener;

import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import java.util.Objects;

import static com.pilates.app.util.Constant.HandlerMessage.CLASS_INITIALIZED;
import static com.pilates.app.util.Constant.HandlerMessage.HANDLE_CONNECTION_ESTABLISHED;
import static com.pilates.app.util.Constant.HandlerMessage.HANDLE_MEDIA_STATS;
import static com.pilates.app.util.Constant.HandlerMessage.HANDLE_ON_HOLD;
import static com.pilates.app.util.Constant.HandlerMessage.HANDLE_REMOTE_VIDEO;
import static com.pilates.app.util.Constant.HandlerMessage.HANDLE_SWITCHED;
import static com.pilates.app.util.Constant.HandlerMessage.HANDLE_TRAINEE_LEAVED;


public class MainActivity extends AppCompatActivity {
    private final UserRegistry userRegistry = UserRegistry.getInstance();
    private PeerConnectionClient peerConnectionClient = PeerConnectionClient.getInstance();
    private final SignalingWebSocket webSocket = SignalingWebSocket.getInstance();
    private final SignalingWebSocketListener webSocketListener = SignalingWebSocketListener.getInstance();

    private SurfaceViewRenderer localView;
    private SurfaceViewRenderer remoteView;
    private ProgressBar pbTime;
    boolean videoCaptureStopped = false;
    private float touchStartX;
    private float touchStartY;

    private Timer timer;
    private Timer sessionTimer;

    private SlidingPanel slidingPanel;
    private ProgressBar pbTimeCurrent;
    private TextView txtTimeRemaining;
    private TestButton tbAudio;
    private TestButton tbStream;

    private boolean classReadyToStart;
    private ImageView btnStartClass;
    private LinearLayout layoutButtonsTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ToggleFullscreen();

        pbTime = findViewById(R.id.pbTimeSession);
        pbTimeCurrent = findViewById(R.id.pbTimeCurrent);
        localView = findViewById(R.id.svLocalView);
        remoteView = findViewById(R.id.svRemoteView);
        slidingPanel = findViewById(R.id.frameDisplaySettings);
        txtTimeRemaining = findViewById(R.id.txtTimeRemaining);
        layoutButtonsTest = findViewById(R.id.layoutButtonsTest);
        tbAudio = findViewById(R.id.tbAudio);
        tbStream = findViewById(R.id.tbStream);
        btnStartClass = findViewById(R.id.btnStartClass);

        btnStartClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(classReadyToStart) {
                    layoutButtonsTest.setVisibility(View.GONE);
                    // start class
//                    startStream();
                }
            }
        });

        tbAudio.setListener(new OnTestButtonListener() {
            @Override
            public void clicked(TestButton.TestButtonState state) {
                final ActionBody body = ActionBody.newBuilder().withMediaStats(new MediaStats(MediaType.AUDIO)).build();
                webSocket.sendMessage(new Action(ActionType.STATS, body));
                // TODO start audio test
            }

            @Override
            public void progressCompleted() {
                // TODO do something after 10 seconds (test completed)

                // below sets result of test
                tbAudio.setTestSuccess(true);
                checkClassReadyToStart();
            }
        });

        tbStream.setListener(new OnTestButtonListener() {
            @Override
            public void clicked(TestButton.TestButtonState state) {

                peerConnectionClient.attachLocalStreamToView(localView);
                peerConnectionClient.attachLocalStreamToView(remoteView);
                final ActionBody body = ActionBody.newBuilder().withMediaStats(new MediaStats(MediaType.VIDEO)).build();
                webSocket.sendMessage(new Action(ActionType.STATS, body));
                // TODO start stream test
            }

            @Override
            public void progressCompleted() {
                // TODO do something after stream test progress is completed

                peerConnectionClient.detachLocalStreamFromView(remoteView);
                remoteView.clearImage();
                tbStream.setTestSuccess(true);
                checkClassReadyToStart();
            }
        });

        slidingPanel.setListener(new OnSlidingPanelEventListener() {
            @Override
            public void changeLayout(String tag) {
                final float scale = getResources().getDisplayMetrics().density;
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                localView.setVisibility(View.VISIBLE);
                ViewCompat.setTranslationZ(localView, 1f);
                ViewCompat.setTranslationZ(remoteView, 0f);

                if(tag.equals("me_small")) {
                    FrameLayout.LayoutParams rvLP = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    remoteView.setLayoutParams(rvLP);

                    FrameLayout.LayoutParams mlp = new FrameLayout.LayoutParams((int) (100 * scale + 0.5f),(int) (200 * scale + 0.5f));
                    mlp.topMargin = (int) (40 * scale + 0.5f);
                    mlp.rightMargin = (int) (30 * scale + 0.5f);
                    mlp.gravity = Gravity.RIGHT;

                    localView.setLayoutParams(mlp);

                    localView.setBackgroundResource(R.drawable.small_cam_view);
                    remoteView.setBackground(null);
                }
                else if(tag.equals("split")) {
                    FrameLayout.LayoutParams rvLP = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)(displayMetrics.heightPixels * 0.7f));
                    rvLP.gravity = Gravity.TOP;
                    rvLP.topMargin = (int)(displayMetrics.heightPixels * 0.5f);
                    remoteView.setLayoutParams(rvLP);

                    FrameLayout.LayoutParams lvLP = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)(displayMetrics.heightPixels * 0.5f));
                    lvLP.gravity = Gravity.TOP;
                    localView.setLayoutParams(lvLP);

                    localView.setBackground(null);
                    remoteView.setBackground(null);
                }
                else if(tag.equals("me_large")) {

                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                    FrameLayout.LayoutParams mlp = new FrameLayout.LayoutParams((int) (100 * scale + 0.5f), (int) (200 * scale + 0.5f));
                    mlp.topMargin = (int) (40 * scale + 0.5f);
                    mlp.rightMargin = (int) (30 * scale + 0.5f);
                    mlp.gravity = Gravity.RIGHT;

                    remoteView.setLayoutParams(mlp);
                    localView.setLayoutParams(layoutParams);

                    localView.setBackground(null);
                    remoteView.setBackgroundResource(R.drawable.small_cam_view);

                    ViewCompat.setTranslationZ(localView, 0f);
                    ViewCompat.setTranslationZ(remoteView, 1f);
                }
                else if(tag.equals("me_hidden")) {
                    localView.setVisibility(View.GONE);
                    FrameLayout.LayoutParams rvLP = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    remoteView.setLayoutParams(rvLP);
                    remoteView.setBackground(null);
                }
            }
        });

        findViewById(R.id.frameBotTrigger).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    touchStartX = event.getX();
                    touchStartY = event.getY();
                } else if (event.getAction() == MotionEvent.ACTION_UP && touchStartY > event.getY()) {
                    slidingPanel.showPanel();
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
                        webSocket.sendMessage(new Action(ActionType.NEXT));
                    }
                }
                return true;
            }
        });

        final UserSession user = userRegistry.getUser();

        timer = new Timer(pbTimeCurrent);
        timer.setListener(new OnTimerCompleteListener() {
            @Override
            public void completed() {
                final UserSession user = UserRegistry.getInstance().getUser();
                System.out.println("TIMER FINISHED");
                if (Objects.equals(user.getRole(), UserRole.TRAINER)) {
                    SignalingWebSocket.getInstance().sendMessage(new Action(ActionType.NEXT));
                }
            }
        });
        sessionTimer = new Timer(pbTime, txtTimeRemaining);

        final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                int what = msg.what;
                switch (what) {
                    case HANDLE_REMOTE_VIDEO:
                        Log.i("[MAIN UI HANDLER]", "Handling remote videoTrack");
                        VideoTrack remoteVideoTrack = (VideoTrack) msg.obj;
                        runOnUiThread(() -> remoteVideoTrack.addSink(remoteView));
                        break;

                    case HANDLE_CONNECTION_ESTABLISHED:
                        long remainingTime = (long) msg.obj;
                        timer.start(remainingTime, 1000);
                        break;

                    case HANDLE_TRAINEE_LEAVED:
                        timer.stop();
                        pbTimeCurrent.setProgress(0);
                        webSocket.sendMessage(new Action(ActionType.NEXT));
                        break;

                    case HANDLE_ON_HOLD:
                        timer.stop();
                        final String connectorName = user.getConnectorName();
                        break;

                    case HANDLE_SWITCHED:
                        pbTimeCurrent.setProgress(0);
                        break;

                    case CLASS_INITIALIZED:
                        ClassInitData classData = (ClassInitData) msg.obj;

                        int mins = (int) ((classData.totalSeconds - classData.currentSeconds) / 60);
                        int secondsToMin = (int) (classData.totalSeconds - classData.currentSeconds) - (mins * 60);

                        txtTimeRemaining.setText(String.format("00", mins) + ":" + String.format("00", secondsToMin));
                        pbTime.setProgress((int) ((classData.currentSeconds / (double) classData.totalSeconds) * 100));

                        sessionTimer.start((classData.totalSeconds - classData.currentSeconds) * 1000, 1000);
                        break;

                    case HANDLE_MEDIA_STATS:
                        final MediaStats mediaStats = (MediaStats) msg.obj;
                        System.out.println("MEDIA STATS HANDLED: " + mediaStats.toString());

                        final MediaType mediaType = mediaStats.getMediaType();
                        long bytesReceived = mediaStats.getBytesReceived();
                        long packetsReceived = mediaStats.getPacketsReceived();
                        long packetsLost = mediaStats.getPacketsLost();
                        long remb = mediaStats.getRemb();

                        break;

                    default:
                        Log.i("[MAIN UI HANDLER]", "No such operation");
                        break;
                }

            }
        };

        webSocketListener.setMainUIHandler(handler);
        peerConnectionClient.setUiHandler(handler);
        startStream();

        pbTimeCurrent.setVisibility(View.GONE);
    }

    private void startStream() {
        // I commented this because it initiated in Start class activity (for trainer)
        // and Post trainee register activity (for trainee)
        // when we remove this activities it comes here

//        peerConnectionClient.initPeerConnectionFactory(this);
//        peerConnectionClient.initPeerConnection(peerConnectionClient.initLocalMediaStream());
        peerConnectionClient.initLocalAndRemoteViews(localView, remoteView);
        peerConnectionClient.startStream(1080, 1920, 30);

        // show timer for current trainee
        pbTimeCurrent.setVisibility(View.VISIBLE);
    }

    private void checkClassReadyToStart() {
        classReadyToStart = tbAudio.testComplete && tbStream.testComplete;
        btnStartClass.setImageResource(classReadyToStart ? R.drawable.btn_start : R.drawable.btn_start_inactive);
    }


    @Override
    protected void onPause() {
        System.out.println("ON PAUSE");
        super.onPause();

        if (!videoCaptureStopped) {
            peerConnectionClient.stopStream();
            videoCaptureStopped = true;
            System.out.println("STOPPED CAPTURE VIDEO");

        }
    }

    @Override
    protected void onResume() {
        System.out.println("ON RESUME");
        super.onResume();
        if (videoCaptureStopped) {
            peerConnectionClient.resumeStream();
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

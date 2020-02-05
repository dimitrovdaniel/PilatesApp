package com.pilates.app.handler;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.pilates.app.model.Action;
import com.pilates.app.model.ActionBody;
import com.pilates.app.model.ActionType;
import com.pilates.app.model.Candidate;
import com.pilates.app.ws.SignalingWebSocket;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.VideoTrack;

import static com.pilates.app.util.Constant.HandlerMessage.HANDLE_REMOTE_VIDEO;

/**
 * Created by chao on 2019/1/28.
 */

public class PeerConnectionAdapter implements PeerConnection.Observer {

    private static PeerConnectionAdapter instance = new PeerConnectionAdapter();

    private String tag;
    private Handler handler;

    private PeerConnectionAdapter() {
        this.tag = "ICE ADAPTER ";
    }

    public static PeerConnectionAdapter getInstance() {
        if (instance == null) {
            instance = new PeerConnectionAdapter();
        }

        return instance;
    }
    private void log(String s) {
        Log.d(tag, s);
    }

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        log("onSignalingChange " + signalingState);
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        log("onIceConnectionChange " + iceConnectionState);
        // when ice ended CONNECTED
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        log("onIceConnectionReceivingChange " + b);
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        log("onIceGatheringChange " + iceGatheringState);

        //when ice ended COMPLETE


    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        log("onIceCandidate " + iceCandidate);
        System.out.println("SENDING ICE: " + iceCandidate);

        final Candidate candidate = Candidate.newBuilder()
                .withCandidate(iceCandidate.sdp)
                .withSdpMid(iceCandidate.sdpMid)
                .withSdpMLineIndex(iceCandidate.sdpMLineIndex).build();
        final ActionBody body = ActionBody.newBuilder().withIceCandidate(candidate).build();


        SignalingWebSocket.getInstance().sendMessage(new Action(ActionType.ICE_EXCHANGE, body));
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        log("onIceCandidatesRemoved " + iceCandidates);
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        log("onAddStream " + mediaStream);

        System.out.println("STREAM TO LOCAL VIEW ADDED");
        VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);

        final Message message = handler.obtainMessage(HANDLE_REMOTE_VIDEO, remoteVideoTrack);
        handler.sendMessage(message);
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        log("onRemoveStream " + mediaStream);
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        log("onDataChannel " + dataChannel);
    }

    @Override
    public void onRenegotiationNeeded() {
        log("onRenegotiationNeeded ");
    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
        log("onAddTrack " + mediaStreams);
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }
}

package com.pilates.app.model;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.util.ArrayList;
import java.util.List;


public class UserSession {

    private final String name;
    private final UserRole role;

    private final List<IceCandidate> remoteCandidates = new ArrayList<>();

    private String trainerName;
    private boolean init;
    private SessionDescription answer;
    public UserSession(final String name, final UserRole role) {
        this.name = name;
        this.role = role;
    }



    public void addRemoteCandidate(final IceCandidate candidate) {
        remoteCandidates.add(candidate);
    }

    public String getName() {
        return name;
    }

    public UserRole getRole() {
        return role;
    }

    public List<IceCandidate> getRemoteCandidates() {
        return remoteCandidates;
    }

    public boolean isInit() {
        return init;
    }

    public void setInit(boolean init) {
        this.init = init;
    }

    public SessionDescription getAnswer() {
        return answer;
    }

    public void setAnswer(SessionDescription answer) {
        this.answer = answer;
    }

//    public void setWsSession(WebSocket wsSession) {
//        this.wsSession = wsSession;
//    }

    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }
}

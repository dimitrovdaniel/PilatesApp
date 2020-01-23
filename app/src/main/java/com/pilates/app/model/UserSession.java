package com.pilates.app.model;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.util.ArrayList;
import java.util.List;


public class UserSession {

    private final String infoId;
    private final String name;
    private final UserRole role;

    private final List<IceCandidate> remoteCandidates = new ArrayList<>();

    private String trainerInfoId;
    private String connectorId;
    private String connectorName;
    private boolean init;
    private SessionDescription answer;
    public UserSession(String infoId, final String name, final UserRole role) {
        this.infoId = infoId;
        this.name = name;
        this.role = role;
    }



    public void addRemoteCandidate(final IceCandidate candidate) {
        remoteCandidates.add(candidate);
    }

    public String getConnectorName() {
        return connectorName;
    }

    public void setConnectorName(String connectorName) {
        this.connectorName = connectorName;
    }

    public String getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
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


    public String getInfoId() {
        return infoId;
    }

    public String getTrainerInfoId() {
        return trainerInfoId;
    }

    public void setConnectorInfoId(String trainerInfoId) {
        this.trainerInfoId = trainerInfoId;
    }
}

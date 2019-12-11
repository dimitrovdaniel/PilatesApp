package com.pilates.app.model;

import com.neovisionaries.ws.client.WebSocket;

import org.webrtc.IceCandidate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class UserSession {

    private final String name;
    private final UserRole role;


    private final List<Candidate> localCandidates = new ArrayList<Candidate>();
    private final List<Candidate> remoteCandidates = new ArrayList<Candidate>();

    private String calleeName;
    private boolean init;
    private String offer;
    private WebSocket wsSession;

    public UserSession(final String name, final UserRole role) {
        this.name = name;
        this.role = role;
    }

    public void sendMessage(final Action action) {


        synchronized (wsSession) {

            System.out.println("Sending message to: " + name + " value: " + action.toString());

            wsSession.sendText(action.toString());
        }

    }


    public void addLocalCandidate(final Candidate candidate) {
        localCandidates.add(candidate);
    }


    public void addRemoteCandidate(final Candidate candidate) {
        localCandidates.add(candidate);
    }

    public String getName() {
        return name;
    }

    public UserRole getRole() {
        return role;
    }

    public WebSocket getWsSession() {
        return wsSession;
    }

    public List<Candidate> getLocalCandidates() {
        return localCandidates;
    }

    public List<Candidate> getRemoteCandidates() {
        return remoteCandidates;
    }

    public boolean isInit() {
        return init;
    }

    public void setInit(boolean init) {
        this.init = init;
    }

    public String getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = offer;
    }

    public void setWsSession(WebSocket wsSession) {
        this.wsSession = wsSession;
    }

    public String getCalleeName() {
        return calleeName;
    }

    public void setCalleeName(String calleeName) {
        this.calleeName = calleeName;
    }
}

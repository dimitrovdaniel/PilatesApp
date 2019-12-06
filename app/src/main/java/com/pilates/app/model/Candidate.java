package com.pilates.app.model;

import com.google.gson.GsonBuilder;

public class Candidate {
    private String candidate;
    private String sdpMid;
    private Integer sdpMLineIndex;

    /*default*/Candidate(Builder builder) {
        this.candidate = builder.candidate;
        this.sdpMid = builder.sdpMid;
        this.sdpMLineIndex = builder.sdpMLineIndex;
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    public String getCandidate() {
        return candidate;
    }

    public void setCandidate(String candidate) {
        this.candidate = candidate;
    }

    public String getSdpMid() {
        return sdpMid;
    }

    public void setSdpMid(String sdpMid) {
        this.sdpMid = sdpMid;
    }

    public Integer getSdpMLineIndex() {
        return sdpMLineIndex;
    }

    public void setSdpMLineIndex(Integer sdpMLineIndex) {
        this.sdpMLineIndex = sdpMLineIndex;
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this, Candidate.class);
    }

    public static final class Builder {
        private String candidate;
        private String sdpMid;
        private Integer sdpMLineIndex;

        /* default */ Builder() {
        }

        public Builder withCandidate(String val) {
            candidate = val;
            return this;
        }

        public Builder withSdpMid(String val) {
            sdpMid = val;
            return this;
        }

        public Builder withSdpMLineIndex(Integer val) {
            sdpMLineIndex = val;
            return this;
        }

        public Candidate build() {
            return new Candidate(this);
        }
    }

}

package com.pilates.app.model;

public class MediaStats {

    private final MediaType mediaType;
    private final long bytesReceived;
    private final long packetsReceived;
    private final long packetsLost;

    /**
    * The Receiver Estimated Maximum Bitrate (REMB) - only for video**/
    private final long remb;

    public MediaStats(MediaType mediaType, long bytesReceived, long packetsReceived, long packetsLost, long remb) {
        this.mediaType = mediaType;
        this.bytesReceived = bytesReceived;
        this.packetsReceived = packetsReceived;
        this.packetsLost = packetsLost;
        this.remb = remb;
    }

    public MediaStats(MediaType mediaType) {
        this.mediaType = mediaType;
        this.bytesReceived = 0;
        this.packetsReceived = 0;
        this.packetsLost = 0;
        this.remb = 0;
    }

    @Override
    public String toString() {
        return "MediaStats{" +
                "mediaType=" + mediaType +
                ", bytesReceived=" + bytesReceived +
                ", packetsReceived=" + packetsReceived +
                ", packetsLost=" + packetsLost +
                ", remb=" + remb +
                '}';
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public long getBytesReceived() {
        return bytesReceived;
    }

    public long getPacketsReceived() {
        return packetsReceived;
    }

    public long getPacketsLost() {
        return packetsLost;
    }

    public long getRemb() {
        return remb;
    }
}

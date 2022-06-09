package com.piano.analytics.avinsights;

abstract class AVRunnable implements Runnable {
    protected Media media;

    AVRunnable(Media media) {
        this.media = media;
    }
}

final class HeartbeatRunnable extends AVRunnable {

    HeartbeatRunnable(Media media) {
        super(media);
    }

    @Override
    public void run() {
        media.processHeartbeat(-1, true, null);
    }
}

final class BufferHeartbeatRunnable extends AVRunnable {

    BufferHeartbeatRunnable(Media media) {
        super(media);
    }

    @Override
    public void run() {
        media.processBufferHeartbeat(true, null);
    }
}

final class RebufferHeartbeatRunnable extends AVRunnable {

    RebufferHeartbeatRunnable(Media media) {
        super(media);
    }

    @Override
    public void run() {
        media.processRebufferHeartbeat(true, null);
    }
}

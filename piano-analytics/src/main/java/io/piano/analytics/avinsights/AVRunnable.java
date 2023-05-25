package io.piano.analytics.avinsights;

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
        media.processHeartbeat(-1, true, media.getExtraProps());
    }
}

final class BufferHeartbeatRunnable extends AVRunnable {

    BufferHeartbeatRunnable(Media media) {
        super(media);
    }

    @Override
    public void run() {
        media.processBufferHeartbeat(true, media.getExtraProps());
    }
}

final class RebufferHeartbeatRunnable extends AVRunnable {

    RebufferHeartbeatRunnable(Media media) {
        super(media);
    }

    @Override
    public void run() {
        media.processRebufferHeartbeat(true, media.getExtraProps());
    }
}

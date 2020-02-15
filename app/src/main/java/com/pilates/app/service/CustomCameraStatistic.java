package com.pilates.app.service;

import org.webrtc.CameraVideoCapturer;
import org.webrtc.Logging;
import org.webrtc.SurfaceTextureHelper;

public class CustomCameraStatistic extends CameraVideoCapturer.CameraStatistics {

    private static final String TAG = "Custom CameraStatistics";
    private static final int CAMERA_OBSERVER_PERIOD_MS = 2000;
    private static final int CAMERA_FREEZE_REPORT_TIMOUT_MS = 4000;
    private final SurfaceTextureHelper surfaceTextureHelper;
    private final CameraVideoCapturer.CameraEventsHandler eventsHandler;
    private int frameCount;
    private int freezePeriodCount;
    private int cameraFps;
    private final Runnable cameraObserver = new Runnable() {
        public void run() {
            cameraFps = Math.round((float) CustomCameraStatistic.this.frameCount * 1000.0F / 2000.0F);
            Logging.d("CUSTOM CameraStatistics", "Camera fps: " + cameraFps + ".");
            if (CustomCameraStatistic.this.frameCount == 0) {
                ++CustomCameraStatistic.this.freezePeriodCount;
                if (2000 * CustomCameraStatistic.this.freezePeriodCount >= 4000 && CustomCameraStatistic.this.eventsHandler != null) {
                    Logging.e("CUSTOM CameraStatistics", "Camera freezed.");
//                    if (CustomCameraStatistic.this.surfaceTextureHelper.isTextureInUse()) {
//                        CustomCameraStatistic.this.eventsHandler.onCameraFreezed("CUSTOM Camera failure. Client must return video buffers.");
//                    } else {
//                        CustomCameraStatistic.this.eventsHandler.onCameraFreezed("CUSTOM Camera failure.");
//                    }

                    return;
                }
            } else {
                CustomCameraStatistic.this.freezePeriodCount = 0;
            }

            CustomCameraStatistic.this.frameCount = 0;
            surfaceTextureHelper.getHandler().postDelayed(this, 2000L);
        }
    };

    public int getFPS() {
        return cameraFps;
    }

    public CustomCameraStatistic(SurfaceTextureHelper surfaceTextureHelper, CameraVideoCapturer.CameraEventsHandler eventsHandler) {
        super(surfaceTextureHelper, eventsHandler);

        this.surfaceTextureHelper = surfaceTextureHelper;
        this.eventsHandler = eventsHandler;
        this.frameCount = 0;
        this.freezePeriodCount = 0;
        surfaceTextureHelper.getHandler().postDelayed(this.cameraObserver, 2000L);
    }

}

package com.lukoxa.videoreg;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.view.SurfaceView;

public class Recorder {
    private final static String TAG = Recorder.class.getName();
    private Camera camera;
    private SurfaceView surfaceView;
    private MediaRecorder mediaRecorder;
    private boolean isWorking = false;

    private final ErrorHandler errorHandler;

    public Recorder(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
    }

    public boolean isWorking() {
        return isWorking;
    }

    public Camera getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
            } catch (Exception e) {
                errorHandler.onError(TAG + ".getCamera", e);
            }
        }
        return camera;
    }

    public void release() {
        releaseMediaRecorder();
        releaseCamera();
    }

    private void releaseCamera() {
        if (camera != null) {
            try {
                camera.stopPreview();
                camera.release();
                camera = null;
            } catch (Exception e) {
                errorHandler.onError(TAG + ".releaseCamera", e);
            }
        }
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
                camera.lock();
            } catch (Exception e) {
                errorHandler.onError(TAG + ".releaseMediaRecorder", e);
            }
        }
    }

    public boolean start(String fileName) {
        try {
            if (prepareMediaRecorder(fileName)) {
                mediaRecorder.start();
                isWorking = true;
            } else {
                releaseMediaRecorder();
                isWorking = false;
            }
        } catch (Exception e) {
            errorHandler.onError(TAG + ".start", e);
        }
        return isWorking;
    }

    public void stop() {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                releaseMediaRecorder();
                camera.lock();
                isWorking = false;
            }
        } catch (Exception e) {
            errorHandler.onError(TAG + ".stop", e);
        }
    }

    private boolean prepareMediaRecorder(String outputFile) {
        try {
            camera = getCamera();
            mediaRecorder = new MediaRecorder();
            camera.unlock();
            mediaRecorder.setCamera(camera);
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mediaRecorder.setProfile(CamcorderProfile
                    .get(CamcorderProfile.QUALITY_HIGH));
            mediaRecorder.setOutputFile(outputFile);
            mediaRecorder.setPreviewDisplay(surfaceView.getHolder()
                    .getSurface());
            try {
                mediaRecorder.prepare();
            } catch (Exception e) {
                errorHandler.onError(TAG + ".prepareVideoRecorder.prepare", e);
                releaseMediaRecorder();
                return false;
            }
        } catch (Exception e) {
            errorHandler.onError(TAG + ".prepareVideoRecorder", e);
            return false;
        }
        return true;
    }
}
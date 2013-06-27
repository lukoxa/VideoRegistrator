package com.lukoxa.videoreg;

import java.io.IOException;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Preview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = Preview.class.getName();
    private final ErrorHandler errorHandler;
    private final Camera camera;
    private SurfaceHolder holder;

    public Preview(Context context, ErrorHandler errorHandler, Camera camera) {
        super(context);
        this.errorHandler = errorHandler;
        this.camera = camera;
        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (holder.getSurface() == null) {
            return;
        }
        try {
            camera.stopPreview();
        } catch (Exception e) {
            errorHandler.onError(TAG + ".surfaceChanged.stopPreview", e);
        }
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (Exception e) {
            errorHandler.onError(TAG + ".surfaceChanged.startPreview", e);
        }
    }
}
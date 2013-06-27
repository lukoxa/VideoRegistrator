package com.lukoxa.videoreg;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import com.lukoxa.test.R;
import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends Activity implements ErrorHandler {
    private final static int FILE_INTERVAL = 1000 * 60 * 60;
    private final static int LOG_INTERVAL = 1000;
    private final static String DATA_DIR = "VideoReg";
    private final static String TAG = MainActivity.class.getName();
    private final static SimpleDateFormat templateFileName = new SimpleDateFormat(
            "yyyyMMddHHmmss", Locale.US);;
    private LocationManager locationManager;
    private File storageDir;
    private Preview preiview;
    private Button btnRecord;
    private Button btnSave;
    private Recorder recorder;
    private LocationLogger logger;
    private Timer timer;
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview);
        PowerManager pm = (PowerManager) this
                .getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
                | PowerManager.ON_AFTER_RELEASE, TAG);
        try {
            storageDir = new File(Environment.getExternalStorageDirectory(),
                    DATA_DIR);
            if (storageDir.exists() == false) {
                storageDir.mkdirs();
            }
            logger = new LocationLogger(this);
            recorder = new Recorder(this);
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            btnRecord = (Button) findViewById(R.id.button_record);
            btnSave = (Button) findViewById(R.id.button_save);
            btnSave.setEnabled(false);
            FrameLayout previewLayout = (FrameLayout) findViewById(R.id.layout_preview);
            preiview = new Preview(this, this, recorder.getCamera());
            previewLayout.addView(preiview);
            recorder.setSurfaceView(preiview);
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnSave.setEnabled(false);
                    stop();
                    start();
                    btnSave.setEnabled(true);
                }
            });
            btnRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recorder.isWorking() || logger.isWorking()) {
                        stop();
                    } else {
                        start();
                    }
                    checkState();
                }
            });
        } catch (Exception e) {
            onError(TAG + ".onCreate", e);
        }
        checkState();
    }

    private void start() {
        String fileName = templateFileName.format(new Date());
        recorder.start(new File(storageDir, fileName + ".3gp").toString());
        logger.start(new File(storageDir, fileName + ".srt").toString(),
                LOG_INTERVAL);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (recorder.isWorking() || logger.isWorking()) {
                    stop();
                    start();
                }
            }
        }, FILE_INTERVAL);
    }

    private void stop() {
        logger.stop();
        recorder.stop();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void checkState() {
        if (recorder.isWorking() || logger.isWorking()) {
            btnRecord.setText(R.string.button_stop);
            btnSave.setEnabled(true);
        } else {
            btnRecord.setText(R.string.button_record);
            btnSave.setEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        wakeLock.acquire();
    }

    @Override
    protected void onPause() {
        super.onPause();
        wakeLock.release();
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                0, logger);
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(logger);
        logger.stop();
        recorder.stop();
        recorder.release();
    }

    @Override
    public void onError(String prefix, Throwable t) {
        try {
            t.printStackTrace();
            Toast.makeText(this, "Was error" + prefix + "\n" + t.getMessage(),
                    Toast.LENGTH_LONG).show();
        } catch (Throwable global) {
            global.printStackTrace();
        }
    }
}
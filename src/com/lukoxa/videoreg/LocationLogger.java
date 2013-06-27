package com.lukoxa.videoreg;

import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class LocationLogger implements LocationListener {
    private final static SimpleDateFormat templateDate = new SimpleDateFormat(
            "dd.MM.yyyy HH:mm:ss", Locale.US);;
    private final static String TAG = LocationLogger.class.getName();
    private final ErrorHandler errorHandler;
    private Location lastLocation;
    private PrintStream output;
    private Timer timer;
    private boolean isWorking;

    public LocationLogger(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public boolean isWorking() {
        return isWorking;
    }

    public void start(String fileName, int interval) {
        try {
            isWorking = true;
            output = new PrintStream(fileName);
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (LocationLogger.this.isWorking()) {
                        LocationLogger.this.writeLogMessage();
                    }
                }
            }, 0, 1000);
        } catch (IOException ioe) {
            errorHandler.onError(TAG + ".start", ioe);
            stop();
        }
    }

    public void stop() {
        if (isWorking) {
            isWorking = false;
            try {
                timer.cancel();
                timer = null;
                output.flush();
                output.close();
            } catch (Exception e) {
                errorHandler.onError(TAG + ".stop", e);
            }
        }
    }

    private void writeLogMessage() {
        StringBuilder buffer = new StringBuilder();
        if (isWorking && output != null) {
            try {
                buffer.append("Time: ");
                buffer.append(templateDate.format(new Date()));
                buffer.append(" Position: ");
                if (lastLocation == null) {
                    buffer.append("Unknown");
                } else {
                    buffer.append(lastLocation.getLatitude());
                    buffer.append(',');
                    buffer.append(lastLocation.getLongitude());
                }
                output.println(buffer.toString());
            } catch (Exception e) {
                errorHandler.onError(TAG + ".writeLogMessage", e);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.lastLocation = location;
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
}
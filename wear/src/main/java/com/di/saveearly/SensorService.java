package com.di.saveearly;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SensorService extends Service implements SensorEventListener {
    private static final String TAG = "SD/SensorService";

    private final static int SENS_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;
    private final static int TIME_WINDOW = 6000;

    private SensorManager mSensorManager;
    private DeviceClient client;
    private ScheduledExecutorService mScheduler;
    private int counter = 0;
    private long previousTime;
    private double previousValue;
    private double currentValue;

    @Override
    public void onCreate() {
        super.onCreate();

        client = DeviceClient.getInstance(this);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Sensor Dashboard");
        builder.setContentText("Collecting sensor data..");

        startForeground(1, builder.build());

        startMeasurement();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopMeasurement();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void startMeasurement() {
        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));

        Sensor accelerometerSensor = mSensorManager.getDefaultSensor(SENS_ACCELEROMETER);
        mSensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void stopMeasurement() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        if (mScheduler != null && !mScheduler.isTerminated()) {
            mScheduler.shutdown();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        boolean isFall = estimateFall(event);

        if(isFall) {
            //client.sendSensorData(event.sensor.getType(), event.accuracy, event.timestamp, event.values);
            client.send();
        }
    }

    private boolean estimateFall(SensorEvent event){

        if(System.currentTimeMillis() - previousTime > 6000){
            previousTime = -1;
            if(counter >= 1 && counter < 14) {
                counter = 0;
                Log.i(TAG, "fall");
                return true;
            }

            counter = 0;
        }

        if (previousValue == 0){
            previousValue = countSMV(event);
            currentValue = previousValue;
        } else{
            previousValue = currentValue;
            currentValue = countSMV(event);
        }

        if((currentValue - previousValue) >= 10){

            Log.i(TAG, (System.currentTimeMillis() - previousTime) + " " + counter);
            if(previousTime == -1) {
                previousTime = System.currentTimeMillis();
            } else{
                if(System.currentTimeMillis() - previousTime <= 6000){
                    //counter++;
                }
            }
        } else if(previousTime != -1 && (currentValue - previousValue) >= 7){
            Log.i(TAG, (System.currentTimeMillis() - previousTime) + " " + counter);
            if(System.currentTimeMillis() - previousTime <= 6000){
                counter++;
            }
        }

        return false;
    }

    private double countSMV(SensorEvent event){
        double x = Math.pow(event.values[0], 2.0);
        double y = Math.pow(event.values[1], 2.0);
        double z = Math.pow(event.values[2], 2.0);

        return Math.sqrt(x + y + z);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

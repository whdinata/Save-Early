package com.di.saveearly;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.di.saveearly.util.SaveEarlyNotification;
import com.di.saveearly.util.SaveEarlySharedPreferenceHelper;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Arrays;

import saveearly.shared.DataMapKeys;

public class SensorReceiverService extends WearableListenerService{
    private static final String TAG = SensorReceiverService.class.getName();

    private RemoteSensorManager sensorManager;

    @Override
    public void onCreate() {
        super.onCreate();

        sensorManager = RemoteSensorManager.getInstance(this);
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);

        Log.i(TAG, "Connected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);

        Log.i(TAG, "Disconnected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged()");

        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = dataEvent.getDataItem();
                Uri uri = dataItem.getUri();
                String path = uri.getPath();

                if (path.startsWith("/sensors/")) {
                    unpackSensorData(
                        Integer.parseInt(uri.getLastPathSegment()),
                        DataMapItem.fromDataItem(dataItem).getDataMap()
                    );
                }
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i(TAG, "on message Received: " + messageEvent.getPath());
        if (messageEvent.getPath().equals("/fall")) {
            Log.i(TAG, "move");
            SaveEarlyNotification notif = SaveEarlyNotification.getInstance(this.getApplicationContext());
            notif.sendMail(SaveEarlySharedPreferenceHelper.getString(this.getApplicationContext(), "email"));
            notif.sendSMS(SaveEarlySharedPreferenceHelper.getString(this.getApplicationContext(), "phone"));
        }
    }

    private void unpackSensorData(int sensorType, DataMap dataMap) {
        int accuracy = dataMap.getInt(DataMapKeys.ACCURACY);
        long timestamp = dataMap.getLong(DataMapKeys.TIMESTAMP);
        float[] values = dataMap.getFloatArray(DataMapKeys.VALUES);

        if(sensorType == 9) {
            Log.d(TAG, "Received sensor data " + sensorType + " = " + Arrays.toString(values));
        }

        //sensorManager.addSensorData(sensorType, accuracy, timestamp, values);
    }
}

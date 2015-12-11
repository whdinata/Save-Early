package com.di.saveearly;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.di.saveearly.util.SaveEarlyNotification;
import com.di.saveearly.util.SaveEarlySharedPreferenceHelper;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationManager locationManager;
    private String provider;
    private String lon, lat;
    private String email, phone;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Save Early");
        initView();

        getLocation();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void initView(){
        final EditText etEmail = (EditText) findViewById(R.id.email);
        final EditText etPhone = (EditText) findViewById(R.id.phone);
        final TextView tvEmail = (TextView) findViewById(R.id.email_content);
        final TextView tvPhone = (TextView) findViewById(R.id.phone_content);

        findViewById(R.id.add_email).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = etEmail.getText().toString();
                tvEmail.setText(email);
            }
        });

        findViewById(R.id.add_phone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phone = etPhone.getText().toString();
                tvPhone.setText(phone);
            }
        });

        findViewById(R.id.activate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveEarlyNotification notif = SaveEarlyNotification.getInstance(MainActivity.this);
                notif.sendMail(email);
                notif.sendSMS("+447490168528");

                SaveEarlySharedPreferenceHelper.putString(MainActivity.this, "email", email);
                SaveEarlySharedPreferenceHelper.putString(MainActivity.this, "phone", phone);

                        ((Button) v).setText("Deactivate");
                Toast.makeText(MainActivity.this, "Detector has been activated", Toast.LENGTH_LONG).show();
            }
        });
    }



    private void getLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        try {
            Location location = getLastBestLocation(locationManager);

            if (location != null) {
                System.out.println("Provider " + provider + " has been selected.");
                onLocationChanged(location);
            } else {
                //Toast.makeText(this, "Location unavailable", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private Location getLastBestLocation(LocationManager mLocationManager)throws SecurityException {
        Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            return locationGPS;
        }
        else {
            return locationNet;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        RemoteSensorManager.getInstance(this).startMeasurement();

        try {
            locationManager.requestLocationUpdates(provider, 400, 1, this);
        } catch(SecurityException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            locationManager.removeUpdates(this);
        } catch(SecurityException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        lon = lng + "";
        this.lat = lat + "";

        System.out.println("Long: " + lng);
        System.out.println("Lat: " + lat);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }
}

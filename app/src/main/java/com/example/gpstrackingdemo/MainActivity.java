package com.example.gpstrackingdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSION_FINE_LOCATION = 99;

    // references to the UI elements

    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_address, tv_wayPointCounts;
    //Button btn_newWaypoint, btn_showWayPointList;
    Button btn_newWaypoint, btn_showWayPointList, btn_showMap;


    Switch sw_locationupdates, sw_gps;

    // variable to remeber if we are tracking location or not
    boolean updateOn = false;

    // current location
    Location currentLocation;

    // list of the saved locations
    List<Location> savedLocations;


    // Location request is a config file for all settings related to fusedlocation
    LocationRequest locationRequest;


    LocationCallback locationCallback;

    // Google's API for location services. The majority of the app functions using this class
    FusedLocationProviderClient fusedLocationProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // give each UI variable a value

        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        sw_gps = findViewById(R.id.sw_gps);
        sw_locationupdates = findViewById(R.id.sw_locationsupdates);
        btn_newWaypoint = findViewById(R.id.btn_newWaypoint);
        btn_showWayPointList = findViewById(R.id.btn_showWayPointList);
        tv_wayPointCounts = findViewById(R.id.tv_countOfCrumbs);
        btn_showMap = findViewById(R.id.btn_showMap);

        // set all properties of locationRequest
        locationRequest = new LocationRequest();

        // how often does the default location check occur?
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);

        // how often does the location occur when set to the most  frequent update
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);

        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // event taht is triggered  whenever the update interval is met.
        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                // save the location
                updateUIValues(locationResult.getLastLocation());
            }
        };

        btn_newWaypoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the gps location

                // add the new location to the global list
                MyApplication myApplication = (MyApplication)getApplicationContext();
                savedLocations = myApplication.getMyLocations();
                savedLocations.add(currentLocation);

            }
        });

        btn_showWayPointList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ShowSavedLocationList.class);
                startActivity(i);

            }
        });

        btn_showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,MapsActivity.class);
                startActivity(i);
            }
        });

        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_gps.isChecked()) {
                    //
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Using GPS sensors");

                } else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Using Towers + WIFI");
                }
            }
        });

        sw_locationupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_locationupdates.isChecked()) {
                    // turn on location tracking
                    startLocationUpdates();

                } else {
                    // turn off tracking
                    stopLocationUpdates();
                }
            }
        });

        updateGPS();


    } // end onCreate method

    private void stopLocationUpdates() {

        tv_updates.setText("Location is Not being tracked");
        tv_lat.setText("Not tracking location");
        tv_lon.setText("Not tracking location");
        tv_speed.setText("Not tracking location");
        tv_address.setText("Not tracking location");
        tv_accuracy.setText("Not tracking location");
        tv_altitude.setText("Not tracking location");

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

    }

    private void startLocationUpdates() {

        tv_updates.setText("Location is being tracked");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        updateGPS();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case  PERMISSION_FINE_LOCATION:
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateGPS();
            }
            else {
                Toast.makeText(this, "This app requires permission to be granted in order to work properly ", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        }
    }

    private void updateGPS (){

        // get permissions from the user to track GPS
        // get the current location from the fused client
        // update the UI

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
        {
            //user provided the permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // we got permissions put the values of Location XXX into
                    updateUIValues(location);
                    currentLocation = location;
                }
            });
        }
        else{
            // permissions not granted yet
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);

            }
        }
    }

    private void updateUIValues(Location location) {

        // update all of the text view objects
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        if (location.hasAltitude()){
            tv_altitude.setText(String.valueOf(location.getAltitude()));

        }
        else{
            tv_altitude.setText("Not available");
        }

        if (location.hasSpeed()){
            tv_speed.setText(String.valueOf(location.getSpeed()));
        }
        else{
            tv_speed.setText("Not available");
        }

        Geocoder geocoder = new Geocoder(MainActivity.this);
        try{
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tv_address.setText(addresses.get(0).getAddressLine(0));
        }
        catch (Exception e){

            tv_address.setText("Unable to get street address");


        }

        MyApplication myApplication = (MyApplication)getApplicationContext();
        savedLocations = myApplication.getMyLocations();

        // show the number of waypoints saved.
        tv_wayPointCounts.setText(Integer.toString(savedLocations.size()));
    }

}
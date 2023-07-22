package com.GPSspeed;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class GPSTracker extends Service implements LocationListener {

    private final Context mContext;

    // Flag for GPS status
    boolean isGPSEnabled = false;

    // Flag for network status
    boolean isNetworkEnabled = false;

    // Flag for GPS status
    boolean canGetLocation = false;

    Location location; // Location
    double altitude; // Altitude
    double latitude; // Latitude
    double longitude; // Longitude
    double altitudeAccuracy; // Altitude accuracy
    double speed; // Speed
    double speedAccuracy; // Speed accuracy
    double bearing; // Bearing, orientation
    double bearingAccuracy; // Bearing accuracy

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000; // 1 seconds

    // Declaring a Location Manager
    protected LocationManager locationManager;

    public GPSTracker(Context context, boolean useNetwork) {
        this.mContext = context;
        if(useNetwork){
            getNetworkLocation();
        }else{
            getGpsLocation();
        }
    }

    public Location getGpsLocation(){
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // Getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

                // If GPS enabled, get latitude/longitude using GPS Services
            if (isGPSEnabled) {
                this.canGetLocation = true;
                if (location == null) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("GPS Enabled", "GPS Enabled");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            altitude = location.getAltitude();
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }
    public Location getNetworkLocation(){
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // Getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isNetworkEnabled) {
                this.canGetLocation = true;
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                Log.d("Network", "Network");
                if (locationManager != null) {
                    location = locationManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app.
     * */
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Function to get latitude
     * */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get altitude
     * */
    public double getAltitude(){
        if(location != null){
            altitude = location.getAltitude();
        }

        // return latitude
        return altitude;
    }

    /**
     * Function to get longitude
     * */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to get altitude accuracy
     * */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public double getVerticalAccuracyMeters(){
        if(location != null){
            altitudeAccuracy = location.getVerticalAccuracyMeters();
        }

        // return latitude accuracy
        return altitudeAccuracy;
    }

    /**
     * Function to get speed
     * */
    public double getSpeed(){
        if(location != null){
            speed = location.getSpeed();
        }

        // return speed
        return speed;
    }

    /**
     * Function to get speed accuracy
     * */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public double getSpeedAccuracyMetersPerSecond(){
        if(location != null){
            speedAccuracy = location.getSpeedAccuracyMetersPerSecond();
        }

        // return speed accuracy
        return speedAccuracy;
    }

    /**
     * Function to get bearing
     * */
    public double getBearing(){
        if(location != null){
            bearing = location.getBearing();
        }

        // return bearing
        return bearing;
    }

    /**
     * Function to get bearing accuracy
     * */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public double getBearingAccuracyDegrees(){
        if(location != null){
            bearingAccuracy = location.getBearingAccuracyDegrees();
        }

        // return bearing accuracy
        return bearingAccuracy;
    }

    /**
     * Function to check GPS/Wi-Fi enabled
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog.
     * On pressing the Settings button it will launch Settings Options.
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing the Settings button.
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // On pressing the cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


    @Override
    public void onLocationChanged(Location location) {
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

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
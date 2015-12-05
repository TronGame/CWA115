package cwa115.trongame.Sensor.Location;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Peter on 09/11/2015.
 * The Custom Location Listener
 * This class is an extension of the normal LocationListener
 * It uses the FusedLocationApi to find the location of the device
 *
 * FusedLocationApi is more intelligent than the usual LocationListener as it
 * switches between GPS, network and wifi to get the location.
 */
public class CustomLocationListener implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleApiClient googleApiClient;        // Controls location tracking
    private LocationRequest locationRequest;        // Contains data used by the location listener
    private boolean isLocationTracking = false;     // Is the location listener tracking

    private LocationObserver observer;              // The object that receives the actual location

    public CustomLocationListener(Activity context, LocationObserver observer, int interval, int fastestInterval, int priority) {
        // Create the googleApiClient
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)           // Connect onConnected(&Suspended) function
                .addOnConnectionFailedListener(this)    // Add onConnectionFailed function
                .addApi(LocationServices.API)
                .build();

        // Set the location tracker's settings
        locationRequest = new LocationRequest();                // Store the locationRequest
        locationRequest.setInterval(interval);                  // The update frequency
        locationRequest.setFastestInterval(fastestInterval);    // Tha absolute minimum frequency
        locationRequest.setPriority(priority);                  // The precision of the location

        // Store the observer
        this.observer = observer;
    }

    public void connectGoogleApi() {
        googleApiClient.connect();
    }

    /**
     * Starts listening for location updates.
     */
    public void startLocationUpdate() {
        if(googleApiClient.isConnected() && !isLocationTracking) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, locationRequest, this
            );
            isLocationTracking = true;
        }
    }

    /**
     * Stops listening for location updates.
     */
    public void stopLocationUpdate() {
        if(googleApiClient.isConnected() && isLocationTracking) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            isLocationTracking = false;
        }
    }

    /**
     * Is called when the googleApiClient is connected
     */
    @Override
    public void onConnected(Bundle hint) {
        startLocationUpdate();
    }

    /**
     * Is called when the googleApiClient is no longer connected
     */
    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * Is called when the googleApiClient can't connect
     * @param connectionResult the result of the connection attempt
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("L/ERROR", connectionResult.toString());
    }

    /**
     * Is called when the device's location is changed and sends the new location
     * to the observer
     * @param location the new location of the device
     */
    @Override
    public void onLocationChanged(Location location) {
        observer.updateLocation(location);
    }
}

package cwa115.trongame;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.RoadsApi;
import com.google.maps.model.SnappedPoint;

import java.util.ArrayList;

import cwa115.trongame.Map.Map;
import cwa115.trongame.Map.Player;
import cwa115.trongame.Map.Wall;
import cwa115.trongame.Sensor.SensorDataObservable;
import cwa115.trongame.Sensor.SensorDataObserver;
import cwa115.trongame.Sensor.SensorFlag;
import cwa115.trongame.Utils.LatLngConversion;
import cwa115.trongame.Utils.Vector2D;

public class GameActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, SensorDataObserver, Handler.Callback {

    @Override
    public boolean handleMessage(Message msg) {
        Bundle bundle = msg.getData();
        snappedGpsLoc = new LatLng(bundle.getDouble("lat"), bundle.getDouble("lng"));
        map.updatePlayer(myId, snappedGpsLoc);
        map.updateCamera(snappedGpsLoc);

        // Test wall creation
        if (creatingWall) {
            testWall.addPoint(snappedGpsLoc);
            map.redraw(testWall.getId());
        }
        return false;
    }

    private class DisplayNotification extends AsyncTask<Void, Float, Void> {
        private static final long STEP_COUNT = 10;
        private static final float NO_ALPHA_DECREASE_FRACTION = .25f;
        private String message;
        private long msDelay;

        /**
         * Constructor
         * @param msg the message (notification) to display
         * @param delay delay time in milliseconds
         */
        public DisplayNotification(String msg, long delay) {
            msDelay = delay;
            message = msg;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            long initialWait = Math.round(msDelay * NO_ALPHA_DECREASE_FRACTION);
            long remainingWait = msDelay - initialWait;

            long step = (remainingWait < STEP_COUNT) ? 1 : (remainingWait / STEP_COUNT);
            float alpha = 1;
            float alphaStep = alpha / STEP_COUNT;
            try {
                // Wait without decreasing the alpha for a fraction NO_ALPHA_DECREASE_FRACTION
                Thread.currentThread().sleep(initialWait);

                // Wait while decreasing the alpha
                for(long time = 0; time < remainingWait; time += step) {
                    Thread.currentThread().sleep(step);
                    alpha -= alphaStep;
                    publishProgress(alpha);
                }
            } catch(InterruptedException e) {
                Thread.interrupted();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            findViewById(R.id.notificationFrame).setVisibility(View.VISIBLE);
            TextView notificationText = (TextView)findViewById(R.id.notificationText);
            notificationText.setText(message);
        }

        @Override
        protected void onProgressUpdate(Float... alpha) {
            findViewById(R.id.notificationFrame).setAlpha(alpha[0]);
        }

        @Override
        protected void onPostExecute(Void v) {
            findViewById(R.id.notificationFrame).setVisibility(View.GONE);
        }

    }

    private static final long NOTIFICATION_DISPLAY_TIME = 2500; // In milliseconds
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 0;
    private static final double LOCATION_THRESHOLD = 0;

    private Map map;                                // Controls the map view
    private GoogleApiClient googleApiClient;        // Controls location tracking
    private LatLng gpsLoc;
    private LatLng snappedGpsLoc;
    private PendingResult<SnappedPoint[]> req;

    private boolean creatingWall = false;
    private Wall testWall;

    private LocationRequest locationRequest;        // Contains data used by the location listener
    private boolean isLocationTracking = false;     // Is the location listener tracking

    private String myId;                            // Player id

    private GeoApiContext context;

    private SensorDataObservable sensorDataObservable;// The sensorDataObservable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the view
        setContentView(R.layout.activity_game);

        // Initialize sensorDataObservable and proximityObserver
        sensorDataObservable = new SensorDataObservable(this);
        sensorDataObservable.startSensorTracking(SensorFlag.PROXIMITY, this);

        // Create the googleApiClient
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)           // Connect onConnected(&Suspended) function
                .addOnConnectionFailedListener(this)    // Add onConnectionFailed function
                .addApi(LocationServices.API)
                .build();

        // Set the location tracker's settings
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1500);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Request permissions before doing anything else
        requestPermissions();

        // Setup the map object
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map = new Map(mapFragment);

        context = new GeoApiContext().setApiKey(
                getString(R.string.google_maps_key_server));

        // Add players
        // (this data is normally recieved from main activity)
        myId = "player_1";
        Player[] players = {
                new Player(myId, GameSettings.getPlayerName(), new LatLng(0.0, 0.0))
        };

        for (Player player : players) {
            map.addMapItem(player);         // Add the players to the map object
        }

        snappedGpsLoc = new LatLng(0, 0);
        gpsLoc = new LatLng(0, 0);
    }

    public void createWall(View view) {
        if (!creatingWall) {
            creatingWall = true;
            testWall = new Wall(
                    "test_wall", new LatLng[0], context);
            map.addMapItem(testWall);
            Button button = (Button) view.findViewById(R.id.wallButton);
            button.setText("Stop Creating Wall");
            new DisplayNotification("Wall creation enabled.", NOTIFICATION_DISPLAY_TIME).execute();
        } else {
            creatingWall = false;
            testWall = null;
            Button button = (Button) view.findViewById(R.id.wallButton);
            button.setText("Create Wall");
            new DisplayNotification("Wall creation disabled.", NOTIFICATION_DISPLAY_TIME).execute();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Permission functionality
    // ---------------------------------------------------------------------------------------------

    /**
     * Request permissions at runtime (required for Android 6.0).
     */
    void requestPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                // TODO: Show an explanation
            // } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_FINE_LOCATION
                );
            // }
        } else {
            googleApiClient.connect();
        }
    }

    /**
     * Is called when the permission request is answered
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch(requestCode) {
            case PERMISSIONS_REQUEST_FINE_LOCATION:
                // TODO: check if the permission was granted, and do something when it was not
                googleApiClient.connect();
                break;

            default:
                // Some other permission was granted
        }
    }


    // ---------------------------------------------------------------------------------------------
    // Location Tracking functionality
    // ---------------------------------------------------------------------------------------------

    /**
     * Starts listening for location updates.
     */
    private void startLocationUpdate() {
        if(googleApiClient.isConnected() && !isLocationTracking) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, locationRequest, this
            );
        }
        isLocationTracking = true;
    }

    /**
     * Stops listening for location updates.
     */
    private void stopLocationUpdate() {
        if(googleApiClient.isConnected() && isLocationTracking) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            isLocationTracking = false;
        }
    }

    /**
     * Is called when the application is paused
     */
    public void onPause() {
        super.onPause();
        sensorDataObservable.Pause();
        stopLocationUpdate();
    }

    /**
     * Is called when the application is resumed
     */
    public void onResume() {
        super.onResume();
        sensorDataObservable.Resume();
        if (googleApiClient.isConnected() && !isLocationTracking)
            startLocationUpdate();
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

    }

    /**
     * Is called when the device's location is changed
     * @param location the new location of the device
     */

    @Override
    public void onLocationChanged(Location location) {
        LatLng newGpsLoc = new LatLng(location.getLatitude(), location.getLongitude());
        double distance = new Vector2D(newGpsLoc).subtract(new Vector2D(gpsLoc)).getLength();
        final Handler mainHandler = new Handler(this);

        if (distance >= LOCATION_THRESHOLD) {
            gpsLoc = newGpsLoc;

            if (req != null) {
                req.cancel();
            }

            req = RoadsApi.snapToRoads(
                    context,
                    true,
                    LatLngConversion.getConvertedPoints(
                            new ArrayList<LatLng>() {{add(gpsLoc);}}
                    )
            );

            req.setCallback(new PendingResult.Callback<SnappedPoint[]>() {
                @Override
                public void onResult(SnappedPoint[] result) {
                    LatLng snappedLoc = LatLngConversion.snappedPointsToPoints(result).get(0);

                    Bundle bundle = new Bundle();
                    bundle.putDouble("lat", snappedLoc.latitude);
                    bundle.putDouble("lng", snappedLoc.longitude);
                    Message msg = new Message();
                    msg.setData(bundle);
                    mainHandler.sendMessage(msg);
                }

                @Override
                public void onFailure(Throwable e) {
                    return;
                }
            });
        }
    }


    @Override
    public void update(SensorDataObservable observable, Object data) {
        if(observable != sensorDataObservable)
            return;
        int proximityCount = (int)data;
        findViewById(R.id.wallButton).performClick();
    }

    @Override
    public int getCountLimit() {
        return 1;
    }

    public void hideNotification(View view) {
        view.setVisibility(View.GONE);
    }
}

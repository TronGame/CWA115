package cwa115.trongame;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

    private static final long NOTIFICATION_DISPLAY_TIME = 2500; // In milliseconds
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 0;
    private static final double LOCATION_THRESHOLD = 1e-5;

    private Map map;                                // Controls the map view
    private GoogleApiClient googleApiClient;        // Controls location tracking
    private LatLng gpsLoc;
    private LatLng snappedGpsLoc;
    private PendingResult<SnappedPoint[]> req;
    private Handler mainHandler;

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
        mainHandler = new Handler(this);
    }

    public void createWall(View view) {
        if (!creatingWall) {
            creatingWall = true;
            testWall = new Wall(
                    "test_wall", new LatLng[0], context);
            map.addMapItem(testWall);
            Button button = (Button) view.findViewById(R.id.wallButton);
            button.setText(getString(R.string.wall_button_off_text));
            showNotification(getString(R.string.wall_on_notification), Toast.LENGTH_SHORT);
        } else {
            creatingWall = false;
            testWall = null;
            Button button = (Button) view.findViewById(R.id.wallButton);
            button.setText(getString(R.string.wall_button_on_text));
            showNotification(getString(R.string.wall_off_notification), Toast.LENGTH_SHORT);
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
     * Asynchronously sends a SnapToRoad request.
     * The callback GameActivity.handleMessage will be invoked when the snapped location is
     *  obtained.
     */
    private void snapLocationToRoad() {
        // If req already exists, it is overridden
        // TODO: cancel requests? (Seems to do networking, so it might throw and crash)
        req = RoadsApi.snapToRoads(context, true, LatLngConversion.getConvertedPoint(gpsLoc));

        req.setCallback(new PendingResult.Callback<SnappedPoint[]>() {
            /**
             * Note: this method is *not* executed on the main thread, so it is
             *  not safe to access objects that live on the main thread here
             * (the mainHandler object is guaranteed not be used on the main thread)
             * @param result the points that were snapped to the nearest road
             */
            @Override
            public void onResult(SnappedPoint[] result) {
                LatLng snappedLoc = LatLngConversion.snappedPointsToPoints(result).get(0);

                Message msg = new Message();
                msg.setData(LatLngConversion.getBundleFromPoint(snappedLoc));
                mainHandler.sendMessage(msg);
            }

            @Override
            public void onFailure(Throwable e) {
                return;
            }
        });
    }

    /**
     * Is called when the device's location is changed
     * @param location the new location of the device
     */
    @Override
    public void onLocationChanged(Location location) {
        LatLng newGpsLoc = new LatLng(location.getLatitude(), location.getLongitude());
        double distance = new Vector2D(newGpsLoc).subtract(new Vector2D(gpsLoc)).getLength();

        if (distance >= LOCATION_THRESHOLD) {
            gpsLoc = newGpsLoc;
            snapLocationToRoad();
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

    @Override
    public boolean handleMessage(Message msg) {
        snappedGpsLoc = LatLngConversion.getPointFromBundle(msg.getData());
        map.updatePlayer(myId, snappedGpsLoc);
        map.updateCamera(snappedGpsLoc);

        // Test wall creation
        if (creatingWall) {
            testWall.addPoint(snappedGpsLoc);
            map.redraw(testWall.getId());
        }
        return false;
    }

    public void hideNotification(View view) {
        view.setVisibility(View.GONE);
    }

    private void showNotification(String message, int duration)
    {
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(
                R.layout.notification_frame, (ViewGroup)findViewById(R.id.notificationFrame)
        );
        TextView text = (TextView)layout.findViewById(R.id.notificationText);
        text.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.setMargin(0, 0);
        toast.setDuration(duration);
        toast.setView(layout);

        toast.show();
    }
}

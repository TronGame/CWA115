package cwa115.trongame;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
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

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.RoadsApi;
import com.google.maps.model.SnappedPoint;

import java.util.ArrayList;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.GameEvent.EventUpdateHandler;
import cwa115.trongame.GameEvent.GameEventHandler;
import cwa115.trongame.GoogleMapsApi.ApiListener;
import cwa115.trongame.GoogleMapsApi.SnappedPointHandler;
import cwa115.trongame.Location.CustomLocationListener;
import cwa115.trongame.Location.LocationObserver;
import cwa115.trongame.Map.Map;
import cwa115.trongame.Map.Player;
import cwa115.trongame.Map.Wall;
import cwa115.trongame.Game.GameUpdateHandler;
import cwa115.trongame.Network.SocketIoConnection;
import cwa115.trongame.Sensor.SensorDataObservable;
import cwa115.trongame.Sensor.SensorDataObserver;
import cwa115.trongame.Sensor.SensorFlag;
import cwa115.trongame.Utils.LatLngConversion;
import cwa115.trongame.Utils.Vector2D;

/**
 * The Game activity
 * Controls all of the game functionality
 * implements:
 *      TODO: update this
 *      LocationObserver: ability receive location updates with updateLocation function
 *      SensorDataObserver: ability to receive sensor updates with updateSensor function
 *      ApiListener: ability to receive snapped location results with handleApiResult function
 *      SocketIoHandler: ability to receive information from other players over the socket
 *                          using the onPlayerJoined and onRemoteLocationChange functions
 */
public class GameActivity extends AppCompatActivity implements
        LocationObserver, SensorDataObserver, ApiListener<ArrayList<LatLng>> {

    // region Variables
    // -----------------------------------------------------------------------------------------------------------------
    // Location thresholds
    private static final double LOCATION_THRESHOLD = LatLngConversion.meterToLatLngDistance(10);   // About 10m
    private static final double MAX_ROAD_DISTANCE = LatLngConversion.meterToLatLngDistance(30);    // About 10m
    private static final double MAX_WALL_DISTANCE = LatLngConversion.meterToLatLngDistance(1);    // About 1m

    // Permission request ids
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 0;

    // Main handler objects
    private Map map;                                    // Controls the map view
    private CustomLocationListener locationListener;    // Controls the location tracking
    private SensorDataObservable sensorDataObservable;  // Controls sensor data

    // Location snapping
    private SnappedPointHandler snappedPointHandler;    // Controls location snapping
    private GeoApiContext context;                      // The context that takes care of the location snapping

    // Location data
    private LatLng gpsLoc;                            // Location of player
    private LatLng snappedGpsLoc;                       // Snapped location of player
    private double travelledDistance;                   // Distance travelled from the start
    private double height;                              // The last recorded height of the player

    // Wall data
    private boolean creatingWall = false;               // Is the player creating a wall
    private String wallId;                              // The Wall id

    // Networking
    private SocketIoConnection connection;
    private GameUpdateHandler gameUpdateHandler;
    private GameEventHandler gameEventHandler;

    // region Get Variables
    public double getHeight () {
        return height;
    }
    // endregion

    // endregion

    // region Initialization
    // ---------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Content of Activity
        // -----------------------------------------------------------------------------------------
        setContentView(R.layout.activity_game);

        // Sensor tracking
        // -----------------------------------------------------------------------------------------
        // Initialize sensorDataObservable and proximityObserver
        sensorDataObservable = new SensorDataObservable(this);
        sensorDataObservable.startSensorTracking(SensorFlag.PROXIMITY, this);

        // Location tracking
        // -----------------------------------------------------------------------------------------
        // Initialize the stored locations to 0,0
        snappedGpsLoc = new LatLng(0, 0);
        gpsLoc = new LatLng(0, 0);
        travelledDistance = 0.0;

        // Initialize location listener
        locationListener = new CustomLocationListener(
                this,                                       // The activity that builds the google api
                this,                                       // The LocationObserver
                1500,                                       // Normal update frequency of the location
                500,                                        // Fastest update frequency of the location
                LocationRequest.PRIORITY_HIGH_ACCURACY      // Accuracy of the location
        );

        // Create the context used by the google api (just stores the google key)
        context = new GeoApiContext().setApiKey(
                getString(R.string.google_maps_key_server));

        // Permissions
        // -----------------------------------------------------------------------------------------
        // Request permissions before doing anything else (after initializing the location listener!)
        requestPermissions();

        // Map Object
        // -----------------------------------------------------------------------------------------
        // Create the map object
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map = new Map(mapFragment);

        // Player objects
        // -----------------------------------------------------------------------------------------
        // Create the player
        map.addMapItem(new Player(GameSettings.getPlayerId(), GameSettings.getPlayerName(), new LatLng(0.0, 0.0)));

        // Networking
        // -----------------------------------------------------------------------------------------
        // Create the gameUpdateHandler object
        connection = new SocketIoConnection("testA1", "1");

        gameUpdateHandler = new GameUpdateHandler(connection, map, context);
        gameEventHandler = new GameEventHandler(connection, this);
    }

    // endregion

    // region Game functionality
    // ---------------------------------------------------------------------------------------------

    /**
     * Most of the game functionality happens in this function.
     * It is called when a location update is snapped to the road by the snappedPointHandler
     *
     * @param result The snapped location
     * @return does not matter
     */
    @Override
    public boolean handleApiResult(ArrayList<LatLng> result) {
        // Get the snapped location from the result (result is a list with one item)
        LatLng newSnappedGpsLoc = result.get(0);

        // Calculate the distance between the snapped location and the actual location
        // So this is basically the distance to the road
        double snappedDistance = new Vector2D(gpsLoc).subtract(new Vector2D(snappedGpsLoc)).getLength();

        // Log.d("VALUE", "Snapped Distance "+String.valueOf(snappedDistance));

        // Check is the player is (almost) on the road
        if (snappedDistance < MAX_ROAD_DISTANCE) {
            // Update the player marker and the camera
            map.updatePlayer(GameSettings.getPlayerId(), snappedGpsLoc);
            map.updateCamera(snappedGpsLoc);

            // Calculate the distance from the last location to the new location and show it on the screen
            double distance = 0.0;
            if (!(snappedGpsLoc.longitude == 0 && snappedGpsLoc.latitude == 0)) {
                // This checks if there has been a previous location update
                distance = new Vector2D(snappedGpsLoc).subtract(new Vector2D(newSnappedGpsLoc)).getLength();
            }
            travelledDistance += distance;

            TextView distanceView = (TextView) findViewById(R.id.travelledDistance);
            distanceView.setText(String.valueOf(LatLngConversion.latLngDistanceToMeter(travelledDistance)));

            snappedGpsLoc = newSnappedGpsLoc;   // update the snapped location

            // Send the player location
            gameUpdateHandler.sendMyLocation(snappedGpsLoc);

            // Wall functionality
            Wall[] walls = map.getWalls();
            for (Wall wall : walls) {
                // Check if the player isn't to close to a wall
                double distanceToWall = wall.getDistanceTo(snappedGpsLoc);
                if (distanceToWall < MAX_WALL_DISTANCE) {
                    // Show the "player to close to wall" notification
                    showNotification(getString(R.string.wall_too_close), Toast.LENGTH_LONG);
                }
            }

            Wall wall = (Wall)map.getItemById(wallId);
            // Update the wall (this must happen after the "player to close to wall" check
            if (creatingWall) {
                wall.addPoint(snappedGpsLoc);   // Add the new point to the wall
                gameUpdateHandler.sendUpdateWall(snappedGpsLoc, wallId);
                map.redraw(wall.getId());       // Redraw the wall on the map
            }

        } else {
            // Player is to far from the road
            map.updatePlayer(GameSettings.getPlayerId(), gpsLoc);     // Draw the player on the actual location instead
            map.updateCamera(gpsLoc);           // Zoom to there as well

            // Send the player location
            gameUpdateHandler.sendMyLocation(gpsLoc);

            // Show the "player to far from road" notification
            showNotification(getString(R.string.road_too_far), Toast.LENGTH_LONG);
        }

        return false;   // This is used by the snappedPointHandler
    }

    /**
     * Called when the start/stop wall button is pressed
     * or when the proximity sensor detects something
     *
     * @param view parameter passed by the button
     */
    public void createWall(View view) {
        // Is the player currently creating a wall
        if (!creatingWall) {
            // Start creating a wall
            creatingWall = true;                        // The player is now creating a wall
            // Create the wall object
            Wall wall = new Wall(
                    "W" + GameSettings.generateUniqueId(),
                    GameSettings.getPlayerId(),
                    GameSettings.getWallColor(),
                    context);
            wallId = wall.getId();                      // Store the wall id
            map.addMapItem(wall);                       // Add the wall to the map

            // Tell the other devices that a new wall has been created
            gameUpdateHandler.sendCreateWall(wallId, new ArrayList<LatLng>(), GameSettings.getWallColor());

            // Update the button
            Button button = (Button) view.findViewById(R.id.wallButton);
            button.setText(getString(R.string.wall_button_off_text));

            // Show the "creating wall" notification
            showNotification(getString(R.string.wall_on_notification), Toast.LENGTH_SHORT);
        } else {
            // Stop creating the wall
            creatingWall = false;                       // The player is no longer creating a wall
            wallId = null;                              // Forget about the current wall

            // Update the button
            Button button = (Button) view.findViewById(R.id.wallButton);
            button.setText(getString(R.string.wall_button_on_text));

            // Show the "stopped creating wall" notification
            showNotification(getString(R.string.wall_off_notification), Toast.LENGTH_SHORT);
        }
    }

    /**
     * Called when the clear wall button is pressed
     * @param view
     */
    public void clearWall(View view) {
        // Is there a wall right now?
        if (wallId != null) {
            map.clear(wallId);    // Clear the wall from the map
        }
    }

    public void addScore(int score) {
        travelledDistance += score;
    }

    // endregion

    // region Permission handling
    // ---------------------------------------------------------------------------------------------

    /**
     * Request permissions at runtime (required for Android 6.0).
     */
    void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
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
            locationListener.connectGoogleApi();
        }
    }

    /**
     * Is called when the permission request is answered
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_FINE_LOCATION:
                // TODO: check if the permission was granted, and do something when it was not
                locationListener.connectGoogleApi();
                break;

            default:
                // Some other permission was granted
        }
    }

    // endregion

    // region Application handling
    // ---------------------------------------------------------------------------------------------

    /**
     * Is called when the application is paused
     */
    public void onPause() {
        super.onPause();
        sensorDataObservable.Pause();           // Pauses the sensor observer
        locationListener.stopLocationUpdate();  // Pauses the lcoation listener
    }

    /**
     * Is called when the application is resumed
     */
    public void onResume() {
        super.onResume();
        sensorDataObservable.Resume();          // Resume the sensor observer
        locationListener.startLocationUpdate(); // Start the location listener again
    }

    // endregion    

    // region Sensors and Gps tracking
    // ---------------------------------------------------------------------------------------------

    /**
     * Is called when the device's location is changed (called by CustomLocationListener)
     *
     * @param location the new location of the device
     */
    @Override
    public void updateLocation(Location location) {
        // Store the new location in a LatLng object
        LatLng newGpsLoc = new LatLng(location.getLatitude(), location.getLongitude());
        // Calculate the distance between the new location and the last location
        double distance = new Vector2D(newGpsLoc).subtract(new Vector2D(gpsLoc)).getLength();

        // Log.d("VALUE", "Distance " + String.valueOf(distance));

        // If the location is to close to the last location nothing needs to happen
        // This decreases the amount of work the application needs to do
        if (distance >= LOCATION_THRESHOLD) {
            // Store the new location
            gpsLoc = newGpsLoc;
            height = location.getAltitude();

            // Create a PendingResult object
            // This is used by the snappedPointHandler to snap the provided points to the road
            PendingResult<SnappedPoint[]> req = RoadsApi.snapToRoads(
                    context,                                    // The context (basically the api key used by google
                    true,                                       // Interpolate the points (add new points to smooth the line
                    LatLngConversion.getConvertedPoint(gpsLoc)  // The points that need to be snapped (these need to be converted to a different type of LatLng
            );
            // Check if the snappedPointHandler is still busy with a previous request
            if (snappedPointHandler != null && !snappedPointHandler.isFinished())
                snappedPointHandler.stop(); // Cancel the previous request

            // Create a new snappedPointHandler to take care of the request
            snappedPointHandler = new SnappedPointHandler(req, this);
        }
    }

    /**
     * Is called when the proximity sensor detects something
     *
     * @param observable The observable that has changed
     * @param data       Extra data attached by observable
     */
    @Override
    public void updateSensor(SensorDataObservable observable, Object data) {
        // Check whether it was the proximity sensor that detected something
        if (observable != sensorDataObservable)
            return;

        int proximityCount = (int) data;
        findViewById(R.id.wallButton).performClick();       // Press the Start/Stop wall button
    }

    /**
     * The amount of times the sensor can detect useful information before updateSensor is called
     */
    @Override
    public int getCountLimit() {
        return 1;
    }

    // endregion

    // region Notification handling
    // ---------------------------------------------------------------------------------------------

    /**
     * Show a notification (a toast object) on the screen
     *
     * @param message  the message shown in the notification
     * @param duration the time that the notification stays visible
     */
    public void showNotification(String message, int duration) {
        // Create the layout used in the notification
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(
                R.layout.notification_frame, (ViewGroup) findViewById(R.id.notificationFrame)
        );
        // Set the text to the message string
        TextView text = (TextView) layout.findViewById(R.id.notificationText);
        text.setText(message);

        // Create the toast
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 0);     // Lock it to the bottom of the screen
        toast.setMargin(0, 0);                      // Set the margin to 0 (doesn't work for some reason
        toast.setDuration(duration);                // Set the duration to the correct value
        toast.setView(layout);                      // Insert the previously created layout

        // Show the notification
        toast.show();
    }

    // endregion

}

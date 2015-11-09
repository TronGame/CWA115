package cwa115.trongame;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cwa115.trongame.GoogleMapsApi.ApiListener;
import cwa115.trongame.GoogleMapsApi.SnappedPointHandler;
import cwa115.trongame.Location.CustomLocationListener;
import cwa115.trongame.Location.LocationObserver;
import cwa115.trongame.Map.Map;
import cwa115.trongame.Map.Player;
import cwa115.trongame.Map.Wall;
import cwa115.trongame.Network.SocketIoConnection;
import cwa115.trongame.Network.SocketIoHandler;
import cwa115.trongame.Sensor.SensorDataObservable;
import cwa115.trongame.Sensor.SensorDataObserver;
import cwa115.trongame.Utils.LatLngConversion;
import cwa115.trongame.Utils.Vector2D;

/**
 * The Game activity
 * Controls all of the game functionality
 * implements:
 *      LocationObserver: ability receive location updates with updateLocation function
 *      SensorDataObserver: ability to receive sensor updates with updateSensor function
 *      ApiListener: ability to receive snapped location results with handleApiResult function
 *      SocketIoHandler: TODO add this
 */
public class GameActivity extends AppCompatActivity implements
        LocationObserver, SensorDataObserver, ApiListener<ArrayList<LatLng>>, SocketIoHandler {

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
    private LatLng gpsLoc;                              // Location of player
    private LatLng snappedGpsLoc;                       // Snapped location of player
    private double travelledDistance;                   // Distance travelled from the start

    // Wall data
    private boolean creatingWall = false;               // Is the player creating a wall
    private Wall wall;                              // The Wall object

    // Player id
    private String myId;

    // Socket IO connection
    private SocketIoConnection socket;

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

        // Networking
        // -----------------------------------------------------------------------------------------
        // Create the socket connection object
        socket = new SocketIoConnection("testA1", "1", this);

        // Map Object
        // -----------------------------------------------------------------------------------------
        // Create the map object
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map = new Map(mapFragment);

        // Player objects
        // -----------------------------------------------------------------------------------------
        // Store the player id
        myId = "P" + GameSettings.generateUniqueId();

        // Create the player objects
        Player[] players = {
                new Player(myId, GameSettings.getPlayerName(), new LatLng(0.0, 0.0))
        };

        // Draw the players on the map
        for (Player player : players) {
            map.addMapItem(player);         // Add the players to the map object
        }
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

        // Send the player location
        sendMyLocation(snappedGpsLoc);

        // Check is the player is (almost) on the road
        if (snappedDistance < MAX_ROAD_DISTANCE) {
            // Update the player marker and the camera
            map.updatePlayer(myId, snappedGpsLoc);
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

            // Wall functionality
            if (wall != null) {
                // Check if the player isn't to close to a wall
                double distanceToWall = wall.getDistanceTo(snappedGpsLoc);
                if (distanceToWall < MAX_WALL_DISTANCE) {
                    // Show the "player to close to wall" notification
                    showNotification(getString(R.string.wall_too_close), Toast.LENGTH_LONG);
                }

                // Update the wall (this must happen after the "player to close to wall" check
                if (creatingWall) {
                    wall.addPoint(snappedGpsLoc);   // Add the new point to the wall
                    sendUpdateWall(snappedGpsLoc);
                    map.redraw(wall.getId());       // Redraw the wall on the map
                }
            }

        } else {
            // Player is to far from the road
            map.updatePlayer(myId, gpsLoc);     // Draw the player on the actual location instead
            map.updateCamera(gpsLoc);           // Zoom to there as well

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
            wall = new Wall("W" + GameSettings.generateUniqueId(), myId, new LatLng[0], context);
            map.addMapItem(wall);                       // Add the wall to the map

            // Update the button
            Button button = (Button) view.findViewById(R.id.wallButton);
            button.setText(getString(R.string.wall_button_off_text));

            // Show the "creating wall" notification
            showNotification(getString(R.string.wall_on_notification), Toast.LENGTH_SHORT);
        } else {
            // Stop creating the wall
            creatingWall = false;                       // The player is no longer creating a wall
            wall = null;                                // Destroy the wall object

            // Update the button
            Button button = (Button) view.findViewById(R.id.wallButton);
            button.setText(getString(R.string.wall_button_on_text));

            // Show the "stopped creating wall" notification
            showNotification(getString(R.string.wall_off_notification), Toast.LENGTH_SHORT);
        }
    }

    /**
     * Called when the clear wall button is pressed
     *
     * @param view
     */
    public void clearWall(View view) {
        // Is there a wall right now?
        if (wall != null) {
            map.clear(wall.getId());    // Clear the wall from the map
        }
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
        sendJoinMessage();                      // TODO check if this is the correct spot for this
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

    // region Networking
    // ---------------------------------------------------------------------------------------------

    /**
     * Send the current player location
     *
     * @param location the location of the player
     */
    private void sendMyLocation(LatLng location) {
        // Create the message that will be send over the socket connection
        JSONObject locationMessage = new JSONObject();
        try {
            locationMessage.put("playerId", myId);
            locationMessage.put("location", LatLngConversion.getJSONFromPoint(location));
        } catch (JSONException e) {
            // end of the world
        }

        // Send the message over the socket
        socket.sendMessage(locationMessage, "updatePosition");
    }

    /**
     * Tell the other players that you joined
     */
    private void sendJoinMessage() {
        // Create the message that will be send over the socket connection
        JSONObject joinMessage = new JSONObject();
        try {
            joinMessage.put("playerId", myId);
            joinMessage.put("playerName", GameSettings.getPlayerName());
        } catch (JSONException e) {
            // end of the world
        }

        // Send the message over the socket
        socket.sendMessage(joinMessage, "join");
    }

    /**
     * Send an extra wall point to the other players.
     * @param point the point to be remotely added to the wall
     */
    private void sendUpdateWall(LatLng point) {
        JSONObject updateWallMessage = new JSONObject();
        try {
            updateWallMessage.put("playerId", myId);
            updateWallMessage.put("wallId", wall.getId());
            updateWallMessage.put("point", LatLngConversion.getJSONFromPoint(point));
        } catch(JSONException e) {
            // end of the world
        }
        socket.sendMessage(updateWallMessage, "updateWall");
    }

    /**
     * Add a player that joined to the map
     *
     * @param playerId   The id of the joined player
     * @param playerName The name of the joined player
     */
    @Override
    public void onPlayerJoined(String playerId, String playerName) {
        // Check if the message we received originates from ourselves
        if (myId.equals(playerId))
            return;

        // Check if the player hasn't been added
        if (!map.hasObject(playerId))
            // Add the player
            map.addMapItem(new Player(playerId, GameSettings.getPlayerName(), new LatLng(0, 0)));

        Log.d("SERVER", "Player joined: " + playerId);
    }

    /**
     * Change the location of another player
     *
     * @param playerId The id of the updated player
     * @param location The location of the updated player
     */
    @Override
    public void onRemoteLocationChange(String playerId, LatLng location) {
        // Check if the message we received originates from ourselves
        if (myId.equals(playerId))
            return;

        // Check if the player has joined
        if (map.hasObject(playerId))
            // Update the location of the player
            map.updatePlayer(playerId, location);

        Log.d("SERVER", "Location of " + playerId + " updated to " + location.toString());
    }

    /**
     * Called when a remote wall is extended by one point, or created
     * @param playerId the player identifier of the wall owner
     * @param wallId the identifier of the wall
     * @param point the newly added point
     */
    @Override
    public void onRemoteWallUpdate(String playerId, String wallId, LatLng point) {
        if(myId.equals(playerId))
            return; // We sent this ourselves
        if(!map.hasObject(wallId)) {
            map.addMapItem(new Wall(wallId, playerId, new LatLng[]{point}, context));
            Log.d("SERVER", "New wall created by " + playerId + " at " + point.toString());
        } else {
            Wall remoteWall = (Wall)map.getItemById(wallId);
            if(!remoteWall.getOwnerId().equals(playerId))
                return;
            remoteWall.addPoint(point);
            Log.d("SERVER", "Update wall " + wallId + " of " + playerId + " by " + point.toString());
            map.redraw(remoteWall.getId());
        }
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
    private void showNotification(String message, int duration) {
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

package cwa115.trongame;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.ImmutableMap;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.RoadsApi;
import com.google.maps.model.SnappedPoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cwa115.trongame.Game.GameEvent.Events.BellEvent;
import cwa115.trongame.Game.GameEvent.Events.GameEvent;
import cwa115.trongame.Game.GameEvent.Events.KingOfHillEvent;
import cwa115.trongame.Game.GameEvent.Events.ShowOffEvent;
import cwa115.trongame.Game.GameEvent.Events.TurnEvent;
import cwa115.trongame.Game.GameEvent.GameEventHandler;
import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.Game.GameUpdateHandler;
import cwa115.trongame.Game.Map.Map;
import cwa115.trongame.Game.Map.Player;
import cwa115.trongame.Game.Map.Wall;
import cwa115.trongame.Network.GoogleMapsApi.ApiListener;
import cwa115.trongame.Network.GoogleMapsApi.SnappedPointHandler;
import cwa115.trongame.Network.Server.HttpConnector;
import cwa115.trongame.Network.Server.ServerCommand;
import cwa115.trongame.Network.Socket.SocketIoConnection;
import cwa115.trongame.Sensor.Accelerometer.HorizontalAccelerationDataHolder;
import cwa115.trongame.Sensor.Frequency.FrequencyListener;
import cwa115.trongame.Sensor.Gyroscope.GyroscopeDataHolder;
import cwa115.trongame.Sensor.Location.CustomLocationListener;
import cwa115.trongame.Sensor.Location.LocationObserver;
import cwa115.trongame.Sensor.SensorDataObservable;
import cwa115.trongame.Sensor.SensorDataObserver;
import cwa115.trongame.Sensor.SensorFlag;
import cwa115.trongame.Utils.LatLngConversion;

/**
 * The Game activity
 * Controls all of the game functionality
 */
public class GameActivity extends AppCompatActivity implements
        LocationObserver, ApiListener<ArrayList<LatLng>> {

    // region Variables
    // -----------------------------------------------------------------------------------------------------------------
    // General Settings
    private static final int FINAL_SCORE_TIMEOUT = 5;   // in seconds
    private static final boolean IMMORTAL = false;
    private static final boolean HAS_EVENTS = true;
    private static final int KILL_SCORE = 2000;
    private static final int WALL_BREAKER_COST = 500;

    // Location thresholds
    private static final double IGNORE_ACCURACY = 70; // This should be equal or less to the largest value of the measured distances
    private static final double LOCATION_THRESHOLD = LatLngConversion.meterToLatLngDistance(20);
    private static final double MAX_ROAD_DISTANCE = LatLngConversion.meterToLatLngDistance(70);
    private static final double MIN_WALL_DISTANCE = LatLngConversion.meterToLatLngDistance(30);
    private static final double MIN_WALL_WARNING_DISTANCE = LatLngConversion.meterToLatLngDistance(50);
    private static final double IGNORE_WALL_DISTANCE = LatLngConversion.meterToLatLngDistance(50);
    private static final double WALL_DELAY_DISTANCE = LatLngConversion.meterToLatLngDistance(300);
    // Only this one is in meter because the distance from the center is calculated in meters
    // instead of in latlng distance
    private static final double WARNING_DISTANCE_TO_WALL = 300;

    // Permission request ids
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 0;

    // Main handler objects
    private Map map;                                    // Controls the map view
    private CustomLocationListener locationListener;    // Controls the location tracking
    private SensorDataObservable sensorDataObservable;  // Controls sensor data
    private FrequencyListener frequencyListener;        // Controls sound detection

    // Location snapping
    private SnappedPointHandler snappedPointHandler;    // Controls location snapping
    private GeoApiContext context;                      // The context that takes care of the location snapping

    // Location data
    private LatLng gpsLoc;                              // Location of player
    private LatLng snappedGpsLoc;                       // Snapped location of player
    private double travelledDistance;                   // Distance travelled from the start
    private double height;                              // The last recorded height of the player
    private LatLng mapCenter;                           // The center of the map

    // Sensor data
    private double acceleration;                        // Cumulative acceleration
    private int accelerationCount;                      // Amount of acceleration measurements
    private boolean isBellRinging;                      // Indicates the state of the bell
    private int bellCount;                              // Stores the amount of times a bell was detected
    private int turns;                                  // Stores the amount of turns
    private GameEvent currentEvent;                     // Stores the current event

    // Wall data
    private double holeSize = LatLngConversion.meterToLatLngDistance(50);
    private boolean creatingWall = false;               // Is the player creating a wall
    private String wallId;                              // The Wall id

    // Game data
    private String winner;
    public boolean isAlive;
    private boolean hasEnded;
    private int playersAliveCount;
    private HashMap<String, Double> playerScores;

    // Networking
    private SocketIoConnection connection;
    private GameUpdateHandler gameUpdateHandler;
    private GameEventHandler gameEventHandler;
    private HttpConnector dataServer;
    // Timing
    private long startTime;
    private long endTime;
    private Handler countdown;
    private Runnable countdownRunnable;

    // region Get Variables
    public double getHeight () {
        return height;
    }

    /**
     * @return the average acceleration since the last reset
     */
    public double getAcceleration() {
        return acceleration / accelerationCount;
    }

    public int getTurns(){ return turns;}

    public void resetTurns(){
        turns = 0;
        sensorDataObservable.resetSensorData(SensorFlag.GYROSCOPE);
    }

    /**
     * Resets the cumulative acceleration and the measurement count.
     */
    public void resetAcceleration() {
        acceleration = 0;
        accelerationCount = 0;
    }

    public int getBellCount() {
        return bellCount;
    }

    public void setBellCount(int i) {
        bellCount = i;
    }

    public double getScore() {
        return travelledDistance;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public void addScore(int score) {
        travelledDistance += score;

        TextView distanceView = (TextView) findViewById(R.id.travelledDistance);
        distanceView.setText(String.valueOf(Math.floor(travelledDistance)));
    }

    public void addScore(double score) {
        travelledDistance += score;

        TextView distanceView = (TextView) findViewById(R.id.travelledDistance);
        distanceView.setText(String.valueOf(Math.floor(travelledDistance)));
    }

    public void setCurrentEvent(GameEvent event){
        this.currentEvent = event;
        findViewById(R.id.eventContainer).setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.eventValue)).setText("");
    }
    public void stopCurrentEvent(){
        this.currentEvent = null;
        findViewById(R.id.eventContainer).setVisibility(View.GONE);
    }

    // endregion

    // endregion

    // region Initialization
    // ---------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            // If the application closed accidentally the player is set to spectator mode
            // At this point the game is corrupt and a lot of problems will arise
            // If this happens to the host it is extremely problematic
            // TODO make the host end the game if his game is corrupt
            GameSettings.loadFromBundle(savedInstanceState);
            GameSettings.setSpectate(true);
            Toast.makeText(this, getString(R.string.game_activity_closed), Toast.LENGTH_SHORT).show();
        }

        // Content of Activity
        // -----------------------------------------------------------------------------------------
        setContentView(R.layout.activity_game);

        // Sensor tracking
        // -----------------------------------------------------------------------------------------
        // Initialize sensorDataObservable and proximityObserver
        acceleration = 0;
        accelerationCount = 0;
        turns = 0;
        sensorDataObservable = new SensorDataObservable(this);
        isBellRinging = false;

        // Location tracking
        // -----------------------------------------------------------------------------------------
        // Initialize the stored locations to 0,0
        snappedGpsLoc = new LatLng(0, 0);
        gpsLoc = new LatLng(0, 0);
        if (savedInstanceState != null)
            travelledDistance = savedInstanceState.getDouble("travelledDistance", 0.0);
        else
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
        connection = new SocketIoConnection("testA1", String.valueOf(GameSettings.getGameId()));

        gameUpdateHandler = new GameUpdateHandler(this, connection, map, context);
        gameEventHandler = new GameEventHandler(connection, this);

        if (savedInstanceState != null)
            // When the game is corrupt, you are considered dead.
            gameUpdateHandler.sendDeathMessage("//", "A corrupted game");

        // Create the data server
        dataServer = new HttpConnector(getString(R.string.dataserver_url));

        // Setup the game ui
        if (!GameSettings.isOwner()) {
            Button startButton = (Button) findViewById(R.id.start_game_button);
            startButton.setVisibility(View.GONE);
        }

        Button wallBreaker = (Button) findViewById(R.id.breakWallButton);
        wallBreaker.setVisibility(View.GONE);

        LinearLayout travelledDistanceContainer = (LinearLayout) findViewById(R.id.travelledDistanceContainer);
        travelledDistanceContainer.setVisibility(View.GONE);

        TextView travelledDistance = (TextView) findViewById(R.id.travelledDistance);
        travelledDistance.setVisibility(View.GONE);

        TextView travelledDistanceHead = (TextView) findViewById(R.id.travelledDistanceHead);
        travelledDistanceHead.setVisibility(View.GONE);

        findViewById(R.id.eventContainer).setVisibility(View.GONE);
        findViewById(R.id.countdown).setVisibility(View.GONE);
    }

    public void onStartGame(View view) {
        view.setVisibility(View.GONE);
        startGame(gpsLoc);
    }

    public void startGame(LatLng startPos) {
        // Tell the other players that the game has started
        if (GameSettings.isOwner())
            gameUpdateHandler.sendStartGame(startPos);

        // Start sensor tracking
        // Listen to proximity updates
        sensorDataObservable.startSensorTracking(SensorFlag.PROXIMITY, new SensorDataObserver() {
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

                sensorDataObservable.resetSensorData(SensorFlag.PROXIMITY);// This was moved from within SensorDataObservable
                if (GameSettings.getCanBreakWall())
                    breakWall(null);       // Activiate break wall button
            }

            /**
             * The amount of times the sensor can detect useful information before updateSensor is called
             */
            @Override
            public int getCountLimit() {
                return 1;
            }
        });
        // Listen to accelerometer updates
        sensorDataObservable.startSensorTracking(SensorFlag.ACCELEROMETER, new SensorDataObserver() {
            @Override
            /**
             * Is called when the accelerometer sensor detects something
             *
             * @param observable The observable that has changed
             * @param data       Extra data attached by observable
             */
            public void updateSensor(SensorDataObservable observable, Object data) {
                // Check whether it was the proximity sensor that detected something
                if (observable != sensorDataObservable)
                    return;

                if(data instanceof HorizontalAccelerationDataHolder) {
                    HorizontalAccelerationDataHolder holder = (HorizontalAccelerationDataHolder) data;
                    acceleration += holder.getAccelerationMagnitude();
                    accelerationCount += 1;
                }
                if(currentEvent!=null && currentEvent instanceof ShowOffEvent){
                    ((TextView)findViewById(R.id.eventValue)).setText(getString(R.string.show_off_event_text).replaceAll("%value",""+Math.floor(acceleration)));
                }
            }

            /**
             * The amount of times the sensor can detect useful information before updateSensor is called
             */
            @Override
            public int getCountLimit() {
                return -1;
            }
        });
        // Listen to gyroscope
        sensorDataObservable.startSensorTracking(SensorFlag.GYROSCOPE, new SensorDataObserver() {
            @Override
            public void updateSensor(SensorDataObservable observable, Object data) {
                // Check whether it was the proximity sensor that detected something
                if (observable != sensorDataObservable)
                    return;

                if(data instanceof GyroscopeDataHolder) {
                    GyroscopeDataHolder holder = (GyroscopeDataHolder) data;
                    turns = holder.getCount();
                }
                if(currentEvent!=null && currentEvent instanceof TurnEvent){
                    ((TextView)findViewById(R.id.eventValue)).setText(getString(R.string.turn_event_text).replaceAll("%value",""+Math.floor(turns)));
                }
            }

            @Override
            public int getCountLimit() {
                return 1;
            }
        });

        // Listen to microphone
        frequencyListener = new FrequencyListener(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                handleBellDetected();
                return false;
            }
        }), 44100, 1650, 2450, 16384);
        frequencyListener.run(); // Start the FrequencyListener

        // Set the correct buttons to visible
        if (!GameSettings.getSpectate()) {
            if (GameSettings.getCanBreakWall()) {
                // TODO hide the breakWall button when there is a proximity sensor
                Button wallBreaker = (Button) findViewById(R.id.breakWallButton);
                wallBreaker.setVisibility(View.VISIBLE);
            }

            LinearLayout travelledDistanceContainer = (LinearLayout) findViewById(R.id.travelledDistanceContainer);
            travelledDistanceContainer.setVisibility(View.VISIBLE);

            TextView travelledDistance = (TextView) findViewById(R.id.travelledDistance);
            travelledDistance.setVisibility(View.VISIBLE);

            TextView travelledDistanceHead = (TextView) findViewById(R.id.travelledDistanceHead);
            travelledDistanceHead.setVisibility(View.VISIBLE);
        }

        // Set start time (in order to measure total playtime)
        startTime = System.currentTimeMillis();
        if (GameSettings.isOwner())
            // Set the amount of players that are alive
            playersAliveCount = GameSettings.getPlayersInGame().size();

        // Set the initial location
        mapCenter = startPos;
        if (GameSettings.getMaxDistance() > 0)
            // Draw the border on the map
            // The distance is GameSettings.getMaxDistance() - WARNING_DISTANCE_TO_WALL so that the
            // player won't die right away after crossing the wall on the map.
            map.drawBorder(mapCenter, GameSettings.getMaxDistanceInMeters());

        // Activate end game timer
        if(GameSettings.getTimeLimit()>=0){
            if(GameSettings.isOwner()) {
                // End the game in FINAL_SCORE_TIMEOUT seconds
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        endGame();
                    }
                }, 1000 * GameSettings.getTimeLimit() * 60);
            }
            // Update countdown
            endTime = startTime + 1000 * GameSettings.getTimeLimit() * 60;
            countdown = new Handler();
            countdownRunnable = new Runnable() {
                @Override
                public void run() {
                    TextView countdownText = (TextView)findViewById(R.id.countdown);
                    if(endTime > System.currentTimeMillis()) {
                        countdownText.setText(formatTime(endTime - System.currentTimeMillis()));
                        countdown.postDelayed(this, 1000);
                    }else{
                        countdownText.setText(formatTime(0));
                        countdownText.setVisibility(View.GONE);
                    }
                }
            };
            countdown.postDelayed(countdownRunnable, 1000);
            findViewById(R.id.countdown).setVisibility(View.VISIBLE);
        }

        // Start the events
        if (GameSettings.isOwner() && HAS_EVENTS)
            gameEventHandler.start();

        // Set the player to alive
        if (!GameSettings.getSpectate())
            isAlive = true;
        else
            isAlive = false;

        hasEnded = false;

        showNotification(getString(R.string.game_started), Toast.LENGTH_SHORT);
    }

    // endregion

    // region Game functionality
    // ---------------------------------------------------------------------------------------------
    // region Game Updates
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
        double snappedDistance = LatLngConversion.getDistancePoints(gpsLoc, newSnappedGpsLoc);

        // Log.d("VALUE", "Snapped Distance "+String.valueOf(snappedDistance));

        if (isAlive && mapCenter != null && GameSettings.getMaxDistance() > 0) {
            if (LatLngConversion.getAccurateDistancePoints(mapCenter, newSnappedGpsLoc) > GameSettings.getMaxDistanceInMeters()) {
                showNotification(getString(R.string.crossed_border), Toast.LENGTH_SHORT);
                onDeath("//", "Map Border");
            }

            if (LatLngConversion.getAccurateDistancePoints(mapCenter, newSnappedGpsLoc) > GameSettings.getMaxDistanceInMeters() - WARNING_DISTANCE_TO_WALL) {
                showNotification(getString(R.string.close_to_border), Toast.LENGTH_SHORT);
            }
        }

        // Check is the player is (almost) on the road
        if (snappedDistance < MAX_ROAD_DISTANCE) {
            // Update the player marker and the camera
            map.updatePlayer(GameSettings.getPlayerId(), newSnappedGpsLoc);
            map.updateCamera(newSnappedGpsLoc);

            // Send the player location
            gameUpdateHandler.sendMyLocation(newSnappedGpsLoc);

            // If the player is alive the interactions with the wall can be checked
            //      Add the travelled distance to the score
            //      Check if the player is to close to or has crossed a wall
            //      Update the wall
            if (isAlive) {
                // Add the travelled distance to the score
                // ---------------------------------------
                double distance = 0.0;
                if (!(snappedGpsLoc.longitude == 0 && snappedGpsLoc.latitude == 0)) {
                    // This checks if there has been a previous location update
                    distance = LatLngConversion.getDistancePoints(snappedGpsLoc, newSnappedGpsLoc);
                }

                addScore(LatLngConversion.latLngDistanceToMeter(distance));
                if (!creatingWall && travelledDistance >= WALL_DELAY_DISTANCE)
                    createWall();

                // Check if the player is to close to or has crossed a wall
                // ---------------------------------------------------------
                ArrayList<Wall> walls = map.getWalls();
                for (Wall wall : walls) {
                    // Check if the player hasn't crossed a wall
                    if (wall.hasCrossed(snappedGpsLoc, newSnappedGpsLoc, MIN_WALL_DISTANCE, IGNORE_WALL_DISTANCE, GameSettings.getPlayerId())) {
                        // The player has crossed the wall and has therefore died
                        showNotification(getString(R.string.wall_crossed), Toast.LENGTH_SHORT);
                        Player killer = (Player)map.getItemById(wall.getOwnerId());
                        String killerName = "";
                        if (killer != null)
                            killerName = killer.getName();

                        onDeath(wall.getOwnerId(), killerName);
                    } else {
                        // Check if the player isn't to close to a wall
                        if (wall.getDistanceTo(newSnappedGpsLoc, MIN_WALL_WARNING_DISTANCE, GameSettings.getPlayerId()) < MIN_WALL_WARNING_DISTANCE) {
                            // Show the "player to close to wall" notification
                            showNotification(getString(R.string.wall_too_close), Toast.LENGTH_SHORT);
                        }
                    }
                }

                // Update the wall
                // ---------------
                Wall wall = (Wall) map.getItemById(wallId);
                // Update the wall (this must happen after the "player to close to wall" check
                if (creatingWall) {
                    wall.addPoint(newSnappedGpsLoc);   // Add the new point to the wall
                    gameUpdateHandler.sendUpdateWall(newSnappedGpsLoc, wallId);
                    map.redraw(wall.getId());       // Redraw the wall on the map
                }
            }

            snappedGpsLoc = newSnappedGpsLoc;   // update the snapped location

        } else {
            // Player is to far from the road
            map.updatePlayer(GameSettings.getPlayerId(), gpsLoc);       // Draw the player on the actual location instead
            map.updateCamera(gpsLoc);                                   // Zoom to there as well

            // Send the player location
            gameUpdateHandler.sendMyLocation(gpsLoc);

            if (isAlive) {
                // Show the "player to far from road" notification
                showNotification(getString(R.string.road_too_far), Toast.LENGTH_SHORT);
                onDeath("", "");
            }
        }

        return false;   // This is used by the snappedPointHandler
    }
    // endregion

    // region Wall Controls
    /**
     * Called when the start/stop wall button is pressed
     * or when the proximity sensor detects something
     */
    public void createWall() {
        if (!isAlive)
            return;

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
            gameUpdateHandler.sendCreateWall(GameSettings.getPlayerId(), wallId, new ArrayList<LatLng>(), GameSettings.getWallColor());

            // Show the "creating wall" notification
            showNotification(getString(R.string.wall_on_notification), Toast.LENGTH_SHORT);
        }
        // The wall is never turned off
        // else {
        //     // Stop creating the wall
        //     creatingWall = false;                       // The player is no longer creating a wall
        //     wallId = null;                              // Forget about the current wall
        //
        //     // Show the "stopped creating wall" notification
        //     showNotification(getString(R.string.wall_off_notification), Toast.LENGTH_SHORT);
        // }
    }

    /**
     * Called when the clear wall button is pressed
     * TODO remove this as it isn't used
     */
    public void clearWall() {
        // Is there a wall right now?
        if (wallId != null) {
            map.clear(wallId);    // Clear the wall from the map
            gameUpdateHandler.sendRemoveWall(wallId);
        }
    }

    public void breakWall(View view) {
        // This should never be true !
        if (!isAlive)
            return;

        if (travelledDistance > WALL_BREAKER_COST) {
            addScore(-WALL_BREAKER_COST);
            // Break the wall
            ArrayList<Wall> walls = map.getWalls();
            for (Wall wall : walls) {
                ArrayList<Wall> newWalls = wall.splitWall(snappedGpsLoc, holeSize);
                if (newWalls != null) {
                    // The wall has to be split
                    for (int i=0; i<newWalls.size(); i++) {
                        Wall newWall = newWalls.get(i);
                        if (newWall.getId().equals(wallId)) {
                            // Remove the old wall
                            map.removeMapItem(newWall.getId());
                        }

                        // Add the new wall to the game
                        map.addMapItem(newWall);
                        gameUpdateHandler.sendCreateWall(newWall.getOwnerId(), newWall.getId(), newWall.getPoints(), newWall.getColor());
                    }
                }
            }
            showNotification(getString(R.string.wall_breaker_notification), Toast.LENGTH_SHORT);
        } else {
            showNotification(getString(R.string.no_wall_breaker_notification), Toast.LENGTH_SHORT);
        }
    }

    // endregion

    // region End Game

    /**
     * Is called when the player himself has died
     * @param killerId The user id of the killer of the player
     * @param killerName The name of the killer
     */
    public void onDeath(String killerId, String killerName) {
        // For debugging purposes
        if (IMMORTAL)
            return;

        // Hide all wall controls
        Button breakWallButton = (Button) findViewById(R.id.breakWallButton);
        breakWallButton.setVisibility(View.GONE);

        // Stop creating the wall
        creatingWall = false;
        wallId = null;

        // Tell the applications that the player has died
        showNotification(getString(R.string.you_died_text), Toast.LENGTH_SHORT);
        gameUpdateHandler.sendDeathMessage(killerId, killerName);

        if (GameSettings.isOwner()) {
            // There is one less player alive now
            playersAliveCount -= 1;
            // If there is only one player left : end the game
            if (playersAliveCount <= 1)
                endGame();
        }

        isAlive = false;
    }

    /**
     * Is called when another player has died
     * @param playerName The name of the player that died
     * @param killerId The id of the killer
     * @param killerName The name of the killer
     */
    public void playerDied(String playerName, String killerId, String killerName) {
        if (killerId.equals("")) {
            // The player died because he went to far from the road
            showNotification(
                    getString(R.string.player_died_text).replaceAll("%name", playerName),
                    Toast.LENGTH_SHORT
            );
        } else {
            // The player crossed a wall
            // The killer gets a bonus score
            if (GameSettings.getPlayerId().equals(killerId)) {
                addScore(KILL_SCORE);
                showNotification(
                        getString(R.string.player_killed_text).replaceAll("%name", playerName).replaceAll("%killer", "you")
                                +" "+getString(R.string.score_received_text).replaceAll("%score", String.valueOf(KILL_SCORE)),
                        Toast.LENGTH_SHORT
                );
            } else {
                showNotification(
                        getString(R.string.player_killed_text).replaceAll("%name", playerName).replaceAll("%killer", killerName),
                        Toast.LENGTH_SHORT
                );
            }
        }

        if (GameSettings.isOwner()) {
            // There is one less player alive now
            playersAliveCount -= 1;
            // If there is only one player left : end the game
            if (!IMMORTAL && playersAliveCount <= 1)
                endGame();
        }
    }

    /**
     * Is called by the host when either the time runs out or when there are no players left
     */
    public void endGame() {
        if (hasEnded)
            return;

        hasEnded = true;

        // The host stores his own score and initializes all of the other scores to -1
        playerScores = new HashMap<>();
        for (Integer player : GameSettings.getPlayersInGame()) {
            if (player == GameSettings.getUserId())
                playerScores.put(String.valueOf(player), travelledDistance);
            else
                playerScores.put(String.valueOf(player), -1.0);
        }

        // Tell the other players that the game has ended
        gameUpdateHandler.sendEndGame();

        // Wait for the final scores of the other players before ending the game
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                winner = processFinalScores();
                notifyEndGame(winner);
            }
        }, FINAL_SCORE_TIMEOUT * 1000);
    }

    /**
     * Used to send a players final score to the host
     */
    public void sendScore() {
        double score = getScore();
        gameUpdateHandler.sendScore(score);
    }

    /**
     * Used by the host to store the player score
     * @param playerId The id of the player
     * @param score The score of the player
     */
    public void storePlayerScore(String playerId, double score) {
        if(playerScores != null) // Avoid crash when start button is pressed too soon
            playerScores.put(playerId, score);
    }

    /**
     * Called by the host when the time to send scores ran out
     * @return The winner
     */
    public String processFinalScores() {
        double maxScore = -1;
        String winningPlayer = "";
        for (String playerId : playerScores.keySet()) {
            if (playerScores.get(playerId) > maxScore) {
                maxScore = playerScores.get(playerId);
                winningPlayer = playerId;
            }
        }
        return winningPlayer;
    }

    public void onEndGame() {
        if (!GameSettings.isOwner()) {
            if (hasEnded)
                return;

            hasEnded = true;
        }

        // The host updates the high scores
        if(travelledDistance>GameSettings.getProfile().getHighscore()){
            dataServer.sendRequest(
                    ServerCommand.SET_HIGHSCORE,
                    ImmutableMap.of(
                            "id", GameSettings.getPlayerId(),
                            "token", GameSettings.getPlayerToken(),
                            "highscore", String.valueOf(Math.round(travelledDistance))),
                    new HttpConnector.Callback() {
                        @Override
                        public void handleResult(String data) {
                            showWinner();
                        }
                    }
            );
        } else {
            showWinner();
        }
    }

    /**
     * Show the winner in a dialog box. (is called when the host has calculated the winner)
     * When the user presses ok the game ends
     */
    public void showWinner() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.game_over_text));

        Player winningPlayer = (Player) map.getItemById(winner);
        String winnerName = "";
        if (winningPlayer != null)
            winnerName = winningPlayer.getName();

        // Show the winner
        builder.setMessage(
                getString(R.string.game_winner_text).replaceAll(
                        "%winner",
                        winnerName)
        );

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // End the game
                quitGame();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Tell all of the players and the server that the game has ended
     * @param winner The winner of the game
     */
    public void notifyEndGame(String winner) {
        // Tell the other players that the game has ended
        // The players show the winner when they receive this message
        gameUpdateHandler.sendWinner(winner);

        // Tell the server that the game has ended
        ImmutableMap query = ImmutableMap.of(
                "gameId", String.valueOf(GameSettings.getGameId()),
                "token", GameSettings.getGameToken(),
                "winnerId", winner);

        dataServer.sendRequest(ServerCommand.END_GAME, query, new HttpConnector.Callback() {
            @Override
            public void handleResult(String data) {
                try {
                    JSONObject result = new JSONObject(data);
                    // TODO check for errors
                    // The host shows the winner here
                    onEndGame();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Quit game activity
     */
    public void quitGame() {
        // Clear data from GameSettings
        GameSettings.setGameId(-1);
        GameSettings.setGameName(null);
        GameSettings.setGameToken(null);
        GameSettings.setCanBreakWall(false);
        GameSettings.setTimeLimit(-1);
        GameSettings.setLastPlayTime((System.currentTimeMillis()-startTime)/1000);

        if(countdown!=null) {
            countdown.removeCallbacks(countdownRunnable);
            countdownRunnable = null;
            countdown = null;
        }

        // Start Lobby activity while destroying RoomActivity and HostActivity
        Intent intent = new Intent(this, LobbyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        // Close the GameActivity
        finish();
    }

    // endregion

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
     * Override the back button so it doesn't work
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //preventing default implementation previous to android.os.Build.VERSION_CODES.ECLAIR
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.

        // Store game settings
        GameSettings.storeInBundle(savedInstanceState);
        savedInstanceState.putDouble("travelledDistance", travelledDistance);
        // TODO maybe add more data (at least it won't crash now)
    }

    /**
     * Is called when the application is paused
     */
    @Override
    public void onPause() {
        super.onPause();
        locationListener.stopLocationUpdate();      // Pauses the lcoation listener
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// Disable screen lock on
        if (isAlive) {
            frequencyListener.pause();
            sensorDataObservable.Pause();           // Pauses the sensor observer
        }
    }

    /**
     * Is called when the application is resumed
     */
    @Override
    public void onResume() {
        super.onResume();
        locationListener.startLocationUpdate(); // Start the location listener again
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// Keep screen on
        if (isAlive) {
            sensorDataObservable.Resume();          // Resume the sensor observer
            frequencyListener.run();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (gameEventHandler != null)
            gameEventHandler.stop();

        // Stop the socket
        connection.pause();
    }

    // endregion    

    // region Sensors (including sound) and Gps tracking
    // ---------------------------------------------------------------------------------------------

    /**
     * Is called when the device's location is changed (called by CustomLocationListener)
     *
     * @param location the new location of the device
     */
    @Override
    public void updateLocation(Location location) {
        if (location.getAccuracy() >= IGNORE_ACCURACY)
            return;

        // Store the new location in a LatLng object
        LatLng newGpsLoc = new LatLng(location.getLatitude(), location.getLongitude());
        // Calculate the distance between the new location and the last location
        double distance = LatLngConversion.getDistancePoints(gpsLoc, newGpsLoc);

        // Log.d("VALUE", "Distance " + String.valueOf(distance));

        // If the location is to close to the last location nothing needs to happen
        // This decreases the amount of work the application needs to do
        if (distance >= LOCATION_THRESHOLD) {
            // Store the new location
            gpsLoc = newGpsLoc;
            height = location.getAltitude();

            if(currentEvent!=null && currentEvent instanceof KingOfHillEvent){
                ((TextView)findViewById(R.id.eventValue)).setText(getString(R.string.king_of_hill_event_text).replaceAll("%value", "" + height));
            }

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

    public void handleBellDetected() {
        // TODO: avoid calling this when the map is not yet ready

        if(isBellRinging)
            return; // Wait for the bell to stop ringing
        isBellRinging = true;
        gameUpdateHandler.sendBellSound(GameSettings.getPlayerId());
        bellCount += 1;

        if(currentEvent!=null && currentEvent instanceof BellEvent){
            ((TextView)findViewById(R.id.eventValue)).setText(getString(R.string.bell_event_text).replaceAll("%value",""+bellCount));
        }

        // After 3 seconds, disable the bell.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isBellRinging = false;
            }
        }, 3000);
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

    public String formatTime(long milliseconds){
        int hours = (int)(milliseconds / (1000*60*60));
        int rest = (int)(milliseconds % (1000*60*60));
        int minutes = rest / (1000*60);
        rest %= 1000*60;
        int seconds = rest / 1000;
        String time = String.format("%02d",minutes) + ":" + String.format("%02d",seconds);
        if(hours!=0)
            time = String.format("%02d",hours) + ":" + time;
        return time;
    }

}

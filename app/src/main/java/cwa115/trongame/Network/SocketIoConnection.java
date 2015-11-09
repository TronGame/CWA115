package cwa115.trongame.Network;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import cwa115.trongame.Utils.LatLngConversion;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


/**
 * Represents a Socket.IO connection
 */
public class SocketIoConnection implements Handler.Callback {

    private static final String SOCKETIO_URL = "http://daddi.cs.kuleuven.be";
    private static final String SOCKETIO_PATH = "/peno3/socket.io";
    private static final String BUNDLE_MESSAGE_KEY = "msg";

    private String groupId;
    private String sessionId;
    private Handler myOnReceiveHandler;
    private SocketIoHandler onReceiveHandler;

    private Socket socket;
    {
        try {
            IO.Options options = new IO.Options();
            options.path = SOCKETIO_PATH;
            socket = IO.socket(SOCKETIO_URL, options);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    private Emitter.Listener handleMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if(args[0] == null)
                return; // Ignore this for now
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putString(BUNDLE_MESSAGE_KEY, args[0].toString());
            msg.setData(bundle);
            myOnReceiveHandler.sendMessage(msg);
        }
    };

    public SocketIoConnection(String groupId, String sessionId, SocketIoHandler givenHandler) {
        myOnReceiveHandler = new Handler(this);
        this.groupId = groupId;
        this.sessionId = sessionId;

        onReceiveHandler = givenHandler;
        socket.on("broadcastReceived", handleMessage);
        socket.connect();
        try {
            JSONObject obj = new JSONObject();
            obj.put("groupid", groupId);
            obj.put("sessionid", sessionId);
            socket.emit("register", obj);
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Broadcasts a JSON message.
     * @param message the JSON message to be sent
     */
    public void sendMessage(JSONObject message, String type) {
        JSONObject req = new JSONObject();
        try {
            message.put("type", type);
            req.put("data", message);
            req.put("groupid", groupId);
            req.put("sessionid", sessionId);
        } catch(JSONException e) {
            // TODO: handle exceptions
        }
        socket.emit("broadcast", req);
    }

    /**
     * Called whenever a new message is received.
     * @param msg the message that was received (containing the data as a bundle)
     */
    @Override
    public boolean handleMessage(Message msg) {
        try {
            JSONObject message = new JSONObject(msg.getData().getString(BUNDLE_MESSAGE_KEY));
            switch(message.getString("type")) {
                case "updatePosition":
                    onReceiveHandler.onRemoteLocationChange(
                            message.getString("playerId"),
                            message.getString("playerName"),
                            LatLngConversion.getPointFromJSON(message.getJSONObject("location"))
                    );
                    break;
                case "updateWall":
                    onReceiveHandler.onRemoteWallUpdate(
                            message.getString("playerId"),
                            message.getString("wallId"),
                            LatLngConversion.getPointFromJSON(message.getJSONObject("point"))
                    );
                    break;
            }

            return true;
        } catch(JSONException e) {
            e.printStackTrace();
            return false;
        }
    }
}

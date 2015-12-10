package cwa115.trongame.Network.GoogleMapsApi;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.PendingResult;
import com.google.maps.model.SnappedPoint;

import java.util.ArrayList;

import cwa115.trongame.Network.GoogleMapsApi.ApiListener;
import cwa115.trongame.Utils.LatLngConversion;

/**
 * Created by Peter on 09/11/2015.
 *
 * The snapped point handler
 * Is used to snap a list of points to the road
 */
public class SnappedPointHandler implements Handler.Callback {

    private PendingResult<SnappedPoint[]> request;          // The snap points request
    private Handler mainHandler;                            // Used to send a message back to the main thread
    private ApiListener<ArrayList<LatLng>> listener;        // The receiver of the snapped point list
    private boolean finished;                               // Is the snapped point handler still working

    public SnappedPointHandler(PendingResult<SnappedPoint[]> request, ApiListener<ArrayList<LatLng>> listener) {
        this.finished = false;                  // The snapped point handler is working

        this.request = request;                 // Store the request (if it is destroyed the callback will never happen)
        this.mainHandler = new Handler(this);   // Store the main handler
        this.listener = listener;               // Store the listener

        // Set the callback for the request
        // What needs to happen when the result is received is specified in here
        this.request.setCallback(new PendingResult.Callback<SnappedPoint[]>() {
            /**
             * Is called when a result is received
             *
             * Note: this method is *not* executed on the main thread, so it is
             *  not safe to access objects that live on the main thread here
             * (the mainHandler object is guaranteed not be used on the main thread)
             * @param result the points that were snapped to the nearest road
             */
            @Override
            public void onResult(SnappedPoint[] result) {
                // The result needs to be stored in a bundle so that it can be send to the main thread
                // This bundle can only store certain objects, which is whe the result
                // needs to be converted to two lists of doubles
                Bundle bundle = LatLngConversion.toBundle(result);
                // Create the message for the main thread
                Message msg = new Message();
                msg.setData(bundle);
                // Send the message to the main thread (this calls handleMessage further in the code)
                mainHandler.sendMessage(msg);
            }

            /**
             * This function is called when the api request failed for some reason
             * @param e provides a reason for the failure
             */
            @Override
            public void onFailure(Throwable e) {
                Log.d("ERROR", e.toString());
            }
        });
    }

    /**
     * Is the snapped point handler finished
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Stop the snapped point handler
     * (This doesn't really do anything for now)
     */
    public void stop() {
        // TODO make this functional
        request = null;
        // request.cancel();
    }

    /**
     * Handle the result on the main thread
     * @param msg the result stored in a bundle
     */
    @Override
    public boolean handleMessage(Message msg) {
        // Transform the information stored in the message to a list of LatLngs that can be send to the listener
        ArrayList<LatLng> result =  LatLngConversion.bundleToLatLng(msg.getData());
        // The handler is now finished
        this.finished = true;
        // Send the result to the listener
        return listener.handleApiResult(result);
    }
}

package cwa115.trongame.GoogleMapsApi;

import android.util.JsonReader;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * A GoogleAPI response.
 * TODO: Support multiple response types
 */
public class ApiResponse {

    private ArrayList<LatLng> points;


    /**
     * Construct from a given JsonReader
     * @param reader the given JsonReader object
     */
    public ApiResponse(JsonReader reader) {
        points = new ArrayList<>();
        // Read
        try {
            reader.beginObject();
            reader.beginArray();
            while(reader.hasNext())
                readPoint(reader);
            reader.endArray();
            reader.endObject();
        } catch(IOException e) {
            // ...
        }
    }

    private void readPoint(JsonReader reader) {
        try {
            reader.beginObject();
            while(reader.hasNext()) {
                String name = reader.nextName();
                if(name == "location") {
                    points.add(readLocation(reader));
                } else {
                    reader.skipValue();
                }
            }
                reader.endObject();
        } catch(IOException e) {
            // ...
        }
    }

    private LatLng readLocation(JsonReader reader) {
        try {
            reader.beginObject();
            double latitude = 0, longitude = 0;
            while(reader.hasNext()) {
                String id = reader.nextName();
                if(id == "latitude")
                    latitude = reader.nextDouble();
                else if(id == "longitude")
                    longitude = reader.nextDouble();
            }
            reader.endObject();
            return new LatLng(latitude, longitude);
        } catch(IOException e) {
            // TODO: Meaningful return statement
            return null;
        }
    }

    public ArrayList<LatLng> getPoints() {
        return points;
    }


}

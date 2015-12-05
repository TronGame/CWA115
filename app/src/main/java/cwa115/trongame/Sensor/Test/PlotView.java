package cwa115.trongame.Sensor.Test;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import java.util.Iterator;

/**
 * Created by Bram on 15-10-2015.
 */
public class PlotView extends View {
    private static int NEXT_ID = 0;

    private SparseArray<Float[]> dataPoints;
    private SparseArray<Paint> dataColors;// The colors corresponding to the dataPoints
    private float maxY, minY;

    private Paint axe, label;// Paints used for drawing

    public PlotView(Context context) {
        super(context);
        construct();
    }

    public PlotView(Context context, AttributeSet attrs) {
        super( context, attrs );
        construct();
    }

    public PlotView(Context context, AttributeSet attrs, int defStyle) {
        super( context, attrs, defStyle );
        construct();
    }

    private void construct(){// Define the used Paints and initialize the LineStops
        maxY = 0;
        minY = 0;

        axe = new Paint();
        axe.setARGB(0, 0, 0, 0);

        label = new Paint();
        label.setColor(Color.BLACK);
        label.setTextAlign(Paint.Align.CENTER);

        dataPoints = new SparseArray<>();
        dataColors = new SparseArray<>();
    }

    private Float[] extractDataFromQueue(DataQueue<Float> dataQueue){
        Float[] data = new Float[dataQueue.size()*2];
        Iterator<Float> iterator = dataQueue.iterator();
        for(int i=0;i<dataQueue.size();i++) {
            Float value = iterator.next();
            data[2*i] = (float)i;
            data[2*i+1] = value;
            updateMinMaxY(value);
        }
        return data;
    }
    private Float[][] extractDataFromQueue(DataQueue<Float[]> dataQueue, int[] vectorIds){
        Float[][] data = new Float[vectorIds.length][dataQueue.size()*2];
        Iterator<Float[]> iterator = dataQueue.iterator();
        for(int i=0;i<dataQueue.size();i++){
            Float[] subData = iterator.next();
            for(int j=0;j<vectorIds.length;j++) {
                data[j][2*i] = (float)i;
                data[j][2*i+1] = subData[vectorIds[j]];
                updateMinMaxY(subData[vectorIds[j]]);
            }
        }
        return data;
    }
    private void updateMinMaxY(float value){
        if(value>maxY) maxY = value;
        if(value<minY) minY = value;
    }

    public int addDataQueue(DataQueue<Float> dataQueue, int color){
        NEXT_ID++;

        dataPoints.put(NEXT_ID, extractDataFromQueue(dataQueue));

        Paint p = new Paint();
        p.setColor(color);
        p.setStyle(Paint.Style.FILL);
        dataColors.put(NEXT_ID, p);

        invalidate();
        return NEXT_ID;
    }
    public void updateDataQueue(int queueId, DataQueue<Float> updatedQueue){
        dataPoints.put(queueId, extractDataFromQueue(updatedQueue));
        invalidate();
    }
    public void removeDataQueue(int queueId){
        dataPoints.delete(queueId);
        dataColors.delete(queueId);
        invalidate();
    }

    public int[] addDataQueue(DataQueue<Float[]> dataQueue, int[] vectorIds, int[] colors) {
        int[] queueIds = new int[vectorIds.length];
        Float[][] data = extractDataFromQueue(dataQueue, vectorIds);
        for(int i=0;i<vectorIds.length;i++) {
            NEXT_ID++;
            queueIds[i] = NEXT_ID;

            Paint p = new Paint();
            p.setColor(colors[i]);
            p.setStyle(Paint.Style.FILL);

            dataPoints.put(NEXT_ID, data[i]);
            dataColors.put(NEXT_ID, p);
        }

        invalidate();
        return queueIds;
    }
    public void updateDataQueue(int[] queueIds, DataQueue<Float[]> updatedQueue, int[] vectorIds){
        Float[][] data = extractDataFromQueue(updatedQueue, vectorIds);
        for(int i=0;i<queueIds.length;i++){
            dataPoints.put(queueIds[i], data[i]);
        }
        invalidate();
    }
    public void removeDataQueue(int[] queueIds){
        for(int queueId : queueIds){
            dataColors.delete(queueId);
            dataPoints.delete(queueId);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas c){
        c.drawColor(Color.argb(10, 0, 0, 0));// Clear canvas

        if(maxY==minY || dataPoints.size()==0) return; // No data set

        float relativeY = getHeight()/(maxY - minY);
        float zeroY = maxY * relativeY;
        c.drawLine(1f, 1f, 1f, getHeight(), axe);// Draw y-axis
        c.drawLine(1f, zeroY, getWidth(), zeroY, axe);// Draw x-axis
        c.drawText("x", getWidth() - 5, zeroY - 5, label);
        c.drawText("y", 5f, 5f, label);
        c.drawText("minY: " + minY + " ; maxY: " + maxY, getWidth()/2, 10f, label);

        // Draw the dataPoints:
        for(int i=0;i<dataPoints.size();i++) {
            int key = dataPoints.keyAt(i);
            Float[] data = dataPoints.get(key);
            for(int j=0;j<data.length;j+=2) {
                // float x = data[j]
                // float y = data[j+1]
                // realX = getWidth() * x * 2 / data.length
                // realY = zeroY - getHeight() * y / (maxY - minY) = maxY * relativeY - y * relativeY = (maxY - y) * relativeY
                c.drawCircle(getWidth() * 2 * data[j] / data.length, relativeY * (maxY - data[j + 1]), 3f, dataColors.get(key));
            }
        }
    }

}

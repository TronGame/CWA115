package cwa115.trongame.Sensor;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import cwa115.trongame.Utils.FFTBase;


/**
 * Asynchronously listens for sound in a given frequency range.
 */
public class FrequencyListener {

    private AudioRecord audio;
    private int bufferSize;
    private int sampleFrequency;
    private double minFreq;
    private double maxFreq;

    private Handler resultHandler;
    private AudioRecord.OnRecordPositionUpdateListener measurementHandler;
    private int nbPoints;
    private HandlerThread handlerThread;
    private Handler fourierHandler;

    /**
     * Constructor.
     * @param resultHandler the handler to be called when the signal is in the given frequency range
     * @param sampleFrequency the sampling frequency, should be at least the Nyquist frequency
     * @param minFreq a lower bound for the frequency to detect
     * @param maxFreq an upper bound for the frequency to detect
     * @param timePoints the amount of points to measure
     */
    public FrequencyListener(Handler resultHandler, int sampleFrequency, double minFreq, double maxFreq,
                             int timePoints) {
        nbPoints = timePoints;
        this.resultHandler = resultHandler;
        this.sampleFrequency = sampleFrequency;
        this.minFreq = minFreq;
        this.maxFreq = maxFreq;

        // Start the listener thread and create a handler for the callback
        handlerThread = new HandlerThread("FrequencyListener");
        handlerThread.start();
        fourierHandler = new Handler(handlerThread.getLooper());

        measurementHandler = new AudioRecord.OnRecordPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioRecord recorder) {}

            @Override
            public void onPeriodicNotification(AudioRecord recorder) {
                short[] audioData = new short[nbPoints];
                int count = recorder.read(audioData, 0, nbPoints);
                handleAudioData(audioData);
            }
        };

        bufferSize = Math.max(AudioRecord.getMinBufferSize(
                sampleFrequency, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        ), nbPoints * 5);
    }

    public void run() {
        Log.d("FrequencyListener", "Started.");
        audio = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleFrequency,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
        );
        Log.d("FrequencyListener", "Sample rate: " + Integer.toString(audio.getSampleRate()));
        Log.d("FrequencyListener", "State: " + Integer.toString(audio.getState()));
        audio.setPositionNotificationPeriod(nbPoints / 2); // 1 short = 2 bytes
        audio.setRecordPositionUpdateListener(measurementHandler, fourierHandler);
        audio.startRecording();
    }

    public void pause() {
        audio.stop();
        audio.release();
    }

    private double frequencyToIndex(double frequency, int length) {
        return frequency *  (1.0 * length) / (1.0 * sampleFrequency);
    }

    private double indexToFrequency(int index, int length) {
        return index *  (1.0 * sampleFrequency) / (1.0 * length);
    }


    private double getFrequency(double[] spectrum, double frequency) {
        final double scaleFactor = (1.0 * spectrum.length) / (1.0 * sampleFrequency);
        return spectrum[(int)(scaleFactor * frequency)];
    }

    private boolean handleAudioData(short[] audioData) {
        double[] audioDataDoubles = new double[nbPoints];
        for(int j = 0; j < nbPoints; ++j) {
            // Express relative amplitude as a value between -1 and 1
            // 32768 = 2^15 (16 bit quantisation)
            audioDataDoubles[j] = (double)audioData[j] / 32768.0;
        }
        double[] dft = FFTBase.fft(audioDataDoubles, new double[nbPoints], true);
        int max_pos = 0;
        for(int i = 0; i < dft.length / 2; ++i) {
            if(dft[max_pos] < dft[i])
                max_pos = i;
        }
        double max_freq = indexToFrequency(max_pos, dft.length);
        if(max_freq < maxFreq && max_freq > minFreq)
            resultHandler.sendEmptyMessage(0);

        Log.d("FrequencyListener", "Maximum frequency: " + Double.toString(max_freq));
        return false;
    }
}
package cwa115.trongame.Sensor.Frequency;

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
        try {
            audio.setPositionNotificationPeriod(nbPoints / 2); // 1 short = 2 bytes
            audio.setRecordPositionUpdateListener(measurementHandler, fourierHandler);
            audio.startRecording();
        } catch(Exception e) {
            // Ignore
        }
    }

    public void pause() {
        if (audio != null) {
            audio.stop();
            audio.release();
            audio = null;
        }
    }

    private int frequencyToIndex(double frequency, int length) {
        return (int)Math.round(frequency *  (1.0 * length) / (1.0 * sampleFrequency));
    }

    private double indexToFrequency(int index, int length) {
        return index *  (1.0 * sampleFrequency) / (1.0 * length);
    }

    private double getBandSum(double[] spectrum, double low, double high) {
        // Scan the spectrum from low to high for the maximum value
        final int min_freq_index = frequencyToIndex(low, spectrum.length);
        final int max_freq_index = frequencyToIndex(high, spectrum.length);
        double value = 0;
        for(int i = min_freq_index; i < max_freq_index; ++i)
            value += spectrum[i];
        return value;
    }

    private boolean handleAudioData(short[] audioData) {
        double[] audioDataDoubles = new double[nbPoints];
        for(int j = 0; j < nbPoints; ++j) {
            // Express relative amplitude as a value between -1 and 1
            // 32768 = 2^15 (16 bit quantisation)
            audioDataDoubles[j] = (double)audioData[j] / 32768.0;
        }
        double[] dft = FFTBase.fft(audioDataDoubles, new double[nbPoints], true);

        double totalSum = getBandSum(dft, 0, Math.min(sampleFrequency / 2.2, 2 * maxFreq));
        double bandSum = getBandSum(dft, minFreq, maxFreq);
        // If the spectrum is concentrated in the band, invoke the handler
        if(bandSum / totalSum > .25)
            resultHandler.sendEmptyMessage(0);
        Log.d("FrequencyListener", "Band ratio: " + Double.toString(bandSum / totalSum));
        return false;
    }
}
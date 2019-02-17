package com.playback.playback;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public boolean isRecording = false;
    private AudioRecord recorder = null;
    private Thread recordingThread, timerThread = null;
    private int BufferElements2Rec = 1764050;
    private short[] file1, file2;
    int currentFile = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupButtons();

        requestRecordAudioPermission();

        file1 = new short[BufferElements2Rec];
        file2 = new short[BufferElements2Rec];

    }

    private void setupButtons(){

        Button start = findViewById(R.id.start);
        Button stop = findViewById(R.id.stop);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isRecording) {
                    startRecording();
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRecording) {
                    stopRecording();
                }
            }
        });

    }

    private void startRecording() {
        recorder = new AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(44100)
                        .setChannelMask(AudioFormat.CHANNEL_IN_STEREO)
                        .build())
                .setBufferSizeInBytes(AudioTrack
                        .getMinBufferSize(44100,
                                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT) * 2)
                .build();

        recorder.startRecording();

        timerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (recorder != null) {
                    recordData();
                    try {
                        Thread.sleep(20000); //record 20 seconds
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    isRecording = false;
                    currentFile = 1 - currentFile;
                    try {
                        Thread.sleep(100); //record 20 seconds
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        timerThread.start();
    }

    private void recordData(){
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }


    private void writeAudioDataToFile() {

        short[] arr;

        if(currentFile == 0){
            arr = file1;
        }
        else{
            arr = file2;
        }

        while (isRecording) {
            // gets the voice output from microphone to byte format
            recorder.read(arr, 0, BufferElements2Rec);
        }

    }

    private void stopRecording() {
        // stops the recording activity
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
            timerThread = null;
        }

        //saves wav file
        AudioTool.writeNewFile(file1, file2, currentFile, getFilesDir().getAbsolutePath());
        file1 = new short[BufferElements2Rec];
        file1 = new short[BufferElements2Rec];

    }

    private void requestRecordAudioPermission() {
    //check API version, do nothing if API version < 23!
    int currentapiVersion = android.os.Build.VERSION.SDK_INT;
    if (currentapiVersion > android.os.Build.VERSION_CODES.LOLLIPOP){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            }
        }
    }
  }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d("Activity", "Granted!");

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("Activity", "Denied!");
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}

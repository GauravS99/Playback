package com.playback.playback;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class AudioTool {


    final static String TAG = "AudioTool";

    private static byte[] waveFileHeader()
            throws IOException {

        byte[] header = new byte[44];

        int totalAudioLen = 3528100;
        int totalDataLen = 36 + totalAudioLen;
        int channels = 2;
        int longSampleRate = 44100;
        int RECORDER_BPP = 16;
        int byteRate = RECORDER_BPP * longSampleRate * channels / 8;


        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte)(totalDataLen & 0xff);
        header[5] = (byte)((totalDataLen >> 8) & 0xff);
        header[6] = (byte)((totalDataLen >> 16) & 0xff);
        header[7] = (byte)((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte)(longSampleRate & 0xff);
        header[25] = (byte)((longSampleRate >> 8) & 0xff);
        header[26] = (byte)((longSampleRate >> 16) & 0xff);
        header[27] = (byte)((longSampleRate >> 24) & 0xff);
        header[28] = (byte)(byteRate & 0xff);
        header[29] = (byte)((byteRate >> 8) & 0xff);
        header[30] = (byte)((byteRate >> 16) & 0xff);
        header[31] = (byte)((byteRate >> 24) & 0xff);
        header[32] = (byte)(2 * 16 / 8);
        header[33] = 0;
        header[34] = (byte) RECORDER_BPP;
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte)(totalAudioLen & 0xff);
        header[41] = (byte)((totalAudioLen >> 8) & 0xff);
        header[42] = (byte)((totalAudioLen >> 16) & 0xff);
        header[43] = (byte)((totalAudioLen >> 24) & 0xff);

        return header;
    }

    // convert short to byte
    private static byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    public static void writeNewFile(short[] dataOne, short[] dataTwo, int currentFile, String dir){
        // Write the output audio in byte
        String filePath =  dir + "/" + System.currentTimeMillis() + ".wav";

       // short[] newData = new short[dataOne.length];

        short[] first = currentFile == 0? dataOne: dataTwo;
        short[] second = currentFile == 1? dataOne: dataTwo;

        Log.i(TAG, "1: " + Arrays.toString(first));
        Log.i(TAG, "2: " + Arrays.toString(second));

//        int begin = -1;
//        for(int i = first.length - 1; i >= 0; i--){
//            if(first[i] != (short)0){
//                begin = i;
//            }
//            if(begin != -1){
//                newData[newData.length - 1 - (begin - i)] = first[i];
//            }
//        }
//
//
//        Log.i(TAG, "3: " + Arrays.toString(newData));
//
//        if(begin == -1){
//            begin = second.length;
//        }
//
//        //  0 1 2 3 4 5 6 7 8 9
//
//        for(int i = second.length - 1; i >= second.length - begin; i--){
//            newData[i - begin] = second[i];
//        }

        short[] newData = new short[dataOne.length + dataTwo.length];

        for(int i=0;i<dataOne.length;i++)
            newData[i] = dataOne[i];
        for(int i=0;i<dataTwo.length;i++)
            newData[i+dataOne.length] = dataTwo[i];

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.w(TAG, "writeNewFile: no 3");
        }

        try {
            byte bData[] = waveFileHeader();
            os.write(bData, 0, bData.length);
            bData = short2byte(newData);
            os.write(bData, 44, newData.length);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, "writeNewFile: no 1");
        }

        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, "writeNewFile: no 2");
        }
    }


}

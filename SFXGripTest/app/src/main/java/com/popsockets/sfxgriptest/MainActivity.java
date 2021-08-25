package com.popsockets.sfxgriptest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.CursorWindow;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;

import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.content.Intent;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

import SFX.ISFXSensorEventHandler;
import SFX.SFXManager;
import mediaplayer.AudioPlayer;
import sqlite.SQLiteUtil;

public class MainActivity extends Activity implements ISFXSensorEventHandler {

    SFXManager sfx;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //this code must be here

        try {
            Field field = CursorWindow.class.getDeclaredField("sCursorWindowSize");
            field.setAccessible(true);
            field.set(null, 5 * 1024 * 1024); //the 100MB is the new size
        } catch (Exception e) {
                e.printStackTrace();
        }

        //

      //  sfx = new SFXManager((SensorManager)getSystemService(Context.SENSOR_SERVICE),this);

    }



      

    public void playAudio(View view) {

        SQLiteUtil util = new SQLiteUtil(this);


        try {

            File tempFile = null;
            InputStream inputStream = getResources().openRawResource(R.raw.hanthane);
            tempFile = File.createTempFile("pre", "suf");
            copyFile(inputStream, new FileOutputStream(tempFile));


            Uri uri =  Uri.fromFile(tempFile);
            Intent objIntent = new Intent(this, AudioPlayer.class);
            objIntent.putExtra("audio", uri.getPath());
            util.save(this,"abc1","hn.mp3",uri);
            startService(objIntent);
        }catch(Exception e){

        }
    }

    public void stopAudio(View view) {
        Intent objIntent = new Intent(this, AudioPlayer.class);
        stopService(objIntent);
        SQLiteUtil util = new SQLiteUtil(this);

        try {
            byte[] filecontent = util.getAudioFile("abc1");
            System.out.println("got the file" + filecontent);
            File tempFile = File.createTempFile("pre", ".mp3");
            ByteArrayInputStream inputStream = new ByteArrayInputStream(filecontent);
            copyFile(inputStream, new FileOutputStream(tempFile));

            System.out.println("file is created" + tempFile);
            Uri uri =  Uri.fromFile(tempFile);
            System.out.println("file is created" + uri.getPath());
            Intent objIntentNew = new Intent(this, AudioPlayer.class);
            objIntentNew.putExtra("audio", uri.getPath());
            startService(objIntentNew);

        }catch(Exception e){
            System.out.println(e);
        }

    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    @Override
    public void onGyroValueChanged(float norm) {
       ;
    }

    @Override
    public void onMagnetValueChanged(float norm) {
        try {

            //


            Intent closeIntent = new Intent(this, AudioPlayer.class);
            stopService(closeIntent);

            InputStream inputStream;
            File tempFile = null;
            if (norm > -25) {
                inputStream  = getResources().openRawResource(R.raw.hanthane);
                System.out.println(norm + " hanthane");
            } else {
                System.out.println(norm + " aradhana");
                inputStream  = getResources().openRawResource(R.raw.aradhana);
            }

            tempFile = File.createTempFile("pre", "suf");

            copyFile(inputStream, new FileOutputStream(tempFile));


            Uri uri =  Uri.fromFile(tempFile);
            Intent objIntent = new Intent(this, AudioPlayer.class);
            objIntent.putExtra("audio", uri.getPath());
            startService(objIntent);
        }catch(Exception e){

        }
    }
}
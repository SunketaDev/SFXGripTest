package mediaplayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.popsockets.sfxgriptest.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AudioPlayer extends Service{
    private static final String LOGCAT = null;
    MediaPlayer objPlayer;

    public void onCreate(){
        super.onCreate();
    }



    public int onStartCommand(Intent intent, int flags, int startId){
        String value = null;
        if (intent !=null && intent.getExtras()!=null) {
           value  = intent.getExtras().getString("audio");
           Log.d(LOGCAT,"value thats is coming" + value);
        }
               Log.d(LOGCAT, "Service Started!");



        try {


            Uri uri = Uri.parse(value);
            System.out.println("--------------" + uri.getPath());

            objPlayer = new MediaPlayer();
            objPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
            objPlayer.setDataSource(getApplicationContext(), uri);
            objPlayer.prepare();
            objPlayer.start();
            Log.d(LOGCAT, "Media Player started!");
            if(!objPlayer.isLooping()){
                Log.d(LOGCAT, "Problem in Playing Audio");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



        return Service.START_STICKY;
    }

    public void onStop(){
        objPlayer.stop();
        objPlayer.release();
    }

    public void onPause(){
        objPlayer.stop();
        objPlayer.release();
    }
    public void onDestroy(){
        objPlayer.stop();
        objPlayer.release();
    }
    @Override
    public IBinder onBind(Intent objIndent) {
        return null;
    }
}

package sqlite;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Build;
import android.util.Log;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.popsockets.sfxgriptest.R;

import java.io.FileInputStream;
import java.io.InputStream;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class SQLiteUtil extends SQLiteOpenHelper {

   private final static String DBNAME = "sfxgripdb.db";
    private static String DB_PATH = "/data/data/sfxgrip/databases";

   private static SQLiteDatabase myDb;
   private Context context;

   private static String Structure_sql = "CREATE TABLE IF NOT EXISTS audio_files_tbl (\n" +
           "  id text PRIMARY KEY,\n" +
           "  file_name text NOT NULL,\n" +
           "  file_blob blob NOT NULL\n" +
           ");";


   public   SQLiteUtil(Context context){
       super(context, SQLiteUtil.DBNAME, null, 1);
        context = context;
       System.out.println("created");
   }

    public  void save(Context context, String id, String fileName,Uri filepath)  {

       try {
          // filepath.getPath()
           //InputStream is = context.getResources().openRawResource(R.raw.hanthane);
           FileInputStream fis = new FileInputStream( filepath.getPath());


           byte[] mediaFile = new byte[fis.available()];
           fis.read(mediaFile);
           ContentValues values = new ContentValues();
           values.put("file_blob", mediaFile);
           values.put("id",id);
           values.put("file_name",fileName);
           System.out.println("data started");
           this.getWritableDatabase().insert("audio_files_tbl",null,values);

           System.out.println("data inserted");
          // Toast.makeTextfile_name(this, "Done", Toast.LENGTH_SHORT).show();
       }catch(Exception e){
           System.out.println(e);
       }
    }

    public  void saveAudioFile(Context context, String id, String fileName, byte[] file)  {

        try {



            ContentValues values = new ContentValues();
            values.put("file_blob", file);
            values.put("id",id);
            values.put("file_name",fileName);
            System.out.println("data started");
            this.getWritableDatabase().insert("audio_files_tbl",null,values);

            System.out.println("data inserted");
            // Toast.makeTextfile_name(this, "Done", Toast.LENGTH_SHORT).show();
        }catch(Exception e){
            System.out.println(e);
        }
    }

    public byte[] getAudioFile(String id){

        String FILE_SELECT_QUERY =
                String.format("SELECT file_blob FROM audio_files_tbl where id = %s", "'"+id+"'");

        byte[] file = null;

        try {



            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery(FILE_SELECT_QUERY, null);
            try {
                if (cursor.moveToFirst()) {
                    do {
                       file = cursor.getBlob(0);

                    } while(cursor.moveToNext());
                }
            } catch (Exception e) {
                Log.d("Get Audio File", "Error while trying to get posts from database");
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }

        }catch(Exception e){
            System.out.println(e);
        }

        return file;

    }
    /*
    public void get(View view) {
        Cursor c = db.rawQuery("select * from imageTb", null);
        if(c.moveToNext())
        {
            byte[] image = c.getBlob(0);
            Bitmap bmp= BitmapFactory.decodeByteArray(image, 0 , image.length);
            imageView.setImageBitmap(bmp);
            Toast.makeText(this,"Done", Toast.LENGTH_SHORT).show();
        }
    }

     */

    public static boolean isTableExists(SQLiteDatabase sqLiteDatabase,String tableName) {

       try {
           boolean isExist = false;
           Cursor cursor = sqLiteDatabase.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
           if (cursor != null) {
               if (cursor.getCount() > 0) {
                   isExist = true;
               }
               cursor.close();
           }

           return isExist;
       } catch(Exception e){
           Log.d("isTableExists",e.getMessage());
           return false;
       }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

// Assumption if one table exists all tables exist
        if (SQLiteUtil.isTableExists(sqLiteDatabase,"audio_files_tbl")){
            // Structure is available
        } else {


            sqLiteDatabase.execSQL(Structure_sql);
            System.out.println("creating structure");
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}

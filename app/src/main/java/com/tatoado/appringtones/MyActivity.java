package com.tatoado.appringtones;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
public class MyActivity extends ActionBarActivity {
    MediaPlayer mp = new MediaPlayer();
    int position;
    int position2;

    int s1[] = {
            R.raw.hoy_hoy_hoy,
            R.raw.huevo_pascua
    };
    String[] title = new String[]{
            "Hoy hoy hoy!!",
            "Huevo de pascua gracioso"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        final ListView list30 = (ListView) findViewById(R.id.list3);

        ArrayAdapter adaptador = new ArrayAdapter(this, android.R.layout.simple_list_item_1, title);

        list30.setAdapter(adaptador);

        list30.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                method(position);
            }
        });
        //list.setAdapter(adapter);
        //Log.i("ramiro", "llego al final");

        list30.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView parent, View view, int position, long id) {
                //saveas(RingtoneManager.TYPE_RINGTONE, position);
                position2 = position; //utilizo position2 porque la this.position es para onItemClick

                final CharSequence[] items = {"Establecer como Ringtone", "Establecer como SMS/Notificación", "Establecer como Alarma"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MyActivity.this);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item){
                            case 0:
                                saveas(RingtoneManager.TYPE_RINGTONE, position2);
                                Toast.makeText(getApplicationContext(), "Se estableció como Ringtone", Toast.LENGTH_SHORT).show();
                                break;
                            case 1:
                                saveas(RingtoneManager.TYPE_NOTIFICATION, position2);
                                Toast.makeText(getApplicationContext(), "Se estableció como SMS/Notificación", Toast.LENGTH_SHORT).show();
                                break;
                            case 2:
                                saveas(RingtoneManager.TYPE_ALARM, position2);
                                Toast.makeText(getApplicationContext(), "Se estableció como Alarma", Toast.LENGTH_SHORT).show();
                                break;
                        }

                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return false;
            }
        });
    }

    public boolean saveas(int type, int position) {
        byte[] buffer = null;
        InputStream fIn = getBaseContext().getResources().openRawResource(
                s1[position]);
        int size = 0;

        try {
            size = fIn.available();
            buffer = new byte[size];
            fIn.read(buffer);
            fIn.close();
        } catch (IOException e) {
            return false;
        }

        String path = Environment.getExternalStorageDirectory().getPath()
                + "/media/audio/ringtones/";

        String filename = title[position];
        Log.i("ramiro", "path: " + path);

        boolean exists = (new File(path)).exists();
        if (!exists) {
            new File(path).mkdirs();
        }

        FileOutputStream save;
        try {
            save = new FileOutputStream(path + filename);
            save.write(buffer);
            save.flush();
            save.close();
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + path + filename)));

        File k = new File(path, filename);

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, k.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, filename);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/ogg");

        // This method allows to change Notification and Alarm tone also. Just
        // pass corresponding type as parameter
        if (RingtoneManager.TYPE_RINGTONE == type) {
            values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        } else if (RingtoneManager.TYPE_NOTIFICATION == type) {
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
        } else if (RingtoneManager.TYPE_ALARM == type) {
            values.put(MediaStore.Audio.Media.IS_ALARM, true);
        }

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(k
                .getAbsolutePath());
        Uri newUri = MyActivity.this.getContentResolver().insert(uri, values);
        RingtoneManager.setActualDefaultRingtoneUri(MyActivity.this, type,
                newUri);

        // Insert it into the database
        this.getContentResolver()
                .insert(MediaStore.Audio.Media.getContentUriForPath(k
                        .getAbsolutePath()), values);

        Log.i("ramiro", "llego al final de la escritura");
        return true;
    }

    public void method(int position){
        if((mp.isPlaying()==true) && (this.position == position))
            mp.stop();
        else{
            this.position = position;
            try{
                mp.reset();                               //resets the media player
                mp.release();                             //release the media player of current audio playing
                mp= MediaPlayer.create(this, s1[position]); //create a new  media player with the selected id
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                //mp.prepare(); //salta exception
                mp.seekTo(0);                             //seek to starting of song means time=0 ms
                mp.start();                               //start media player
            }
            catch (Exception e)
            {
                Toast.makeText(getApplication(), "error exception", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == event.KEYCODE_BACK) {
            System.out.println("Back is called...");
            mp.stop();
            mp.release();
            mp = null;
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
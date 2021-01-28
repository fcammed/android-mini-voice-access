package com.widget.floatingwidget;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.graphics.PixelFormat;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class FloatingWidgetShowService extends Service{


    WindowManager windowManager;
    View floatingView, collapsedView, expandedView;
    SeekBar barraVolumen;
    WindowManager.LayoutParams params ;
    boolean selfDestroy = false;
    LockScreenReceiver lockScreenReceiver;
    SettingsContentObserver settingsContentObserver;
    private String version="v0.9.3";

    public FloatingWidgetShowService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel chan = new NotificationChannel(
                    getApplicationContext().getPackageName(),
                    "FloatingWidget",
                    NotificationManager.IMPORTANCE_LOW);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_SECRET);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                    this, getApplicationContext().getPackageName());
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.volumenl)
                    .setContentTitle("App is running on foreground")
                    .setPriority(NotificationManager.IMPORTANCE_LOW)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setChannelId(getApplicationContext().getPackageName())
                    .build();

            startForeground(1, notification);
        }

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_widget_layout, null);
        TextView myVolumeTextView = floatingView.findViewById(R.id.Widget_Volumen_Text);
        myVolumeTextView.setText(version + " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));


        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);


        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        windowManager.addView(floatingView, params);

        expandedView = floatingView.findViewById(R.id.Layout_Expended);

        collapsedView = floatingView.findViewById(R.id.Layout_Collapsed);

        barraVolumen = floatingView.findViewById(R.id.seekBar1);
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        barraVolumen.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        barraVolumen.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        //registrando el receiver para detectar que la pantalla se enciende
        lockScreenReceiver = new LockScreenReceiver(windowManager,floatingView,params);
        IntentFilter lockFilter = new IntentFilter();
        lockFilter.addAction(Intent.ACTION_SCREEN_ON);
        lockFilter.addAction(Intent.ACTION_SCREEN_OFF);
        lockFilter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(lockScreenReceiver, lockFilter);
        //end registro

        //registrando un content receiver para detectar cuando cambian el volumen
        settingsContentObserver = new SettingsContentObserver( new Handler(),this.getApplicationContext(),floatingView);
        this.getApplicationContext().getContentResolver().registerContentObserver(
                Settings.System.CONTENT_URI,true,settingsContentObserver
        );


        floatingView.findViewById(R.id.Widget_Close_Icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selfDestroy=true;
                stopSelf();
                //Toast.makeText(FloatingWidgetShowService.this, "System Alert Window haz hecho click en cerrar.", Toast.LENGTH_LONG).show();

            }
        });

        expandedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);

            }
        });

        /*collapsedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                collapsedView.setVisibility(View.GONE);
                expandedView.setVisibility(View.VISIBLE);

            }
        });*/

        floatingView.findViewById(R.id.MainParentRelativeLayout).setOnTouchListener(new View.OnTouchListener() {
            int X_Axis, Y_Axis;
            float TouchX, TouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        X_Axis = params.x;
                        Y_Axis = params.y;
                        TouchX = event.getRawX();
                        TouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_UP:

                        collapsedView.setVisibility(View.GONE);
                        expandedView.setVisibility(View.VISIBLE);
                        return true;

                    case MotionEvent.ACTION_MOVE:

                        params.x = X_Axis + (int) (event.getRawX() - TouchX);
                        params.y = Y_Axis + (int) (event.getRawY() - TouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });

        //Aqui disminuimos el volumen

        floatingView.findViewById(R.id.Widget_Volumen_L_Icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Primero obtenemos el valor del volumen del telefono
                //getStreamVolume(int streamType)
                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                //int volumen = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int volumen_inicial =audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int volumen_siguiente = volumen_inicial-(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/10);
                //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,volumen_siguiente ,0);

            }
        });
        floatingView.findViewById(R.id.Widget_Volumen_H_Icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Primero obtenemos el valor del volumen del telefono
                //getStreamVolume(int streamType)
                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                //int volumen = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int volumen_inicial =audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int volumen_siguiente = volumen_inicial+(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/10);
                //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,volumen_siguiente ,0);

            }
        });
        floatingView.findViewById(R.id.Widget_Volumen_No_Icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Primero obtenemos el valor del volumen del telefono
                //getStreamVolume(int streamType)
                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                //int volumen = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int volumen_inicial =audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int volumen_siguiente = volumen_inicial+(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/10);
                //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,0 ,0);

            }
        });
        barraVolumen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //Primero obtenemos el valor del volumen del telefono
                //getStreamVolume(int streamType)
                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                //Ajusta el volumen del stream con el valor de la barra
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,progress ,0);


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });


    }

    //@Override
    protected void onStop()
    {
        //Log.e("floatingwidget","I_am ON_STOP");
        unregisterReceiver(lockScreenReceiver);
        getApplicationContext().getContentResolver().unregisterContentObserver(settingsContentObserver);
        //super.onStop();
    }

    @Override
    public void onDestroy() {
        //Log.e("floatingwidget","I_am ON_DESTROY");
        unregisterReceiver(lockScreenReceiver);
        getApplicationContext().getContentResolver().unregisterContentObserver(settingsContentObserver);
        if (floatingView != null) windowManager.removeView(floatingView);
        if (!selfDestroy)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, FloatingWidgetShowService.class));
            } else {
                startService(new Intent(this, FloatingWidgetShowService.class));
            }
        super.onDestroy();
    }

}
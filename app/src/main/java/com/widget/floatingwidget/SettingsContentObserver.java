package com.widget.floatingwidget;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

public class SettingsContentObserver extends ContentObserver {
    private View floatingView;
    int previousVolume;
    Context context;

    public SettingsContentObserver(Handler handler, Context c, View floatingView) {
        super(handler);
        this.context=c;
        this.floatingView = floatingView;
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        SeekBar barraVolumen = floatingView.findViewById(R.id.seekBar1);


        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

        int delta=previousVolume-currentVolume;

        if(delta>0)
        {
            //Logger.d("Decreased");
            previousVolume=currentVolume;
            barraVolumen.setProgress(currentVolume);
        }
        else if(delta<0)
        {
            //Logger.d("Increased");
            previousVolume=currentVolume;
            barraVolumen.setProgress(currentVolume);
        }
    }
}

package com.widget.floatingwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class LockScreenReceiver extends BroadcastReceiver
{
    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;

    public LockScreenReceiver(WindowManager windowManager, View floatingView, WindowManager.LayoutParams params) {
        this.windowManager = windowManager;
        this.floatingView = floatingView;
        this.params = params;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent != null && intent.getAction() != null)
        {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
            {
                // Screen is on but not unlocked (if any locking mechanism present)
                windowManager.updateViewLayout(floatingView, params);
                floatingView.setVisibility(View.VISIBLE);
                //Log.e("floatingwidget","I_am ON");
            }
            else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
            {
                // Screen is locked
            }
            else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT))
            {
                // Screen is unlocked
                windowManager.updateViewLayout(floatingView, params);
                floatingView.setVisibility(View.VISIBLE);
                /*Intent myIntent = new Intent(context, FloatingWidgetShowService.class);
                context.startService(myIntent);*/

                //Log.e("floatingwidget","I_am USER_PRESENT");
            }
        }
    }
}

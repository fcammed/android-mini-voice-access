package com.widget.floatingwidget;

//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final int SYSTEM_ALERT_WINDOW_PERMISSION = 7;
    Button button ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        button = (Button)findViewById(R.id.buttonShow);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            RuntimePermissionForUser();
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(new Intent(MainActivity.this, FloatingWidgetShowService.class));
                    } else {
                        startService(new Intent(MainActivity.this, FloatingWidgetShowService.class));
                    }
                    finish();

                } else if (Settings.canDrawOverlays(MainActivity.this)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(new Intent(MainActivity.this, FloatingWidgetShowService.class));
                    } else {
                        startService(new Intent(MainActivity.this, FloatingWidgetShowService.class));
                    }
                    finish();

                } else {
                    RuntimePermissionForUser();

                    Toast.makeText(MainActivity.this, "System Alert Window Permission Is Required For Floating Widget.", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    public void RuntimePermissionForUser() {

        Intent PermissionIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));

        startActivityForResult(PermissionIntent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }
}
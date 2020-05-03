package com.example.luxmeter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import io.github.hyuwah.draggableviewlib.DraggableUtils;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensmgr;
    private Sensor accsensor;
    private TextView floatingLuxView, inside_luxView;
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private boolean isShowing_floating_lux = false;
    private Button floating_btn;
    private float sensor_lux;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initial();
    }

    private void initial(){

        inside_luxView = findViewById(R.id.textview_inside);
        DraggableUtils.makeDraggable(inside_luxView);
        //register light sensor
        sensmgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        accsensor = sensmgr.getDefaultSensor(Sensor.TYPE_LIGHT);

        InitialTextView();

        floating_btn = findViewById(R.id.button);
        floating_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToggleFloatingLux();
            }
        });

    }

    private void InitialTextView(){
        floatingLuxView = new TextView(this);
        floatingLuxView.setText("Hi I am floating View");
        floatingLuxView.setTextColor(Color.rgb(255, 255, 0));
        floatingLuxView.setTextSize(32f);
        floatingLuxView.setShadowLayer(10, 5, 5, Color.rgb(56, 56, 56));

        //TODO, why this implementation case issue ?
        DraggableUtils.makeDraggable(floatingLuxView);

        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);

        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP|Gravity.LEFT;

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        sensor_lux = sensorEvent.values[0];
        Log.d("DEBUG", "sensor_lux = " + sensor_lux);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                inside_luxView.setText(String.valueOf(sensor_lux));
                floatingLuxView.setText(String.valueOf(sensor_lux));
            }
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        sensmgr.registerListener(this, accsensor, SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }

    private void ToggleFloatingLux(){
        requestOverlayPermission();
        if (!isShowing_floating_lux){
            //show
            showFloatingLux();
        }else{
            //off
            closeFloatingLux();
        }
    }

    private void showFloatingLux(){
        if (!isShowing_floating_lux){
            windowManager.addView(floatingLuxView, params);
            isShowing_floating_lux = true;
        }
    }

    private void closeFloatingLux(){
        if (isShowing_floating_lux){
            windowManager.removeViewImmediate(floatingLuxView);
            isShowing_floating_lux = false;
        }
    }

    @Override
    protected void onDestroy() {
        closeFloatingLux();
        super.onDestroy();
    }

    private void requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 1234);
        }
    }
}

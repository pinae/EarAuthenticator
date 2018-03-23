package de.ct.earauthenticator;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mProximity;
    private static final int SENSOR_SENSITIVITY = 4;
    private EarTouchArea mEarTouchArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mEarTouchArea = findViewById(R.id.mainEarTouchArea);
        final ImageButton plusButton = findViewById(R.id.addEar);
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEarTouchArea.startTrainMode();
                view.setVisibility(View.INVISIBLE);
            }
        });
        mEarTouchArea.setTrainFinishedEventListener(
                new EarTouchArea.OnTrainFinishedEventListener() {
            @Override
            public void onEvent() {
                plusButton.setVisibility(View.VISIBLE);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 250 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(250,
                            VibrationEffect.DEFAULT_AMPLITUDE));
                }else{
                    //deprecated in API 26
                    v.vibrate(250);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] >= -SENSOR_SENSITIVITY && event.values[0] <= SENSOR_SENSITIVITY) {
                //near
                if (mEarTouchArea != null) {
                    mEarTouchArea.setEarPossible(true);
                }
            } else {
                //far
                if (mEarTouchArea != null) {
                    mEarTouchArea.setEarPossible(false);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}

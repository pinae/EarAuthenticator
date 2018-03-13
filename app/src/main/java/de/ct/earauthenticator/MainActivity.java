package de.ct.earauthenticator;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mProximity;
    private static final int SENSOR_SENSITIVITY = 4;
    private EarTouchArea mEarTouchArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mEarTouchArea = findViewById(R.id.mainEarTouchArea);
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

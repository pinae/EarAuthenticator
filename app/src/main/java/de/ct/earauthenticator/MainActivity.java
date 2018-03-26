package de.ct.earauthenticator;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import java.util.LinkedList;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mProximity;
    private static final int SENSOR_SENSITIVITY = 4;
    private EarTouchArea mEarTouchArea;
    private MenuItem mClearItem;

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
                saveTrainingData();
                mClearItem.setVisible(true);
                Log.d("Training finished.", "Data Saved.");
                vibrate(250);
            }
        });
        mEarTouchArea.setEarVerifiedEventListener(new EarTouchArea.OnEarVerifiedListener() {
            @Override
            public void onEvent() {
                vibrate(10);
            }
        });
    }

    private void vibrate(int duration) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(duration,
                    VibrationEffect.DEFAULT_AMPLITUDE));
        }else{
            //deprecated in API 26
            v.vibrate(duration);
        }
    }

    private void saveTrainingData() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        LinkedList<EarDataset> trainingData = mEarTouchArea.getTrainingData();
        for (int i=0; i < trainingData.size(); i++) {
            editor.putString("data " + Integer.toString(i),
                    trainingData.get(i).toString());
        }
        editor.apply();
    }

    private void loadTrainingData() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        LinkedList<EarDataset> dataset = new LinkedList<>();
        Map<String, ?> prefs = sharedPref.getAll();
        for (Map.Entry<String, ?> entry1 : prefs.entrySet()) {
            String key = entry1.getKey();
            if (key.matches("data \\d+")) {
                EarDataset d = new EarDataset((String) entry1.getValue());
                dataset.add(d);
            }
        }
        mEarTouchArea.setTrainingData(dataset);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
        loadTrainingData();
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

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_actions, menu);
        mClearItem = menu.findItem(R.id.action_clear);
        mClearItem.setVisible(this.mEarTouchArea.getTrainingData().size() > 0);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();
        if (id == R.id.action_clear) {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.apply();
            mEarTouchArea.setTrainingData(new LinkedList<EarDataset>());
            mClearItem.setVisible(false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

package com.brainlessdevelopers.debumper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Main activity in most basic version
 * showing the information from the accelerometer on
 * the android device
 *
 * @author Fran Grgić
 * @version a0.0.2
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //TODO ne upisuje se svaka promjena u file nego u neku listu i onda dretva neka svakih deset sekundi upiše informacije u datoteku :)

    public static final int DIV_MILISECONDS_BY = 10;

    Toast toast;

    /**
     * manages all the sensors
     */
    private SensorManager sm;

    /**
     * accelerometer sensor
     */
    private Sensor accelerometer;
    /**
     * linear accelerometer sensor
     */
    private Sensor linearAccelerometer;
    /**
     * orientation sensor
     */
    private Sensor magneticField;

    File accelerometerOutputFolder;
    File linearAccelerometerOutputFolder;
    File magneticFieldOutputFolder;

    File accelerometerLog;
    File linearAccelerometerLog;
    File magneticFieldLog;

    /**
     * temporary list for storing accelerometer data.
     * Emptied every time it's data is appended to a file
     */
    List<String> accelerometerList = new ArrayList<>();

    // used to see if it should write to a file or not (space fills up too quickly otherwise)
    long timePassed;

    //TextView to display all the information from the sensor
    private TextView log;

    /**
     * Activity launches here
     *
     * @param savedInstanceState saved instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        accelerometerList.add("kkkk");

        // check if this app has all the required permissions
        allPermissionsGranted();

        // At what time the sensor tracking started
        timePassed = (new Date().getTime()) / DIV_MILISECONDS_BY;

        // Check if external storage is available for write
        if (!isExternalStorageWritable()) {
            toast = Toast.makeText(getApplicationContext(), "External storage unavailable, app won't save any data", Toast.LENGTH_LONG);
            toast.show();
        } else {
            Log.e("Storage", "available");
        }

        initializeFolders();

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        linearAccelerometer = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        magneticField = sm.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        // Setting sensor manager to listen to changes at normal rate
        sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, linearAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void initializeFolders() {
        // Last part of the filename: record tracking.
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");

        accelerometerOutputFolder = getOutputFolder("sensors/accelerometer");
        accelerometerLog = new File(accelerometerOutputFolder, "accelerometer_" + df.format(c.getTime()) + ".txt");
        linearAccelerometerOutputFolder = getOutputFolder("sensors/linearAccelerometer");
        linearAccelerometerLog = new File(linearAccelerometerOutputFolder, "linearAccelerometer_" + df.format(c.getTime()) + ".txt");
        magneticFieldOutputFolder = getOutputFolder("sensors/magneticField");
        magneticFieldLog = new File(magneticFieldOutputFolder, "magneticField_" + df.format(c.getTime()) + ".txt");
    }

    /**
     * Asks user to provide the app with the required permissions
     */
    private void allPermissionsGranted() {
        int storagePermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (storagePermissionCheck == PackageManager.PERMISSION_GRANTED) {
            Log.e("Write permission", "granted ");
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // TODO explain why user needs to enable this permission!
                Toast.makeText(getApplicationContext(), "Please enable this app to write in your storage since that's its main purpose", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 1);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 1);
            }
        }
    }

    private void updateAcceleration(SensorEvent event) {
        log = (TextView) findViewById(R.id.log);
        String display = String.format("X: %.2f\nY: %.2f\nZ: %.2f\n", event.values[0], event.values[1], event.values[2]);
        String result = String.format("%f|%f|%f\n", event.values[0], event.values[1], event.values[2]);
        log.setText(display);

        appendingToTheFile(accelerometerLog, result, false);
    }

    private void updateLinearAcceleration(SensorEvent event) {
        String result = String.format("%f|%f|%f\n", event.values[0], event.values[1], event.values[2]);
        appendingToTheFile(linearAccelerometerLog, result, false);
    }

    private void updateMagneticField(SensorEvent event) {
        String result = Double.valueOf(event.values[0]).toString() + "\n";
        appendingToTheFile(magneticFieldLog, result, false);
    }

    private void appendingToTheFile(File file, String s, boolean slower) {
        long currentTime = (new Date().getTime());
        if (slower) {
            currentTime /= DIV_MILISECONDS_BY;
        }

        try (FileWriter fileWrite = new FileWriter(file, true)) {
            // ako je prošlo više od desetine sekunde, piši u file
            if ((currentTime - timePassed) > 1) {
                timePassed = currentTime;
                fileWrite.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The method called when the value of each registered sensor is changed
     *
     * @param event event that happened with the sensor
     */
    public void onSensorChanged(SensorEvent event) {
        int eventType = event.sensor.getType();

        if (eventType == Sensor.TYPE_ACCELEROMETER) {
            updateAcceleration(event);
        }
        if (eventType == Sensor.TYPE_LINEAR_ACCELERATION) {
            updateLinearAcceleration(event);
        }
        if (eventType == Sensor.TYPE_ORIENTATION) {
            updateMagneticField(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //not using this
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public File getOutputFolder(String folderName) {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), folderName);
        if (!file.mkdirs()) {
            Log.e("Directory ", "Directory not created");
        }
        return file;
    }

    protected void onPause() {
        super.onPause();
        sm.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, linearAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
}

package io.archylex.guidingstar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private SensorManager mSensorManager;
    private Sensor mRotationV;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private boolean hasAccelerometer = false;
    private boolean hasMagnetometer = false;
    private float[] orientation = new float[3];
    private float[] rMat = new float[9];
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private ImageView arrow;
    private ImageButton btnLight;
    private ImageButton btnSOS;
    private boolean isFlashOn = false;
    private boolean isSosOn = false;
    private boolean hasFlash = false;
    private CameraManager cameraManager;
    private Camera camera;
    private Camera.Parameters params;

    private static final int CAMERA_REQUEST = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        btnLight = findViewById(R.id.btnLight);
        btnSOS = findViewById(R.id.btnSOS);
        arrow = findViewById(R.id.arrow);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);

        hasFlash = getPackageManager().
                hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!hasFlash)
            Toast.makeText(MainActivity.this, "This device doesn't supprot camera.", Toast.LENGTH_LONG).show();
        else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
                cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            } else{
                if (camera == null) {
                    try {
                        camera = Camera.open();
                        params = camera.getParameters();
                    } catch (RuntimeException e) {
                    }
                }
            }
        }

        toggleButtonImages();

        btnLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasFlash) {
                    if (isFlashOn)
                        flashlightOff();
                    else
                        flashlightOn();
                }
            }
        });

        btnSOS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasFlash) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && !isSosOn)
                        SOS();
                }
            }
        });

        startCompass();
    }

    private void flashlightOn() {
        isFlashOn = true;
        toggleButtonImages();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                cameraManager.setTorchMode(cameraManager.getCameraIdList()[0], true);
            } catch (CameraAccessException e) {
            }
        } else {
            if (camera == null || params == null)
                return;

            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
        }
    }

    private void flashlightOff() {
        isFlashOn = false;
        toggleButtonImages();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                cameraManager.setTorchMode(cameraManager.getCameraIdList()[0], false);
            } catch (CameraAccessException e) {
            }
        } else {
            if (camera == null || params == null)
                return;

            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
        }
    }

    private void SOS()
    {
        isSosOn = true;
        toggleButtonImages();
        String signal = "101010202020101010";
        for (int i = 0; i < signal.length(); i++) {
            long delay = 200;
            switch (signal.charAt(i)) {
                case '0':
                    delay = 200;
                    break;
                case '1':
                    delay = 200;
                    break;
                case '2':
                    delay = 1000;
                    break;
            }
            if (signal.charAt(i) == '0') {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    try {
                        cameraManager.setTorchMode(cameraManager.getCameraIdList()[0], false);
                    } catch (CameraAccessException e) {
                    }
                }
            } else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    try {
                        cameraManager.setTorchMode(cameraManager.getCameraIdList()[0], true);
                    } catch (CameraAccessException e) {
                    }
                }
            }
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        isSosOn = false;
        toggleButtonImages();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasFlash = getPackageManager().
                            hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
                } else {
                    btnLight.setEnabled(false);
                    btnSOS.setEnabled(false);
                    Toast.makeText(MainActivity.this, "Get permission for camera.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void toggleButtonImages() {
        if(isFlashOn)
            btnLight.setImageResource(R.drawable.lg_on);
        else
            btnLight.setImageResource(R.drawable.lg_off);

        if(isSosOn)
            btnSOS.setImageResource(R.drawable.sos_on);
        else
            btnSOS.setImageResource(R.drawable.sos_off);
    }

    public void startCompass() {
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
            if ((mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) || (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null)) {
                Toast.makeText(MainActivity.this, "This device no has compass.", Toast.LENGTH_LONG).show();
            } else {
                mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                hasAccelerometer = mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
                hasMagnetometer = mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
            }
        } else{
            mRotationV = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            hasAccelerometer = mSensorManager.registerListener(this, mRotationV, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stopCompass() {
        if (hasAccelerometer) {
            mSensorManager.unregisterListener(this, mRotationV);
        }
        else {
            mSensorManager.unregisterListener(this, mAccelerometer);
            mSensorManager.unregisterListener(this, mMagnetometer);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCompass();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCompass();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int mAzimuth = 0;

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(rMat, orientation);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }

        mAzimuth = Math.round(mAzimuth);
        arrow.setRotation(-mAzimuth);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            if (camera != null) {
                camera.release();
                camera = null;
            }
        }
    }
}
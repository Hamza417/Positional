package app.simple.positional.parallax;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

public class ParallaxView extends AppCompatImageView implements SensorEventListener {
    
    private static final int DEFAULT_SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;
    public static final int DEFAULT_MOVEMENT_MULTIPLIER = 3;
    public static final int DEFAULT_MIN_MOVED_PIXELS = 1;
    private static final float DEFAULT_MIN_SENSIBILITY = 0;
    
    private float mMovementMultiplier = DEFAULT_MOVEMENT_MULTIPLIER;
    private int mSensorDelay = DEFAULT_SENSOR_DELAY;
    private int mMinMovedPixelsToUpdate = DEFAULT_MIN_MOVED_PIXELS;
    private float mMinSensibility = DEFAULT_MIN_SENSIBILITY;
    
    private float mSensorX;
    private float mSensorY;
    
    private Float mFirstSensorX;
    private Float mFirstSensorY;
    private Float mPreviousSensorX;
    private Float mPreviousSensorY;
    
    private float mTranslationX = 0;
    private float mTranslationY = 0;
    
    private SensorManager mSensorManager;
    private Sensor mSensor;
    
    private final int sensitivity = 10;
    private float translationMultiplier;
    
    public enum SensorDelay {
        FASTEST,
        GAME,
        UI,
        NORMAL
    }
    
    public ParallaxView(Context context) {
        super(context);
    }
    
    public ParallaxView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public ParallaxView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    public void init() {
        mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        setSensor();
    }
    
    private void setNewPosition() {
        int destinyX = (int) ((mFirstSensorX - mSensorX) * mMovementMultiplier);
        int destinyY = (int) ((mFirstSensorY - mSensorY) * mMovementMultiplier);
        
        calculateTranslationX(destinyX);
        calculateTranslationY(destinyY);
    }
    
    private void calculateTranslationX(int destinyX) {
        if (mTranslationX + mMinMovedPixelsToUpdate < destinyX) {
            mTranslationX++;
        }
        else if (mTranslationX - mMinMovedPixelsToUpdate > destinyX) {
            mTranslationX--;
        }
    }
    
    private void calculateTranslationY(int destinyY) {
        if (mTranslationY + mMinMovedPixelsToUpdate < destinyY) {
            mTranslationY++;
        }
        else if (mTranslationY - mMinMovedPixelsToUpdate > destinyY) {
            mTranslationY--;
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setTranslationX(mTranslationX * getMultiplier());
        setTranslationY(mTranslationY * getMultiplier());
        setRotationX(mTranslationX);
        setRotationY(mTranslationY);
        invalidate();
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            mSensorX = event.values[0] * -sensitivity;
            mSensorY = event.values[2] * sensitivity;
            
            /*
            if (mSensorX > 5.0) {
                mSensorX = 4.0f;
            }
            else if (mSensorX < -5.0) {
                mSensorX = -4.0f;
            }
    
            if (mSensorY > 5.0) {
                mSensorY = 5.0f;
            }
            else if (mSensorY < -5.0) {
                mSensorY = -4.0f;
            }
             */
            
            manageSensorValues();
        }
        else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mSensorX = event.values[1];
            mSensorY = event.values[0];
            
            manageSensorValues();
        }
    }
    
    public void calibrate() {
        mFirstSensorX = mFirstSensorY = mPreviousSensorX = mPreviousSensorY = null;
        manageSensorValues();
    }
    
    private void manageSensorValues() {
        if (mFirstSensorX == null) {
            setFirstSensorValues();
        }
        
        if (mPreviousSensorX == null || isSensorValuesMovedEnough()) {
            setNewPosition();
            setPreviousSensorValues();
        }
    }
    
    private void setFirstSensorValues() {
        mFirstSensorX = mSensorX;
        mFirstSensorY = mSensorY;
    }
    
    private void setPreviousSensorValues() {
        mPreviousSensorX = mSensorX;
        mPreviousSensorY = mSensorY;
    }
    
    private boolean isSensorValuesMovedEnough() {
        return mSensorX > mPreviousSensorX + mMinSensibility ||
                mSensorX < mPreviousSensorX - mMinSensibility ||
                mSensorY > mPreviousSensorY + mMinSensibility ||
                mSensorY < mPreviousSensorX - mMinSensibility;
    }
    
    public void registerSensorListener() {
        mSensorManager.registerListener(this, mSensor, mSensorDelay);
    }
    
    public void registerSensorListener(SensorDelay sensorDelay) {
        switch (sensorDelay) {
            case FASTEST:
                mSensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
                break;
            case GAME:
                mSensorDelay = SensorManager.SENSOR_DELAY_GAME;
                break;
            case UI:
                mSensorDelay = SensorManager.SENSOR_DELAY_UI;
                break;
            case NORMAL:
                mSensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
                break;
        }
        registerSensorListener();
    }
    
    public void unregisterSensorListener() {
        mSensorManager.unregisterListener(this);
    }
    
    public void resetTranslationValues() {
        mTranslationY = 0;
        mTranslationX = 0;
        unregisterSensorListener();
    }
    
    public void setSensor() {
        PackageManager packageManager = getContext().getPackageManager();
        
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE)) {
            //Toast.makeText(getContext(), "Gyro", Toast.LENGTH_SHORT).show();
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }
        else if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)) {
            //Toast.makeText(getContext(), "Acce", Toast.LENGTH_SHORT).show();
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        else {
            mSensor = null;
        }
    }
    
    public void setMovementMultiplier(float multiplier) {
        mMovementMultiplier = multiplier;
    }
    
    public void setMinimumMovedPixelsToUpdate(int minMovedPixelsToUpdate) {
        mMinMovedPixelsToUpdate = minMovedPixelsToUpdate;
    }
    
    public void setMinimumSensibility(int minSensibility) {
        mMinSensibility = minSensibility;
    }
    
    public float getMultiplier() {
        return translationMultiplier;
    }
    
    public void setTranslationMultiplier(float translationMultiplier) {
        this.translationMultiplier = translationMultiplier;
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
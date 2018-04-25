package com.example.cranberry;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.baidu.mapapi.map.MyLocationData;

/**
 * Created by 李玉娟 on 2018/4/23.
 */

public class MyOrientationListener implements SensorEventListener {

    private Context context;
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetic;

    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];

    private float lastX ;
    public static final float PI = 3.14f;

    private OnOrientationListener onOrientationListener ;

    public MyOrientationListener(Context context){
        this.context = context;
    }

    // 创建传感器
    public void start()
    {
        // 获得传感器管理器
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        // 获得方向传感器
        if (mSensorManager != null)
        {
            // 初始化加速器传感器
            accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(this, accelerometer, Sensor.TYPE_ACCELEROMETER);
            // 初始化地磁场传感器
            magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            mSensorManager.registerListener(this, magnetic, Sensor.TYPE_MAGNETIC_FIELD);
        }
    }

    // 停止传感器监听
    public void stop()
    {
        mSensorManager.unregisterListener(this);
    }
    @Override
    public void onSensorChanged(SensorEvent event){
        float[] R = new float[9];
        float[] values =new float[3];
        float x;

        // 接收传感器类型
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticFieldValues = event.values;
        }
        // 获得方位角数据
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);
        x = values[0]*PI/180;
        if(Math.abs(x-lastX)>1.0)
        {
            onOrientationListener.onOrientationChanged(x);
        }
        lastX = x;
    }

    public void setOnOrientationListener(OnOrientationListener onOrientationListener)
    {
        this.onOrientationListener = onOrientationListener ;
    }


    public interface OnOrientationListener
    {
        void onOrientationChanged(float x);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }
}

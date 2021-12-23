package com.devnation.helix_timex;


import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.crrepa.ble.CRPBleClient;
import com.crrepa.ble.conn.CRPBleConnection;
import com.crrepa.ble.conn.CRPBleDevice;
import com.crrepa.ble.conn.bean.CRPHeartRateInfo;
import com.crrepa.ble.conn.bean.CRPMovementHeartRateInfo;
import com.crrepa.ble.conn.listener.CRPBleConnectionStateListener;
import com.crrepa.ble.conn.listener.CRPBleECGChangeListener;
import com.crrepa.ble.conn.listener.CRPBloodOxygenChangeListener;
import com.crrepa.ble.conn.listener.CRPBloodPressureChangeListener;
import com.crrepa.ble.conn.listener.CRPFindPhoneListener;
import com.crrepa.ble.conn.listener.CRPHeartRateChangeListener;
import com.crrepa.ble.conn.listener.CRPStepsCategoryChangeListener;
import com.crrepa.ble.conn.type.CRPEcgMeasureType;
import com.crrepa.ble.scan.bean.CRPScanDevice;
import com.crrepa.ble.scan.callback.CRPScanCallback;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * HelixTimexPlugin
 */
public class HelixTimexPlugin  implements FlutterPlugin, MethodCallHandler {


    private MethodChannel channel;

    private Context context;


    private ScanResultsAdapter mResultsAdapter;
    private CRPBleClient mBleClient;
    String macAdd="";
    CRPBleDevice mBleDevice;
    CRPBleConnection mBleConnection;




    private Handler handler;



    private static final int SCAN_PERIOD = 10 * 1000;
    private boolean mScanState = false;;


    private EventChannel heartRateEventChannel;
    private EventChannel.EventSink heartRateDataSink;

    private EventChannel spo2EventChannel;
    private EventChannel.EventSink spo2DataSink;

    private EventChannel bloodPressureEventChannel;
    private EventChannel.EventSink bloodPressureDataSink;

    private EventChannel deviceFoundEventChannel;
    private EventChannel.EventSink deviceFoundSink;


    private EventChannel scanningState;
    private EventChannel.EventSink scanningStateSink;

    private EventChannel connectionState;
    private EventChannel.EventSink connectionStateSink;


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        context= flutterPluginBinding.getApplicationContext();
        mBleClient=CRPBleClient.create(context);
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "helix_timex");
        channel.setMethodCallHandler(this);


        //  synth = new Synth();
        handler = new Handler(Looper.getMainLooper());

        heartRateEventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "helix_timex_heartRate");
        heartRateEventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object listener, EventChannel.EventSink eventSink) {
                heartRateDataSink = eventSink;
            }

            @Override
            public void onCancel(Object listener) {
                heartRateDataSink = null;
            }
        });

        spo2EventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "helix_timex_spo2");
        spo2EventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object listener, EventChannel.EventSink eventSink) {
                spo2DataSink = eventSink;
            }

            @Override
            public void onCancel(Object listener) {
                spo2DataSink = null;
            }
        });

        bloodPressureEventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "helix_timex_bloodPressure");
        bloodPressureEventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object listener, EventChannel.EventSink eventSink) {
                bloodPressureDataSink = eventSink;
            }

            @Override
            public void onCancel(Object listener) {
                bloodPressureDataSink = null;
            }
        });


        deviceFoundEventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "helix_timex_device_found_stream");
        deviceFoundEventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object listener, EventChannel.EventSink eventSink) {

                deviceFoundSink = eventSink;

            }

            @Override
            public void onCancel(Object listener) {
                deviceFoundSink = null;
            }
        });


        scanningState = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "helix_timex_device_scanning_state");
        scanningState.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object listener, EventChannel.EventSink eventSink) {

                scanningStateSink = eventSink;

            }

            @Override
            public void onCancel(Object listener) {
                scanningStateSink = null;
            }
        });

        connectionState = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "helix_timex_device_connection_state");
        connectionState.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object listener, EventChannel.EventSink eventSink) {

                connectionStateSink = eventSink;

            }

            @Override
            public void onCancel(Object listener) {
                connectionStateSink = null;
            }
        });


    }


    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("startScanDevice")) {
            try {


                Log.i("TAG", "Yessssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss" + call.method.toString());
//        ArrayList arguments = (ArrayList) call.arguments;
//        Log.d("Connect Array", arguments.toString());
//        Log.d("Connect Array", arguments.get(0).toString());
                // startScanDevice();
                // ScanActivity.startScan();
                try {

                    startScan();
                    result.success(1);
                } catch (Exception ex) {
                    result.success(0);
                    Log.d("Exception", ex.toString());
                }

                //  eventChannelSink.success("Yessssssssssssssssssssssssssss Dataaaaaaaaaaaaa");

            } catch (Exception ex) {

                result.error("1", ex.getMessage(), ex.getStackTrace());
            }
        } else if (call.method.equals("disConnect")) {
            try {

                disConnectDevice();
                result.success(1);
            } catch (Exception ex) {
                result.error("1", ex.getMessage(), ex.getStackTrace());
            }
        } else if (call.method.equals("connect")) {
            try {
                ArrayList arguments = (ArrayList) call.arguments;
                connectDevice(arguments.get(0).toString(),arguments.get(1).toString());
                result.success(1);
            } catch (Exception ex) {
                result.error("1", ex.getMessage(), ex.getStackTrace());
            }
        }
        else if (call.method.equals("measureDynamicRate")) {
            try {
                measureDynamicRate();
                result.success(1);
            } catch (Exception ex) {
                result.error("1", ex.getMessage(), ex.getStackTrace());
            }
        }

        else if (call.method.equals("measureHeartRate")) {
            try {
                measureHeartRate();
                result.success(1);
            } catch (Exception ex) {
                result.error("1", ex.getMessage(), ex.getStackTrace());
            }
        }
        else if (call.method.equals("measureBloodPressure")) {
            try {
                measureBloodPressure();
                result.success(1);
            } catch (Exception ex) {
                result.error("1", ex.getMessage(), ex.getStackTrace());
            }
        }
        else if (call.method.equals("measureSpo2")) {
            try {
                measureSpo2();
                result.success(1);
            } catch (Exception ex) {
                result.error("1", ex.getMessage(), ex.getStackTrace());
            }
        }

        else {
            result.notImplemented();
        }
    }


    private void startScan() {
        boolean success =  mBleClient.scanDevice(new CRPScanCallback() {
            @Override
            public void onScanning(final CRPScanDevice device) {
                if(scanningStateSink!=null){
                    scanningStateSink.success(true);
                }


                if(device.getDevice().getName()!=null)
                {
                    if(device.getDevice().getName().equals("HXW01")){
                        Map<String, Object> searchResultData = new HashMap<>();
                        searchResultData.put("macAddress", device.getDevice().getAddress().toString());
                        searchResultData.put("deviceName", device.getDevice().getName().toString());
                        macAdd=device.getDevice().getAddress();

                        Log.i("Search Result", searchResultData.toString());

                        if (deviceFoundSink != null) {
                            deviceFoundSink.success(searchResultData);
                        }
                    }
                }



                Log.d("TAG", "name: " + device.getDevice().getName());
                Log.d("TAG", "address: " + device.getDevice().getAddress());
                if (TextUtils.isEmpty(device.getDevice().getName())) {
                    return;
                }

            }

            @Override
            public void onScanComplete(List<CRPScanDevice> results) {
                if(scanningStateSink!=null){
                    scanningStateSink.success(false);
                }
                if (mScanState) {
                    mScanState = false;
                }
            }
        }, SCAN_PERIOD);
        if (success) {
            mScanState = true;
        }
    }

    private void cancelScan() {
         mBleClient.cancelScan();
    }




    void connectDevice(String macAddress,String deviceName) {
        mBleDevice=mBleClient.getBleDevice(macAddress);

        mBleConnection = mBleDevice.connect();
        mBleConnection.setConnectionStateListener(newState -> {
            Log.d("TAG", "onConnectionStateChangeYes: " + newState);
            int state = -1;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(connectionStateSink!=null){
                        connectionStateSink.success(newState);
                    }
                }});


            switch (newState) {
                case CRPBleConnectionStateListener.STATE_CONNECTED:
                    testSet();
                    break;
                case CRPBleConnectionStateListener.STATE_CONNECTING:

                    break;
                case CRPBleConnectionStateListener.STATE_DISCONNECTED:

                    break;
            }

        });

        mBleConnection.setHeartRateChangeListener(mHeartRateChangListener);
        mBleConnection.setBloodPressureChangeListener(mBloodPressureChangeListener);
        mBleConnection.setBloodOxygenChangeListener(mBloodOxygenChangeListener);
        mBleConnection.setFindPhoneListener(mFindPhoneListener);
        mBleConnection.setECGChangeListener(mECGChangeListener, CRPEcgMeasureType.TYHX);
        mBleConnection.setStepsCategoryListener(mStepsCategoryChangeListener);


    }

    private void testSet() {
        Log.d("TAG", "testSet");
        mBleConnection.syncTime();
    }


    void measureDynamicRate(){
        if (mBleConnection != null) {
            mBleConnection.enableTimingMeasureHeartRate(1);
        }
    }


    void measureHeartRate(){
        if (mBleConnection != null) {
            mBleConnection.startMeasureOnceHeartRate();
        }
    }

    void measureBloodPressure(){
        if (mBleConnection != null) {
            mBleConnection.startMeasureBloodPressure();
        }
    }

    void measureSpo2(){
        if (mBleConnection != null) {
            mBleConnection.startMeasureBloodOxygen();
        }
    }



    void disConnectDevice(){

        if (mBleDevice != null) {
            mBleDevice.disconnect();
        }
    }



    CRPHeartRateChangeListener mHeartRateChangListener = new CRPHeartRateChangeListener() {
        @Override
        public void onMeasuring(int rate) {
            Log.d("TAG", "onMeasuring: " + rate);;
        }

        @Override
        public void onOnceMeasureComplete(int rate) {
            Log.d("TAG", "onOnceMeasureComplete: " + rate);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (heartRateDataSink != null) {
                        heartRateDataSink.success(rate);
                    }

                }});

        }

        @Override
        public void onMeasureComplete(CRPHeartRateInfo info) {
            if (info != null && info.getMeasureData() != null) {
                for (Integer integer : info.getMeasureData()) {
                    Log.d("TAG", "onMeasureComplete: " + integer);
                }
            }
        }

        @Override
        public void on24HourMeasureResult(CRPHeartRateInfo info) {
            List<Integer> data = info.getMeasureData();
            Log.d("TAG", "on24HourMeasureResult: " + data.size());
        }

        @Override
        public void onMovementMeasureResult(List<CRPMovementHeartRateInfo> list) {
            for (CRPMovementHeartRateInfo info : list) {
                if (info != null) {
                    Log.d("TAG", "onMovementMeasureResult: " + info.getStartTime());
                }
            }
        }

    };

    CRPBloodPressureChangeListener mBloodPressureChangeListener = new CRPBloodPressureChangeListener() {
        @Override
        public void onBloodPressureChange(int sbp, int dbp) {
            Log.d("TAG", "sbp: " + sbp + ",dbp: " + dbp);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Map<String, Object> discoveryResult = new HashMap<>();
                    discoveryResult.put("sbp", sbp);
                    discoveryResult.put("dbp", dbp);

                    Log.i("Detected data", discoveryResult.toString());

                    if (bloodPressureDataSink != null) {
                        bloodPressureDataSink.success(discoveryResult);
                    }


                }});





        }
    };

    CRPBloodOxygenChangeListener mBloodOxygenChangeListener = new CRPBloodOxygenChangeListener() {
        @Override
        public void onBloodOxygenChange(int bloodOxygen) {
            Log.d("TAG", "Blood Oxygen: "  +bloodOxygen);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (spo2DataSink != null) {
                        spo2DataSink.success(bloodOxygen);
                    }

                }});


        }
    };

    CRPBleECGChangeListener mECGChangeListener = new CRPBleECGChangeListener() {
        @Override
        public void onECGChange(int[] ecg) {
            for (int i = 0; i < ecg.length; i++) {
                Log.d("TAG", "ecg: " + ecg[i]);
            }
        }

        @Override
        public void onMeasureComplete() {
            Log.d("TAG", "onMeasureComplete");
        }

        @Override
        public void onTransCpmplete(Date date) {
            Log.d("TAG", "onTransCpmplete");
        }

        @Override
        public void onCancel() {
            Log.d("TAG", "onCancel");
        }

        @Override
        public void onFail() {
            Log.d("TAG", "onFail");
        }
    };


    CRPFindPhoneListener mFindPhoneListener = new CRPFindPhoneListener() {
        @Override
        public void onFindPhone() {
            Log.d("TAG", "onFindPhone");
        }

        @Override
        public void onFindPhoneComplete() {
            Log.d("TAG", "onFindPhoneComplete");
        }
    };

    CRPStepsCategoryChangeListener mStepsCategoryChangeListener = info -> {
        List<Integer> stepsList = info.getStepsList();
        Log.d("TAG", "onStepsCategoryChange size: " + stepsList.size());
        for (int i = 0; i < stepsList.size(); i++) {
            Log.d("TAG", "onStepsCategoryChange: " + stepsList.get(i).intValue());
        }
    };
    
    

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

}





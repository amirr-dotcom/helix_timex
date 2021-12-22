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
import com.crrepa.ble.scan.bean.CRPScanDevice;
import com.crrepa.ble.scan.callback.CRPScanCallback;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * HelixTimexPlugin
 */
public class HelixTimexPlugin extends Application implements FlutterPlugin, MethodCallHandler {


    private MethodChannel channel;


    private ScanResultsAdapter mResultsAdapter;








    private static final int SCAN_PERIOD = 10 * 1000;
    private boolean mScanState = false;;


    private EventChannel detectDataEventChannel;
    private EventChannel.EventSink detectDataSink;


    private EventChannel scanningState;
    private EventChannel.EventSink scanningStateSink;

    private EventChannel connectionState;
    private EventChannel.EventSink connectionStateSink;


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "helix_timex");
        channel.setMethodCallHandler(this);


        //  synth = new Synth();


        detectDataEventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "helix_timex_detect_data");
        detectDataEventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object listener, EventChannel.EventSink eventSink) {
                detectDataSink = eventSink;
            }

            @Override
            public void onCancel(Object listener) {
                detectDataSink = null;
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

                ArrayList arguments = (ArrayList) call.arguments;
                //  disConnectDevice(arguments.get(0).toString());
                result.success(1);
            } catch (Exception ex) {
                result.error("1", ex.getMessage(), ex.getStackTrace());
            }
        } else if (call.method.equals("connect")) {
            try {
                ArrayList arguments = (ArrayList) call.arguments;
                //  connectOximeter(arguments.get(0).toString(),arguments.get(1).toString());
                result.success(1);
            } catch (Exception ex) {
                result.error("1", ex.getMessage(), ex.getStackTrace());
            }
        } else {
            result.notImplemented();
        }
    }


    private void startScan() {
        boolean success =  CRPBleClient.create(this).scanDevice(new CRPScanCallback() {
            @Override
            public void onScanning(final CRPScanDevice device) {
                Log.d("TAG", "name: " + device.getDevice().getName());
                Log.d("TAG", "address: " + device.getDevice().getAddress());
                if (TextUtils.isEmpty(device.getDevice().getName())) {
                    return;
                }

            }

            @Override
            public void onScanComplete(List<CRPScanDevice> results) {
                if (mScanState) {
                    mScanState = false;
                }
            }
        }, SCAN_PERIOD);
        if (success) {
            mScanState = true;
            mResultsAdapter.clearScanResults();
        }
    }

    private void cancelScan() {
         SampleApplication.getBleClient(this).cancelScan();
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

}





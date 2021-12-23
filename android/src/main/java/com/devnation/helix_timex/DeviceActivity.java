package com.devnation.helix_timex;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by bill on 2017/5/15.
 */

public class DeviceActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "DeviceActivity";
    public static final String DEVICE_MACADDR = "device_macaddr";
    ProgressDialog mProgressDialog;
    CRPBleClient mBleClient;
    CRPBleDevice mBleDevice;
    CRPBleConnection mBleConnection;
    TextView tvConnectState;
    TextView tvHeartRate, txtId;
    TextView tv_bp_sys, tv_bp_dias;
    TextView tvBloodOxygen;
    Button btnSubmit, btnBleDisconnect, btn_start_measure_heart_rate, btn_stop_measure_heart_rate, btn_start_measure_blood_pressure, btn_stop_measure_blood_pressure, btn_start_measure_blood_oxygen, btn_stop_measure_blood_oxygen;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgressDialog = new ProgressDialog(this);
        btn_start_measure_heart_rate.setOnClickListener(this);
        btn_stop_measure_heart_rate.setOnClickListener(this);
        btn_start_measure_blood_pressure.setOnClickListener(this);
        btn_stop_measure_blood_pressure.setOnClickListener(this);
        btn_start_measure_blood_oxygen.setOnClickListener(this);
        btn_stop_measure_blood_oxygen.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        String macAddr = getIntent().getStringExtra(DEVICE_MACADDR);
        if (TextUtils.isEmpty(macAddr)) {
            finish();
            return;
        }

        mBleClient = new SampleApplication().getBleClient(this);
        mBleDevice = mBleClient.getBleDevice(macAddr);
        if (mBleDevice != null) {
            connect();
        }
        btnBleDisconnect.setOnClickListener(view -> {
            if (mBleDevice.isConnected()) {
                mBleDevice.disconnect();
            } else {
                connect();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBleDevice != null) {
            mBleDevice.disconnect();
        }
    }


    void connect() {
        mProgressDialog.show();
        mBleConnection = mBleDevice.connect();
        mBleConnection.setConnectionStateListener(newState -> {
            Log.d(TAG, "onConnectionStateChange: " + newState);
            int state = -1;

            switch (newState) {
                case CRPBleConnectionStateListener.STATE_CONNECTED:
                    //state = R.string.connected;
                    mProgressDialog.dismiss();
                  //  updateTextView(btnBleDisconnect, getString(R.string.disconnect));
                    testSet();
                    break;
                case CRPBleConnectionStateListener.STATE_CONNECTING:
                  //  state = R.string.bluetooth_connecting;
                    break;
                case CRPBleConnectionStateListener.STATE_DISCONNECTED:
                  //  state = R.string.disconnected;
                    mProgressDialog.dismiss();
                   // updateTextView(btnBleDisconnect, getString(R.string.connect));
                    break;
            }
            updateConnectState(state);
        });

        mBleConnection.setHeartRateChangeListener(mHeartRateChangListener);
        mBleConnection.setBloodPressureChangeListener(mBloodPressureChangeListener);
        mBleConnection.setBloodOxygenChangeListener(mBloodOxygenChangeListener);
        mBleConnection.setFindPhoneListener(mFindPhoneListener);
        mBleConnection.setECGChangeListener(mECGChangeListener, CRPEcgMeasureType.TYHX);
        mBleConnection.setStepsCategoryListener(mStepsCategoryChangeListener);

    }

    private void testSet() {
        Log.d(TAG, "testSet");
        mBleConnection.syncTime();
    }

    private void sendFindBandMessage() {
        handler.sendEmptyMessageDelayed(1, 5000);
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                mBleConnection.findDevice();
                sendFindBandMessage();
            }
        }
    };

    CRPStepsCategoryChangeListener mStepsCategoryChangeListener = info -> {
        List<Integer> stepsList = info.getStepsList();
        Log.d(TAG, "onStepsCategoryChange size: " + stepsList.size());
        for (int i = 0; i < stepsList.size(); i++) {
            Log.d(TAG, "onStepsCategoryChange: " + stepsList.get(i).intValue());
        }
    };

    CRPHeartRateChangeListener mHeartRateChangListener = new CRPHeartRateChangeListener() {
        @Override
        public void onMeasuring(int rate) {
            Log.d(TAG, "onMeasuring: " + rate);
            updateTextView(tvHeartRate, String.valueOf(rate));
        }

        @Override
        public void onOnceMeasureComplete(int rate) {
            Log.d(TAG, "onOnceMeasureComplete: " + rate);
        }

        @Override
        public void onMeasureComplete(CRPHeartRateInfo info) {
            if (info != null && info.getMeasureData() != null) {
                for (Integer integer : info.getMeasureData()) {
                    Log.d(TAG, "onMeasureComplete: " + integer);
                }
            }
        }

        @Override
        public void on24HourMeasureResult(CRPHeartRateInfo info) {
            List<Integer> data = info.getMeasureData();
            Log.d(TAG, "on24HourMeasureResult: " + data.size());
        }

        @Override
        public void onMovementMeasureResult(List<CRPMovementHeartRateInfo> list) {
            for (CRPMovementHeartRateInfo info : list) {
                if (info != null) {
                    Log.d(TAG, "onMovementMeasureResult: " + info.getStartTime());
                }
            }
        }

    };

    CRPBloodPressureChangeListener mBloodPressureChangeListener = new CRPBloodPressureChangeListener() {
        @Override
        public void onBloodPressureChange(int sbp, int dbp) {
            Log.d(TAG, "sbp: " + sbp + ",dbp: " + dbp);
            updateTextView(tv_bp_sys, String.valueOf(sbp));
            updateTextView(tv_bp_dias, String.valueOf(dbp));
        }
    };

    CRPBloodOxygenChangeListener mBloodOxygenChangeListener = new CRPBloodOxygenChangeListener() {
        @Override
        public void onBloodOxygenChange(int bloodOxygen) {
            updateTextView(tvBloodOxygen, String.valueOf(bloodOxygen));
        }
    };

    CRPBleECGChangeListener mECGChangeListener = new CRPBleECGChangeListener() {
        @Override
        public void onECGChange(int[] ecg) {
            for (int i = 0; i < ecg.length; i++) {
                Log.d(TAG, "ecg: " + ecg[i]);
            }
        }

        @Override
        public void onMeasureComplete() {
            Log.d(TAG, "onMeasureComplete");
        }

        @Override
        public void onTransCpmplete(Date date) {
            Log.d(TAG, "onTransCpmplete");
        }

        @Override
        public void onCancel() {
            Log.d(TAG, "onCancel");
        }

        @Override
        public void onFail() {
            Log.d(TAG, "onFail");
        }
    };


    private void queryLastMeasureECGData() {
        this.mBleConnection.queryLastMeasureECGData();
    }


    CRPFindPhoneListener mFindPhoneListener = new CRPFindPhoneListener() {
        @Override
        public void onFindPhone() {
            Log.d(TAG, "onFindPhone");
        }

        @Override
        public void onFindPhoneComplete() {
            Log.d(TAG, "onFindPhoneComplete");
        }
    };
    
    

    void updateConnectState(final int state) {
        if (state < 0) {
            return;
        }
        updateTextView(tvConnectState, getString(state));
    }

    void updateTextView(final TextView view, final String con) {
        runOnUiThread(() -> view.setText(con));
    }

    @Override
    public void onClick(View view) {

    }

    public void saveBluetoothVital(JSONArray dt) {

    }
}

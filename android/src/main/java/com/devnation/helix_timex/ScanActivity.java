package com.devnation.helix_timex;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crrepa.ble.CRPBleClient;
import com.crrepa.ble.scan.bean.CRPScanDevice;
import com.crrepa.ble.scan.callback.CRPScanCallback;


import java.io.File;
import java.util.List;

public class ScanActivity extends AppCompatActivity {
    private static final String TAG = "ScanActivity";
    private static final int SCAN_PERIOD = 10 * 1000;

    private static final int REQUEST_UPDATEBANDCONFIG = 4;
    private static final String[] PERMISSION_UPDATEBANDCONFIG = new String[] {
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION"};

//    @BindView(R.id.btn_scan_toggle)
    Button scanToggleBtn;
//    @BindView(R.id.scan_results)
    RecyclerView scanResults;
    /*@BindView(R.id.tv_firmware_fix_state)
    TextView tvFirmwareFixState;*/
    private CRPBleClient mBleClient;
    private ScanResultsAdapter mResultsAdapter;
    private boolean mScanState = false;


    private static final String UPGRADE_APP_FILE_PATH = Environment.getExternalStorageDirectory().getPath()
            + File.separator + "crrepa" + File.separator + "app_band-hs.bin";
    private static final String UPGRADE_USER_FILE_PATH = Environment.getExternalStorageDirectory().getPath()
            + File.separator + "crrepa" + File.separator + "usr.bin";
    private static final String USER_START_ADDRESS = "23000";
//    private static final String BAND_ADDRESS = "C1:C4:7C:DE:44:5B";
//    private static final String BAND_ADDRESS = "D9:4D:C2:BB:F3:F4";
    private static final String BAND_ADDRESS = "FB:09:C5:C7:1A:90";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        ButterKnife.bind(this);
//        sampleApplication=new SampleApplication();
        mBleClient = new SampleApplication().getBleClient(this);

        configureResultList();

        requestPermissions();
        scanToggleBtn.setOnClickListener(view -> {
            if (!mBleClient.isBluetoothEnable()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBtIntent);
                return;
            }

            if (mScanState) {
                cancelScan();
            } else {
                startScan();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelScan();
    }

    /*@OnClick({R.id.btn_scan_toggle})
    public void onViewClicked(View view) {
        if (!mBleClient.isBluetoothEnable()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            return;
        }

        switch (view.getId()) {
            case R.id.btn_scan_toggle:
                if (mScanState) {
                    cancelScan();
                } else {
                    startScan();
                }
                break;
        }

    }*/

    private void startScan() {
        boolean success = mBleClient.scanDevice(new CRPScanCallback() {
            @Override
            public void onScanning(final CRPScanDevice device) {
                Log.d(TAG, "address: " + device.getDevice().getAddress());
                if (TextUtils.isEmpty(device.getDevice().getName())) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mResultsAdapter.addScanResult(device);
                    }
                });
            }

            @Override
            public void onScanComplete(List<CRPScanDevice> results) {
                if (mScanState) {
                    mScanState = false;
                    updateButtonUIState();
                }
            }
        }, SCAN_PERIOD);
        if (success) {
            mScanState = true;
            updateButtonUIState();
            mResultsAdapter.clearScanResults();
        }
    }

    private void cancelScan() {
        mBleClient.cancelScan();
    }


    private void configureResultList() {
//        scanResults.setHasFixedSize(true);
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(this);
        scanResults.setLayoutManager(recyclerLayoutManager);
        mResultsAdapter = new ScanResultsAdapter();
        scanResults.setAdapter(mResultsAdapter);
        mResultsAdapter.setOnAdapterItemClickListener(new ScanResultsAdapter.OnAdapterItemClickListener() {
            @Override
            public void onAdapterViewClick(View view) {
                final int childAdapterPosition = scanResults.getChildAdapterPosition(view);
                final CRPScanDevice itemAtPosition = mResultsAdapter.getItemAtPosition(childAdapterPosition);
                onAdapterItemClick(itemAtPosition);
            }
        });
    }


    private void onAdapterItemClick(CRPScanDevice scanResults) {
        final String macAddress = scanResults.getDevice().getAddress();
        mBleClient.cancelScan();

        final Intent intent = new Intent(this, DeviceActivity.class);
        intent.putExtra(DeviceActivity.DEVICE_MACADDR, macAddress);
        startActivity(intent);
    }


    private void updateButtonUIState() {
     //   scanToggleBtn.setText(mScanState ? R.string.stop_scan : R.string.start_scan);
    }

    void updateTextView(final TextView view, final String con) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setText(con);
            }
        });
    }


    void requestPermissions() {
        if (!PermissionUtils.hasSelfPermissions(this, PERMISSION_UPDATEBANDCONFIG)) {
            ActivityCompat.requestPermissions(
                    this, PERMISSION_UPDATEBANDCONFIG, REQUEST_UPDATEBANDCONFIG);
        }
    }
}

package xkw.example.com.androidBLE;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

/**
 * @author xkw
 */
public class SearchDeviceActivity extends AppCompatActivity {

    /**
     * constant
     */
    private static final String TAG = SearchDeviceActivity.class.getSimpleName();
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;


    /**
     * variable
     */
    private Handler mHandler;
    private BLEAdapter mBLEAdapter;
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_device);

        mHandler = new Handler();
        mBLEAdapter = new BLEAdapter();
        mBLEAdapter.setClickListener(new BLEAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                BLEDevice device = mBLEAdapter.getDevice(position);
                Intent intent = new Intent();
                intent.putExtra(MainActivity.DEVICE_ADDRESS, device.getDeviceAddress());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        RecyclerView deviceList = (RecyclerView) findViewById(R.id.rv_device);
        deviceList.setLayoutManager(new LinearLayoutManager(this));
        deviceList.setAdapter(mBLEAdapter);

        startSearchBLE();

        Toast.makeText(this,"连接设备",Toast.LENGTH_SHORT).show();
    }

    public void startSearchBLE(){
        //判断本机是否支持蓝牙
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        if (bluetoothManager != null){
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }
        if (mBluetoothAdapter == null)
        {
            Toast.makeText(this, R.string.ble_not_supported,Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //Android6.0以上需要动态获取权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MainActivity.PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

        //检查蓝牙是否开启,若未开启则提醒用户
        if (!mBluetoothAdapter.isEnabled()){
            Intent enableBTintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTintent, MainActivity.REQUEST_ENABLE_BT);
        }

        scanLeDevice(true);
    }

    //扫描到设备时候的回调函数
    private BluetoothAdapter.LeScanCallback mLeScanCallBack = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "Device name:" + bluetoothDevice.getName());
                    BLEDevice mBLEDevice = new BLEDevice(bluetoothDevice.getName(), bluetoothDevice.getAddress());
                    mBLEAdapter.addDevice(mBLEDevice);
                    mBLEAdapter.notifyDataSetChanged();
                    mBLEAdapter.updateSections();
                }
            });
        }
    };

    private void scanLeDevice(final boolean enable){
        if (enable){
            //在预定的扫描时间之后停止扫描
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallBack);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(mLeScanCallBack);
        }
        else {
            mBluetoothAdapter.stopLeScan(mLeScanCallBack);
        }
        invalidateOptionsMenu();
    }
}

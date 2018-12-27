package xkw.example.com.androidBLE;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @author xkw
 */
public class MainActivity extends AppCompatActivity
        implements OnChartValueSelectedListener,OnChartGestureListener {

    /**
     * constant
     */
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String DEVICE_ADDRESS = "device_address";
    private static final String MEASURE_TEMPERATURE = "temperature";
    private static final String MEASURE_DISTANCE = "distance";
    private static final int REQUEST_CODE = 0x01;
    public static final int REQUEST_ENABLE_BT = 0x02;
    public static final int PERMISSION_REQUEST_COARSE_LOCATION = 0x03;

    /**
     * UI variable
     */
    private LineChart mChart;
    private TextView tvRec;

    /**
     * variable
     */
    private ArrayList<Float> temperatureData = new ArrayList<>();
    private ArrayList<Float> distanceData = new ArrayList<>();
    private StringBuilder temperatureStr = new StringBuilder();
    private StringBuilder distanceStr = new StringBuilder();
    private boolean mConnected = false;
    private boolean ifService = false;
    private boolean isTemperature = true;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattService rd_wr_GattService;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothGattCharacteristic rd_wr_Characteristic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(mToolbar);

        Button btMeasure = (Button) findViewById(R.id.btn_measure);
        btMeasure.setOnClickListener(view -> {
            sendData(isTemperature ? MEASURE_TEMPERATURE : MEASURE_DISTANCE);
            Random random = new Random();
            addData(random.nextFloat() * 40);
            refreshUI();
        });

        RadioGroup categoryGroup = (RadioGroup) findViewById(R.id.category_group);
        categoryGroup.setOnCheckedChangeListener(((group, checkedId) -> {
            isTemperature = checkedId == R.id.measure_temperature;
            refreshUI();
        }));

        tvRec = (TextView) findViewById(R.id.tv_data_rec);
        tvRec.setMovementMethod(new ScrollingMovementMethod());

        initChart();

        // start the service
        Intent gattServiceIntent = new Intent(this.getApplicationContext(), BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        updateConnectionState();
        refreshUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        if (mConnected){
            menu.findItem(R.id.connect_device).setVisible(false);
            menu.findItem(R.id.disconnect_device).setVisible(true);
        }else {
            menu.findItem(R.id.connect_device).setVisible(true);
            menu.findItem(R.id.disconnect_device).setVisible(false);
        }
        menu.findItem(R.id.clear_temperature).setVisible(true);
        menu.findItem(R.id.clear_distance).setVisible(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.connect_device:
                Log.e(TAG, "connect to device");
                Intent intent = new Intent(MainActivity.this, SearchDeviceActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case R.id.disconnect_device:
                Log.e(TAG, "disconnect the device");
                Toast.makeText(this,"断开设备",Toast.LENGTH_SHORT).show();
                mConnected = false;
                mBluetoothLeService.disconnect();
                invalidateOptionsMenu();
                break;
            case R.id.clear_temperature:
                Log.e(TAG, "clear temperature data");
                Toast.makeText(this, getString(R.string.clear_temperature_data), Toast.LENGTH_SHORT).show();
                temperatureData.clear();
                temperatureStr.delete(0, temperatureStr.length());
                refreshUI();
                break;
            case R.id.clear_distance:
                Log.e(TAG, "clear distance data");
                Toast.makeText(this, getString(R.string.clear_distance_data), Toast.LENGTH_SHORT).show();
                distanceData.clear();
                distanceStr.delete(0, distanceStr.length());
                refreshUI();
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            if (data != null){
                String address = data.getStringExtra(DEVICE_ADDRESS);
                connect(address);
            }
        }
        //用户未开启蓝牙则退出程序
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED){
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Code to manage Service lifecycle
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            ifService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            ifService = false;
        }
    };

    /**
     * Handles various events fired by the Service.
     *     ACTION_GATT_CONNECTED: connected to a GATT server.
     *     ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
     *     ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
     *     ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
     *                            or notification operations.
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState();
                //使菜单无效之后会重新加载，进而重新设置item的可见
                invalidateOptionsMenu();
                Log.e(TAG, "Connect State: " + mConnected);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState();
                invalidateOptionsMenu();
                Log.e(TAG, "Connect State: " + mConnected);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                //获得数据读取服务
                rd_wr_GattService = mBluetoothLeService.getSupportedGattServices(
                        UUID.fromString(BluetoothData.DEVICE_SERVICE));
                //没有此项服务
                if (rd_wr_GattService == null){
                    Log.e(TAG, "rd_wr_GattService: null");
                }else {
                    //从该项服务中获取相应的characteristic
                    rd_wr_Characteristic = rd_wr_GattService.getCharacteristic(
                            UUID.fromString(BluetoothData.DEVICE_CHARACTERISTIC_SERVICE));
                    //没有此项characteristic
                    if (rd_wr_Characteristic == null){
                        Log.e(TAG, "rd_wr_Characteristic: null");
                    }else {
                        //获取characteristic当前的属性
                        final int charaProp = rd_wr_Characteristic.getProperties();
                        //BluetoothGattCharacteristic.PROPERTY_READ —— 数据处于可读取状态
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0){
                            if (mNotifyCharacteristic != null){
                                mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic,false);
                                mNotifyCharacteristic = null;
                            }
                            //读取数据
                            mBluetoothLeService.readCharacteristic(rd_wr_Characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0){
                            mNotifyCharacteristic = rd_wr_Characteristic;
                            mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic,true);
                        }
                    }
                }

            }else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                // 读取到数据或者数据发生改变
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.e(TAG, "Data received:" + data);
                try{
                    addData(Float.valueOf(data));
                }catch (NumberFormatException e){
                    Log.e(TAG, e.toString());
                }
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void initChart(){
        mChart = (LineChart)findViewById(R.id.data_chart);
        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);
        // no description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // remove the right y-axis
        YAxis yAxisRight = mChart.getAxisRight();
        yAxisRight.setEnabled(false);

        // add an empty data object
        mChart.setData(new LineData());

        mChart.invalidate();
    }

    private void addData(float newData){
        if (isTemperature){
            temperatureData.add(newData);
            temperatureStr.append(temperatureStr.length() == 0 ? "" : "\n");
            temperatureStr.append("温度：");
            temperatureStr.append(newData);
            temperatureStr.append("/℃");
        }else {
            distanceData.add(newData);
            distanceStr.append(distanceStr.length() == 0 ? "" : "\n");
            distanceStr.append("距离：");
            distanceStr.append(newData);
            distanceStr.append("/cm");
        }
    }

    private void refreshUI(){
        if (isTemperature){
            tvRec.setText(temperatureStr.toString());
            drawData(temperatureData);
        }else {
            tvRec.setText(distanceStr.toString());
            drawData(distanceData);
        }
    }

    private void drawData(List<Float> yVal) {
        ArrayList<Entry> showVals = new ArrayList<Entry>();
        for (int i = 0;i < yVal.size();i ++){
            showVals.add(new Entry(i, yVal.get(i)));
        }
        LineDataSet set = new LineDataSet(showVals, isTemperature ? "温度" : "距离");
        set.setLineWidth(2.5f);
        set.setCircleRadius(4.5f);
        set.setColor(Color.rgb(240, 99, 99));
        set.setCircleColor(Color.rgb(240, 99, 99));
        set.setHighLightColor(Color.rgb(190, 190, 190));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextSize(10f);
        mChart.setData(new LineData(set));
        mChart.invalidate();
    }

    private void updateConnectionState(){
        if (mConnected){
            getSupportActionBar().setSubtitle("已连接");
        }else {
            getSupportActionBar().setSubtitle("未连接");
        }
    }

    public void connect(String deviceAddress){
        if (ifService && mBluetoothLeService != null){
            mBluetoothLeService.connect(deviceAddress);
        }
    }

    /**
     * send command via Bluetooth
     * @param str command
     */
    public void sendData(String str){
        if (mConnected){
            if (rd_wr_Characteristic == null){
                Toast.makeText(MainActivity.this,"无读写服务",Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onClick: BLE not connect");
            }else {
                rd_wr_Characteristic.setValue(str);
                mBluetoothLeService.writeCharacteristic(rd_wr_Characteristic);
            }
        }
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {

    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected() {

    }
}
